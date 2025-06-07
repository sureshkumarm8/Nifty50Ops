package com.example.nifty50ops.network

/**
 * You'll need a Firebase project and the `firebase-ai` dependency to run this
 * code. Learn how to set up your environment: https://firebase.google.com/docs/ai-logic/get-started
 */

import com.example.nifty50ops.model.MarketInsightEntity
import com.example.nifty50ops.utils.Constants
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig

public suspend fun generateContent(NIFTY50_ANALYSIS_PROMPT: String): String {

    val generationConfig = generationConfig {
        responseMimeType = "text/plain"
    }

    val model = Firebase.ai.generativeModel(
        modelName = "gemini-2.0-flash",
        generationConfig = generationConfig,

        )
    val message = content("user") {
        text(NIFTY50_ANALYSIS_PROMPT)
    }

    val chat = model.startChat()

    val response = chat.sendMessage(message)
//    println(response.text ?: "")
    return response.text ?: "";
}

fun buildPromptForGemini(insight: MarketInsightEntity): String {
    return """
${Constants.SYSTEM_ROLE_DESCRIPTION}

${Constants.RESPONSE_FORMAT}

${Constants.SNAPSHOT_HEADER}:
• Timestamp: ${insight.timestamp}
• Latest Price (LTP): ${insight.ltp}
• Points Changed: ${insight.pointsChanged}

--- Stock Summary ---
${insight.stockSummary}

--- Option Summary ---
${insight.optionSummary}

--- Sentiment Summary ---
${insight.sentimentSummary}

--- Top 5 Stock Fluctuations ---
${insight.top5StockFluctuations}

--- Top 5 Option Fluctuations ---
${insight.top5OptionFluctuations}

--- Trading Hints from App ---
${insight.tradingHints}

---

${Constants.SNAPSHOT_INSTRUCTION}
""".trimIndent()
}


