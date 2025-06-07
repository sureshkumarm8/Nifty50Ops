package com.example.nifty50ops.special_features

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.Header

// Request model
data class GeminiRequest(
    val prompt: String,
    val max_tokens: Int = 500,
    val temperature: Double = 0.7,
    val top_p: Double = 1.0,
    val n: Int = 1,
    val stop: List<String>? = null
)

// Response model (adjust based on Gemini AI API response)
data class GeminiChoice(val text: String)

data class GeminiResponse(
    val id: String,
    val choices: List<GeminiChoice>
)

interface GeminiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")  // Use actual Gemini AI endpoint path
    suspend fun getMarketAnalysis(
        @Header("Authorization") authHeader: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://api.gemini.com/" // replace with Gemini AI base url

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}

suspend fun callGeminiAI(prompt: String): String {
    // Replace with your actual API key
    val apiKey = "AIzaSyAMPFwY9RhwxChrRjR0ZMDOvcKgmC4rPLc"

    val request = GeminiRequest(prompt = prompt)

    val response = GeminiApiClient.apiService.getMarketAnalysis(authHeader = apiKey, request = request)

    // Assuming the response contains a list of choices and we want the first one's text
    return response.choices.firstOrNull()?.text?.trim() ?: "No response from Gemini AI"
}
