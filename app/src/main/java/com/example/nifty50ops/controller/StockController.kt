package com.example.nifty50ops.controller

import android.content.Context
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.network.ApiService
import com.example.nifty50ops.repository.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StockController(private val stockRepository: StockRepository) {

    suspend fun fetchStockData(context: Context) {
        val prefs = securityIdToSymbol.keys.joinToString(",") { "NSE:$it:EQUITY" }
        ApiService.fetchData(context, prefs)?.let { responseBody ->
            saveToDatabase(parseStockResponse(responseBody))
        }
    }

    // Define this globally or in your class
    private val previousStockMap = mutableMapOf<String, StockEntity>()
    private val firstStockMap = mutableMapOf<String, StockEntity>()

    private fun parseStockResponse(response: String): List<StockEntity> {
        val stockList = mutableListOf<StockEntity>()
        val jsonObject = JSONObject(response)
        val dataArray: JSONArray = jsonObject.optJSONArray("data") ?: JSONArray()

        for (i in 0 until dataArray.length()) {
            val stockObject = dataArray.getJSONObject(i)
            val securityId = stockObject.optInt("security_id", -1)
            if (securityId == -1) continue

            val name = securityIdToSymbol[securityId] ?: "Unknown"
            val ltp = stockObject.optDouble("last_price", 0.0)
            val buyQty = stockObject.optInt("total_buy_quantity", 0)
            val sellQty = stockObject.optInt("total_sell_quantity", 0)
            val lastTradeTime = stockObject.optLong("last_trade_time", 0)
            val timestamp = formatToHourMinute(lastTradeTime)

            val previous = previousStockMap[name]

            val buyDiffPercent = previous?.let {
                if (it.buyQty != 0) ((buyQty - it.buyQty).toDouble() / it.buyQty) * 100 else 0.0
            } ?: 0.0

            val sellDiffPercent = previous?.let {
                if (it.sellQty != 0) ((sellQty - it.sellQty).toDouble() / it.sellQty) * 100 else 0.0
            } ?: 0.0

            // Store the first value if not already stored
            val first = firstStockMap.getOrPut(name) {
                StockEntity(
                    name = name,
                    ltp = ltp,
                    buyQty = buyQty,
                    sellQty = sellQty,
                    buyDiffPercent = 0.0,
                    sellDiffPercent = 0.0,
                    buyStrengthPercent = 0.0,
                    sellStrengthPercent = 0.0,
                    timestamp = timestamp
                )
            }

            val buyStrengthPercent = if (first.buyQty != 0) {
                ((buyQty - first.buyQty).toDouble() / first.buyQty) * 100
            } else 0.0

            val sellStrengthPercent = if (first.sellQty != 0) {
                ((sellQty - first.sellQty).toDouble() / first.sellQty) * 100
            } else 0.0

            val currentStock = StockEntity(
                name = name,
                ltp = ltp,
                buyQty = buyQty,
                sellQty = sellQty,
                buyDiffPercent = buyDiffPercent,
                sellDiffPercent = sellDiffPercent,
                buyStrengthPercent = buyStrengthPercent,
                sellStrengthPercent = sellStrengthPercent,
                timestamp = timestamp
            )

            previousStockMap[name] = currentStock
            stockList.add(currentStock)
        }

        return stockList
    }


    private suspend fun saveToDatabase(stockEntities: List<StockEntity>) {
        withContext(Dispatchers.IO) {
            for (stock in stockEntities) {
                stockRepository.insertStock(stock)
            }
        }
    }

    private val securityIdToSymbol = mapOf(
        500180 to "HDFCBANK", 532174 to "ICICIBANK", 500325 to "RELIANCE", 1594 to "INFY", 500875 to "ITC",
        500510 to "LT", 10604 to "BHARTIARTL", 11536 to "TCS", 5900 to "AXISBANK", 500520 to "M&M",
        3045 to "SBIN", 1922 to "KOTAKBANK", 500696 to "HINDUNILVR", 7229 to "HCLTECH", 3351 to "SUNPHARMA",
        500034 to "BAJFINANCE", 532500 to "MARUTI", 3456 to "TATAMOTORS", 532555 to "NTPC", 500114 to "TITAN",
        14977 to "POWERGRID", 500312 to "ONGC", 532538 to "ULTRACEMCO", 500228 to "JSWSTEEL", 532977 to "BAJAJ-AUTO",
        500470 to "TATASTEEL", 532978 to "BAJAJFINSV", 13538 to "TECHM", 500820 to "ASIANPAINT", 18143 to "JIOFIN",
        500300 to "GRASIM", 15083 to "ADANIPORTS", 25 to "ADANIENT", 533278 to "COALINDIA", 500440 to "HINDALCO",
        500124 to "DRREDDY", 3787 to "WIPRO", 694 to "CIPLA", 500790 to "NESTLEIND", 508869 to "APOLLOHOSP",
        505200 to "EICHERMOT", 540777 to "HDFCLIFE", 540719 to "SBILIFE", 532187 to "INDUSINDBK", 500800 to "TATACONSUM",
        500251 to "TRENT", 1348 to "HEROMOTOCO", 500049 to "BEL", 4306 to "SHRIRAMFIN", 5097 to "ZOMATO")

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
