package com.example.nifty50ops.network

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object ApiService {

    private val client = OkHttpClient()

    suspend fun fetchData(context: Context, prefs: String): String? {
        val apiUrl = "https://developer.paytmmoney.com/data/v1/price/live?mode=FULL&pref=$prefs"
        val jwtToken = ""

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("x-jwt-token", jwtToken)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    println("✅ Success: fetched data" + response)
                    response.body?.string()
                } else {
                    println("❌ Failed: ${response.code} - ${response.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "❌ Failed to fetch: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    null
                }
            } catch (e: Exception) {
                println("⚠️ Exception: ${e.message}")
                null
            }
        }
    }

}
