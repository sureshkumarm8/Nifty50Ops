package com.example.nifty50ops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nifty50ops.ui.theme.Nifty50OpsTheme
import com.example.nifty50ops.view.StockScreen
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nifty50OpsTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                  StockScreen(modifier = Modifier.padding(innerPadding))
                    StockScreen(context = this)
//                }
            }
        }
    }
}


val securityIdToSymbol = mapOf(
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

@Composable
fun StockScreen(modifier: Modifier = Modifier) {
    var stockList by remember { mutableStateOf<List<StockData>>(emptyList()) }

    LaunchedEffect(Unit) {
        val client = OkHttpClient()
        val allSecurityIds = securityIdToSymbol.keys.toList()
        val chunkSize = 4 // API limit

        while (true) {
            val tempStockList = mutableListOf<StockData>()
            val securityChunks = allSecurityIds.chunked(chunkSize)

            for (chunk in securityChunks) {
                val prefs = chunk.joinToString(",") { "NSE:$it:EQUITY" }
                val apiUrl = "https://developer.paytmmoney.com/data/v1/price/live?mode=QUOTE&pref=$prefs"
                val jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJtZXJjaGFudCIsImlzcyI6InBheXRtbW9uZXkiLCJpZCI6MTQyMjA2MSwiZXhwIjoxNzQzNzkxMzk5fQ.f_mTToQnUZ08yFlMuBq2AU1yJ8NeNWWU5022j4PJ5Yk" // Replace with actual token

                withContext(Dispatchers.IO) { // Run in background thread
                    val request = Request.Builder()
                        .url(apiUrl)
                        .addHeader("x-jwt-token", jwtToken)
                        .build()

                    try {
                        val response = client.newCall(request).execute()
                        response.use {
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string()
                                responseBody?.let {
                                    tempStockList.addAll(parseStockResponse(it))
                                }
                            }
                        }
                    } catch (e: IOException) {
                        tempStockList.add(StockData("Error", 0.0, 0, 0))
                    }
                }

                delay(500) // Short delay between API calls
            }

            stockList = tempStockList
            delay(60 * 1000) // Fetch every minute
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Nifty 50 Stock Data", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(10.dp))

        // Table Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Name", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("LTP", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("Buy Qty", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("Sell Qty", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        }

        Divider()

        // Table Content
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(stockList) { stock ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stock.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text(stock.ltp.toString(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text(stock.buyQty.toString(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text(stock.sellQty.toString(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                }
                Divider()
            }
        }
    }
}

// Data class for stock
data class StockData(val name: String, val ltp: Double, val buyQty: Int, val sellQty: Int)

// Function to parse API response
fun parseStockResponse(response: String): List<StockData> {
    val stockList = mutableListOf<StockData>()
    try {
        val jsonObject = JSONObject(response)

        if (!jsonObject.has("data")) {
            return listOf(StockData("No Data", 0.0, 0, 0))
        }

        val dataArray = jsonObject.getJSONArray("data")

        for (i in 0 until dataArray.length()) {
            val stockObject = dataArray.getJSONObject(i)

            // Handle missing keys safely
            val securityId = stockObject.optInt("security_id", -1)
            if (securityId == -1) continue  // Skip if security_id is missing

            val name = securityIdToSymbol[securityId] ?: "Unknown"
            val ltp = stockObject.optDouble("last_price", 0.0)
            val buyQty = stockObject.optInt("total_buy_quantity", 0)
            val sellQty = stockObject.optInt("total_sell_quantity", 0)

            stockList.add(StockData(name, ltp, buyQty, sellQty))
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        return listOf(StockData("Parsing Error", 0.0, 0, 0))
    }
    return stockList
}

fun extractStockDetails(jsonResponse: String): String {
    return try {
        val jsonObject = JSONObject(jsonResponse)

        if (!jsonObject.has("data")) {
            return "Error: 'data' field missing"
        }

        val dataArray = jsonObject.getJSONArray("data")
        if (dataArray.length() == 0) {
            return "No stock data found"
        }

        val builder = StringBuilder()

        for (i in 0 until dataArray.length()) {
            val stock = dataArray.getJSONObject(i)
            val securityId = stock.optInt("security_id", 0)
            val symbol = securityIdToSymbol[securityId] ?: "UNKNOWN"
            val lastPrice = stock.optDouble("last_price", 0.0)
            val totalBuyQuantity = stock.optInt("total_buy_quantity", 0)
            val totalSellQuantity = stock.optInt("total_sell_quantity", 0)

            builder.appendLine("🔹 $symbol ($securityId)")
            builder.appendLine("   🔸 Last Price: $lastPrice")
            builder.appendLine("   🟢 Buy Qty: $totalBuyQuantity")
            builder.appendLine("   🔴 Sell Qty: $totalSellQuantity")
            builder.appendLine()
        }

        builder.toString().trim()
    } catch (e: Exception) {
        "Error parsing data: ${e.message}"
    }
}

@Preview(showBackground = true)
@Composable
fun StockScreenPreview() {
    Nifty50OpsTheme {
        StockScreen()
    }
}

