package com.example.nifty50ops.controller

import com.example.nifty50ops.model.StockData
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.repository.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class StockController(private val stockRepository: StockRepository) {
    private val client = OkHttpClient()

    suspend fun fetchStockData() {
        val securityChunks = securityIdToSymbol.keys.chunked(4)
        withContext(Dispatchers.IO) {
            for (chunk in securityChunks) {
                val prefs = chunk.joinToString(",") { "NSE:$it:EQUITY" }
                val apiUrl = "https://developer.paytmmoney.com/data/v1/price/live?mode=QUOTE&pref=$prefs"
                val jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJtZXJjaGFudCIsImlzcyI6InBheXRtbW9uZXkiLCJpZCI6MTQyMjM0MywiZXhwIjoxNzQzODc3Nzk5fQ.UYPhiVf4wh9wyEk9mjhUq3-eU8Ws7MQHIoRPi7jGkkI"

                val request = Request.Builder().url(apiUrl).addHeader("x-jwt-token", jwtToken).build()

                try {
                    val response = client.newCall(request).execute()
                    response.use {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            responseBody?.let { saveToDatabase(parseStockResponse(it)) }
                        }
                    }
                } catch (_: Exception) { }
            }
        }
    }

    // Already exists in your StockController.kt
    val previousStockMap = mutableMapOf<String, StockEntity>()

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

            val previous = previousStockMap[name]

            val buyDiffPercent = previous?.let {
                if (it.buyQty != 0) ((buyQty - it.buyQty).toDouble() / it.buyQty) * 100 else 0.0
            } ?: 0.0

            val sellDiffPercent = previous?.let {
                if (it.sellQty != 0) ((sellQty - it.sellQty).toDouble() / it.sellQty) * 100 else 0.0
            } ?: 0.0

            val currentStock = StockEntity(
                name = name,
                ltp = ltp,
                buyQty = buyQty,
                sellQty = sellQty,
                buyDiffPercent = buyDiffPercent,
                sellDiffPercent = sellDiffPercent
            )

            previousStockMap[name] = currentStock
            stockList.add(currentStock)
        }

        return stockList
    }


    private suspend fun saveToDatabase(stockList: List<StockEntity>) {
        for (stock in stockList) {
            stockRepository.insertStock(stock)
        }
    }

    private val securityIdToSymbol = mapOf(
            500696 to "HINDUNILVR", 500228 to "JSWSTEEL", 3456 to "TATAMOTORS", 540777 to "HDFCLIFE",
    540719 to "SBILIFE", 3351 to "SUNPHARMA", 11536 to "TCS", 3787 to "WIPRO",
    500114 to "TITAN", 532500 to "MARUTI", 13538 to "TECHM", 532187 to "INDUSINDBK",
    1922 to "KOTAKBANK", 5900 to "AXISBANK", 500251 to "TRENT", 25 to "ADANIENT",
    500300 to "GRASIM", 694 to "CIPLA", 500325 to "RELIANCE", 500312 to "ONGC",
    532538 to "ULTRACEMCO", 10604 to "BHARTIARTL", 500820 to "ASIANPAINT", 505200 to "EICHERMOT",
    532977 to "BAJAJ-AUTO", 500800 to "TATACONSUM", 500510 to "LT", 500440 to "HINDALCO",
    508869 to "APOLLOHOSP", 532555 to "NTPC", 532174 to "ICICIBANK", 1348 to "HEROMOTOCO",
    500049 to "BEL", 14977 to "POWERGRID", 1594 to "INFY", 500875 to "ITC",
    7229 to "HCLTECH", 3045 to "SBIN", 500520 to "M&M", 500034 to "BAJFINANCE",
    500180 to "HDFCBANK", 15083 to "ADANIPORTS", 533278 to "COALINDIA", 5097 to "ZOMATO",
    500470 to "TATASTEEL", 532978 to "BAJAJFINSV", 18143 to "JIOFIN", 500790 to "NESTLEIND",
    500124 to "DRREDDY", 4306 to "SHRIRAMFIN"
    )
}
