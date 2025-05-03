package com.example.nifty50ops.controller

import android.content.Context
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.network.ApiService
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.utils.readSecurityIdToSymbolMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OptionsController(private val optionRepository: OptionsRepository) {

    var securityIdToSymbol = mapOf<Int, String>()

    suspend fun fetchOptionsData(context: Context) {
        securityIdToSymbol = readSecurityIdToSymbolMap(context)
        val prefs = securityIdToSymbol.keys.joinToString(",") { "NSE:$it:OPTION" }
        ApiService.fetchData(context, prefs)?.let { responseBody ->
            saveToDatabase(parseOptionResponse(responseBody))
        }
    }

    // Define this globally or in your class
    private val previousOptionMap = mutableMapOf<String, OptionsEntity>()
    private val firstOptionMap = mutableMapOf<String, OptionsEntity>()

    private fun parseOptionResponse(response: String): List<OptionsEntity> {
        val optionsList = mutableListOf<OptionsEntity>()
        val jsonObject = JSONObject(response)
        val dataArray: JSONArray = jsonObject.optJSONArray("data") ?: JSONArray()

        for (i in 0 until dataArray.length()) {
            val optionsObject = dataArray.getJSONObject(i)
            val securityId = optionsObject.optInt("security_id", -1)
            if (securityId == -1) continue

            val name = securityIdToSymbol[securityId] ?: "Unknown"
            val ltp = optionsObject.optDouble("last_price", 0.0)
            val volTraded = optionsObject.optInt("volume_traded", 0)
            val buyQty = optionsObject.optInt("total_buy_quantity", 0)
            val sellQty = optionsObject.optInt("total_sell_quantity", 0)
            val oiQty = optionsObject.optInt("oi", 0)
            val oiChange = optionsObject.optDouble("change_oi", 0.0)
            val lastTradeTime = optionsObject.optLong("last_trade_time", 0)
            val timestamp = formatToHourMinute(lastTradeTime)

            val previous = previousOptionMap[name]

            // Store first record
            val first = firstOptionMap.getOrPut(name) {
                OptionsEntity(
                    timestamp = timestamp,
                    name = name,
                    ltp = ltp,
                    buyQty = buyQty,
                    sellQty = sellQty,
                    volTraded = volTraded,
                    buyDiffPercent = 0.0,
                    sellDiffPercent = 0.0,
                    lastMinSentiment = 0.0,
                    buyStrengthPercent = 0.0,
                    sellStrengthPercent = 0.0,
                    overAllSentiment = 0.0,
                    oiQty = oiQty,
                    oiChange = oiChange
                )
            }

            val buyDiffPercent = previous?.let {
                if (it.buyQty != 0) ((buyQty - it.buyQty).toDouble() / it.buyQty) * 100 else 0.0
            } ?: 0.0

            val sellDiffPercent = previous?.let {
                if (it.sellQty != 0) ((sellQty - it.sellQty).toDouble() / it.sellQty) * 100 else 0.0
            } ?: 0.0

            val lastMinSentiment = buyDiffPercent - sellDiffPercent

            val buyStrengthPercent = if (first.buyQty != 0) {
                ((buyQty - first.buyQty).toDouble() / first.buyQty) * 100
            } else 0.0

            val sellStrengthPercent = if (first.sellQty != 0) {
                ((sellQty - first.sellQty).toDouble() / first.sellQty) * 100
            } else 0.0

            val overAllSentiment = buyStrengthPercent - sellStrengthPercent

            val currentOption = OptionsEntity(
                timestamp = timestamp,
                name = name,
                ltp = ltp,
                buyQty = buyQty,
                sellQty = sellQty,
                volTraded = volTraded,
                buyDiffPercent = buyDiffPercent,
                sellDiffPercent = sellDiffPercent,
                lastMinSentiment = lastMinSentiment,
                buyStrengthPercent = buyStrengthPercent,
                sellStrengthPercent = sellStrengthPercent,
                overAllSentiment = overAllSentiment,
                oiQty = oiQty,
                oiChange = oiChange

            )

            previousOptionMap[name] = currentOption
            optionsList.add(currentOption)
        }

        return optionsList
    }


    private suspend fun saveToDatabase(optionsList: List<OptionsEntity>) {
        withContext(Dispatchers.IO) {
            for (option in optionsList) {
                optionRepository.insertOption(option)
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
