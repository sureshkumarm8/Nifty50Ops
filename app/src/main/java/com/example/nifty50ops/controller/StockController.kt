package com.example.nifty50ops.controller

import android.content.Context
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.network.PayTMMoneyApiService
import com.example.nifty50ops.repository.StockRepository
import com.example.nifty50ops.utils.roundTo2DecimalPlaces
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StockController(private val stockRepository: StockRepository) {

    suspend fun fetchStockData(context: Context) {
        val prefs = securityIdToSymbol.keys.joinToString(",") { "NSE:$it:EQUITY" }
        PayTMMoneyApiService.fetchData(context, prefs)?.let { responseBody ->
            saveToDatabase(parseStockResponse(responseBody))
        }
    }

    // Define this globally or in your class
    private val previousStockMap = mutableMapOf<String, StockEntity>()
    private val firstStockMap = mutableMapOf<String, StockEntity>()

    private suspend fun parseStockResponse(response: String): List<StockEntity> {
        val stockList = mutableListOf<StockEntity>()
        val jsonObject = JSONObject(response)
        val dataArray: JSONArray = jsonObject.optJSONArray("data") ?: JSONArray()

        // Fetch latest stored stocks from DB (once)
        val latestStoredStocks = stockRepository.getLastMinStocks().firstOrNull() ?: emptyList()

        // Initialize previousStockMap from DB if not already populated
        for (stock in latestStoredStocks) {
            if (!previousStockMap.containsKey(stock.name)) {
                previousStockMap[stock.name] = stock
            }
        }

        if (firstStockMap.isEmpty()) {
            val firstMinuteStocks = stockRepository.getFirstMinuteStocks()
            for (stock in firstMinuteStocks) {
                firstStockMap[stock.name] = stock
            }
        }

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

            // Store the first value if not already stored
            val first = firstStockMap.getOrPut(name) {
                StockEntity(
                    timestamp = timestamp,
                    name = name,
                    ltp = ltp,
                    buyQty = buyQty,
                    sellQty = sellQty,
                    buyDiffPercent = 0.0,
                    sellDiffPercent = 0.0,
                    lastMinSentiment = 0.0,
                    buyStrengthPercent = 0.0,
                    sellStrengthPercent = 0.0,
                    overAllSentiment = 0.0
                )
            }

            val buyDiffPercent = previous?.let {
                if (it.buyQty != 0) ((buyQty - it.buyQty).toDouble() / it.buyQty) * 100 else 0.0
            } ?: 0.0

            val sellDiffPercent = previous?.let {
                if (it.sellQty != 0) ((sellQty - it.sellQty).toDouble() / it.sellQty) * 100 else 0.0
            } ?: 0.0

//            val lastMinMomentum2 = (previous?.buyDiffPercent ?: 0.0) - (previous?.sellDiffPercent ?: 0.0)
//            val lastMinMomentum = buyDiffPercent - sellDiffPercent

            val lastMinSentiment = buyDiffPercent - sellDiffPercent

            val buyStrengthPercent = if (first.buyQty != 0) {
                ((buyQty - first.buyQty).toDouble() / first.buyQty) * 100
            } else 0.0

            val sellStrengthPercent = if (first.sellQty != 0) {
                ((sellQty - first.sellQty).toDouble() / first.sellQty) * 100
            } else 0.0

            val overAllSentiment = buyStrengthPercent - sellStrengthPercent

            val currentStock = StockEntity(
                timestamp = timestamp,
                name = name,
                ltp = ltp,
                buyQty = buyQty,
                sellQty = sellQty,
                buyDiffPercent = buyDiffPercent.roundTo2DecimalPlaces(),
                sellDiffPercent = sellDiffPercent.roundTo2DecimalPlaces(),
                lastMinSentiment = lastMinSentiment.roundTo2DecimalPlaces(),
                buyStrengthPercent = buyStrengthPercent.roundTo2DecimalPlaces(),
                sellStrengthPercent = sellStrengthPercent.roundTo2DecimalPlaces(),
                overAllSentiment = overAllSentiment.roundTo2DecimalPlaces()
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
        1333 to "HDFCBANK", 4963 to "ICICIBANK", 2885 to "RELIANCE", 1594 to "INFY", 1660 to "ITC",
        11483 to "LT", 10604 to "BHARTIARTL", 11536 to "TCS", 5900 to "AXISBANK", 2031 to "M&M",
        3045 to "SBIN", 1922 to "KOTAKBANK", 1394 to "HINDUNILVR", 7229 to "HCLTECH", 3351 to "SUNPHARMA",
        317 to "BAJFINANCE", 10999 to "MARUTI", 3456 to "TATAMOTORS", 11630 to "NTPC", 3506 to "TITAN",
        14977 to "POWERGRID", 2475 to "ONGC", 11532 to "ULTRACEMCO", 11723 to "JSWSTEEL", 16669 to "BAJAJ-AUTO",
        3499 to "TATASTEEL", 16675 to "BAJAJFINSV", 13538 to "TECHM", 236 to "ASIANPAINT", 18143 to "JIOFIN",
        1232 to "GRASIM", 15083 to "ADANIPORTS", 25 to "ADANIENT", 20374 to "COALINDIA", 1363 to "HINDALCO",
        881 to "DRREDDY", 3787 to "WIPRO", 694 to "CIPLA", 17963 to "NESTLEIND", 157 to "APOLLOHOSP",
        910 to "EICHERMOT", 467 to "HDFCLIFE", 21808 to "SBILIFE", 5258 to "INDUSINDBK", 3432 to "TATACONSUM",
        1964 to "TRENT", 1348 to "HEROMOTOCO", 383 to "BEL", 4306 to "SHRIRAMFIN", 5097 to "ZOMATO")

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
