package com.example.nifty50ops.controller

import android.content.Context
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.network.ApiService
import com.example.nifty50ops.repository.MarketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarketController(private val marketRepository: MarketRepository) {


    suspend fun fetchMarketData(context: Context) {
        val prefs = "NSE:13:INDEX"
        ApiService.fetchData(context, prefs)?.let { responseBody ->
            saveToDatabase(parseMarketResponse(responseBody))
        }
    }

    var previousLtp: Double? = null

    private suspend fun parseMarketResponse(response: String): List<MarketsEntity> {
        val marketsList = mutableListOf<MarketsEntity>()
        val jsonObject = JSONObject(response)
        val dataArray: JSONArray = jsonObject.optJSONArray("data") ?: JSONArray()

        // Fetch latest LTP from DB once before the loop
        if (previousLtp == null) {
            previousLtp = marketRepository.getLatestData().firstOrNull()?.ltp
        }

        for (i in 0 until dataArray.length()) {
            val marketData = dataArray.getJSONObject(i)
            val securityId = marketData.optInt("security_id", -1)
            if (securityId == -1) continue

            val name = "Nifty 50"
            val ltp = marketData.optDouble("last_price", 0.0)
            val lastTradeTime = marketData.optLong("last_update_time", 0)
            val timestamp = formatToHourMinute(lastTradeTime)


            val pointsChanged = if (previousLtp != null) {
                ltp - previousLtp!!
            } else {
                0.0 // First value, so no change
            }

            val marketsEntity = MarketsEntity(
                timestamp = timestamp,
                name = name,
                ltp = ltp,
                pointsChanged = pointsChanged.toInt()
            )

            marketsList.add(marketsEntity)
            previousLtp = ltp
        }

        return marketsList
    }


    private suspend fun saveToDatabase(marketsEntities: List<MarketsEntity>) {
        withContext(Dispatchers.IO) {
            for (marketsEntity in marketsEntities) {
                marketRepository.insertEntity(marketsEntity)
            }
        }
    }

    fun formatToHourMinute(unixTimestamp: Long): String {
        return if (unixTimestamp > 0) {
            val date = Date(unixTimestamp * 1000) // Convert to milliseconds
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } else {
            "--:--"
        }
    }
}
