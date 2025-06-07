package com.example.nifty50ops.special_features

import android.content.Context
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.MarketInsightEntity
import com.example.nifty50ops.network.generateContent
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import com.example.nifty50ops.utils.roundTo2DecimalPlaces
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sign

object MarketThresholds {
    const val bullishMomentumThreshold = 0.0
    const val bearishMomentumThreshold = 0.0
    const val highOiBuildupThreshold = 1.0
    const val strongBuyDominanceThreshold = 1.5
    const val strongSellDominanceThreshold = -1.5
    const val reversalTolerance = 0.0 // simply checks for sign change
    const val breakoutMoveThreshold = 2.0
    const val oiDivergenceThreshold = 0.0 // detects opposite signs
}

object MarketOverview {

    suspend fun generateMarketReviewSummary(context: Context): MarketInsightEntity {
        val insightsEntity = withContext(Dispatchers.IO) {
            val database = MarketDatabase.getDatabase(context)
            val dao = database.marketDao()
            val repository = MarketRepository(dao)
            val stockRepository = StockRepository(dao)
            val optionRepository = OptionsRepository(dao)

            // Collect snapshot
            val stockList = stockRepository.getLastMinStocks().first()
            val optionList = optionRepository.getLastMinOptions().first()
            val sentiment = repository.getLastSentimentSummary().first()
            val stockSummaryEntity = repository.getLatestStockSummary().first()
            val optionsSummaryEntity = repository.getLatestOptionsSummary().first()

            // Top stock & option
            val topStock = stockList.maxByOrNull {
                abs(it.buyDiffPercent) + abs(it.sellDiffPercent)
            }

            val topOption = optionList.maxByOrNull {
                abs(it.buyDiffPercent) + abs(it.sellDiffPercent)
            }

            // Top 5 fluctuating stocks/options
            val top5Stocks = stockList.sortedByDescending {
                maxOf(abs(it.buyDiffPercent), abs(it.sellDiffPercent))
            }.take(5)

            val top5Options = optionList.sortedByDescending {
                maxOf(
                    maxOf(abs(it.buyDiffPercent), abs(it.sellDiffPercent)),
                    abs(it.overAllOIChange)
                )
            }.take(5)

            // Stock Group Summary
            val stockSummary = """
            📊 Stock Group Summary
            • Time: ${stockSummaryEntity.lastUpdated}
            • LTP: ${"%.1f".format(stockSummaryEntity.ltp)}
            • Buy Avg: ${"%.1f".format(stockSummaryEntity.buyAvg)}% | Sell Avg: ${
                "%.1f".format(
                    stockSummaryEntity.sellAvg
                )
            }%
            • Buy Strength: ${"%.1f".format(stockSummaryEntity.stockBuyStr)}% | Sell Strength: ${
                "%.1f".format(
                    stockSummaryEntity.stockSellStr
                )
            }%
            • Overall Sentiment: ${"%.1f".format(stockSummaryEntity.overAllSentiment)}%
        """.trimIndent()

            // Option Group Summary
            val optionSummary = """
            📉 Option Group Summary
            • Time: ${optionsSummaryEntity.lastUpdated}
            • LTP: ${"%.1f".format(optionsSummaryEntity.ltp)}
            • Volume Traded: ${optionsSummaryEntity.volumeTraded}
            • Buy Avg: ${"%.1f".format(optionsSummaryEntity.buyAvg)}% | Sell Avg: ${
                "%.1f".format(
                    optionsSummaryEntity.sellAvg
                )
            }%
            • Buy Strength: ${"%.1f".format(optionsSummaryEntity.optionsBuyStr)}% | Sell Strength: ${
                "%.1f".format(
                    optionsSummaryEntity.optionsSellStr
                )
            }%
            • Overall Sentiment: ${"%.1f".format(optionsSummaryEntity.overAllSentiment)}%
            • OI Qty: ${optionsSummaryEntity.oiQty}
            • OI Change: ${"%.1f".format(optionsSummaryEntity.oiChange)}%
            • 1Min OI Change: ${"%.1f".format(optionsSummaryEntity.lastMinOIChange)}%
            • Overall OI Change: ${"%.1f".format(optionsSummaryEntity.overAllOIChange)}%
        """.trimIndent()

            // Helper function to get bullish/bearish/neutral label
            fun getSentimentLabel(value: Double): String =
                when {
                    value > 0.5 -> "Bullish"
                    value < -0.5 -> "Bearish"
                    else -> "Neutral"
                }

            // Build sentiment summary
            val sentimentSummary = buildString {
                appendLine("🧠 Sentiment Summary")
                appendLine("• Point Diff: ${sentiment.first().pointsChanged}")
                appendLine(
                    "• Stock Overall: ${sentiment.first().stockOverAllChange.roundTo2DecimalPlaces()} (${
                        getSentimentLabel(
                            sentiment.first().stockOverAllChange
                        )
                    })"
                )
                appendLine(
                    "• Stock 1Min: ${sentiment.first().stock1MinChange.roundTo2DecimalPlaces()} (${
                        getSentimentLabel(
                            sentiment.first().stock1MinChange
                        )
                    })"
                )
                appendLine(
                    "• Option Overall: ${sentiment.first().optionOverAllChange.roundTo2DecimalPlaces()} (${
                        getSentimentLabel(
                            sentiment.first().optionOverAllChange
                        )
                    })"
                )
                appendLine(
                    "• Option 1Min: ${sentiment.first().option1MinChange.roundTo2DecimalPlaces()} (${
                        getSentimentLabel(
                            sentiment.first().option1MinChange
                        )
                    })"
                )
                appendLine(
                    "• OI Overall: ${sentiment.first().oiOverAllChange.roundTo2DecimalPlaces()} (${
                        getSentimentLabel(
                            sentiment.first().oiOverAllChange
                        )
                    })"
                )
                appendLine(
                    "• OI 1Min: ${sentiment.first().oi1MinChange.roundTo2DecimalPlaces()} (${
                        getSentimentLabel(
                            sentiment.first().oi1MinChange
                        )
                    })"
                )

                val combinedMomentum =
                    sentiment.first().stock1MinChange + sentiment.first().option1MinChange
                appendLine(
                    "• Combined Momentum (1Min): ${"%.2f".format(combinedMomentum)} (${
                        getSentimentLabel(
                            combinedMomentum
                        )
                    })"
                )

                if (kotlin.math.abs(combinedMomentum) > 2.0) {
                    appendLine("⚡ Strong momentum detected in last minute.")
                }
                if (sentiment.first().oi1MinChange > 1.0) {
                    appendLine("⚠️ Significant OI buildup in last minute.")
                } else if (sentiment.first().oi1MinChange < -1.0) {
                    appendLine("⚠️ Significant OI drop in last minute.")
                }

                appendLine("• Last Updated: ${sentiment.first().lastUpdated}")
            }

            // Top 5 Fluctuations text
            val top5StockFluctuations = top5Stocks.joinToString("\n") {
                "→ ${it.name}: Buy=${"%.1f".format(it.buyDiffPercent)}%, Sell=${"%.1f".format(it.sellDiffPercent)}%"
            }

            val top5OptionFluctuations = top5Options.joinToString("\n") {
                "→ ${it.name}: Buy=${"%.1f".format(it.buyDiffPercent)}%, Sell=${"%.1f".format(it.sellDiffPercent)}%"
            }

            // Trading Hints
            val tradingHints = buildString {
                val stock1Min = sentiment.first().stock1MinChange
                val option1Min = sentiment.first().option1MinChange
                val stockOverAll = sentiment.first().stockOverAllChange
                val optionOverAll = sentiment.first().optionOverAllChange
                val oi1Min = sentiment.first().oi1MinChange

                if (stock1Min > MarketThresholds.bullishMomentumThreshold && option1Min > MarketThresholds.bullishMomentumThreshold) {
                    append("📈 Bullish momentum building.\n")
                } else if (stock1Min < MarketThresholds.bearishMomentumThreshold && option1Min < MarketThresholds.bearishMomentumThreshold) {
                    append("📉 Bearish momentum seen.\n")
                } else {
                    append("🔄 Mixed signals, trade cautiously.\n")
                }

                if (oi1Min > MarketThresholds.highOiBuildupThreshold) {
                    append("⚠️ High OI buildup — watch for breakout/reversal.\n")
                }

                if (stockOverAll > MarketThresholds.strongBuyDominanceThreshold && optionOverAll > MarketThresholds.strongBuyDominanceThreshold) {
                    append("🟢 Strong buying dominance across Stocks & Options — bias long.\n")
                } else if (stockOverAll < MarketThresholds.strongSellDominanceThreshold && optionOverAll < MarketThresholds.strongSellDominanceThreshold) {
                    append("🔴 Strong selling dominance across Stocks & Options — bias short.\n")
                }

                if (stock1Min.sign != stockOverAll.sign) {
                    append("⚠️ Possible Stock reversal building — watch carefully.\n")
                }
                if (option1Min.sign != optionOverAll.sign) {
                    append("⚠️ Possible Option reversal building — monitor for traps.\n")
                }

                if (abs(stock1Min) > MarketThresholds.breakoutMoveThreshold && abs(option1Min) > MarketThresholds.breakoutMoveThreshold) {
                    append("🚀 Sudden strong move detected — possible breakout underway.\n")
                }

                if (option1Min > MarketThresholds.oiDivergenceThreshold && oi1Min < MarketThresholds.oiDivergenceThreshold) {
                    append("⚠️ Option price rising but OI dropping — potential bull trap.\n")
                } else if (option1Min < MarketThresholds.oiDivergenceThreshold && oi1Min < MarketThresholds.oiDivergenceThreshold) {
                    append("⚠️ Option price falling with falling OI — possible bear trap.\n")
                }
            }

            // Insert MarketInsightEntity
            val insightsEntity = MarketInsightEntity(
                timestamp = sentiment.first().lastUpdated,
                name = "NIFTY50",
                ltp = sentiment.first().ltp,
                pointsChanged = sentiment.first().pointsChanged,
                intervalMinutes = "1Min",
                stockSummary = stockSummary,
                optionSummary = optionSummary,
                sentimentSummary = sentimentSummary,
                top5StockFluctuations = top5StockFluctuations,
                top5OptionFluctuations = top5OptionFluctuations,
                tradingHints = tradingHints
            )
            dao.insertMarketInsight(insightsEntity)
            insightsEntity
        }
        return insightsEntity
    }

    suspend fun generateAggregatedMarketInsight(
        context: Context,
        aggregator: StockOptionsAggregator,
        repository: MarketRepository,
        intervalInMin: Int
    ): MarketInsightEntity {

        val dao = MarketDatabase.getDatabase(context).marketDao()

        val stockSummaries = aggregator.getStockSummaryForIntervals(intervalInMin, repository)
        val optionSummaries = aggregator.getOptionsSummaryForIntervals(intervalInMin, repository)
        val sentimentSummary = repository.getLastSentimentSummary().first()

        val latestStockSummary = stockSummaries.lastOrNull()
            ?: throw IllegalStateException("No stock summary found for interval $intervalInMin")

        val latestOptionSummary = optionSummaries.lastOrNull()
            ?: throw IllegalStateException("No option summary found for interval $intervalInMin")

        // Top 5 Fluctuations
        val stockRepo = StockRepository(dao)
        val optionRepo = OptionsRepository(dao)
        val stockList = aggregator.getStocksForIntervals(intervalInMin, stockRepo)
        val optionList = aggregator.getOptionsForIntervals(intervalInMin, optionRepo)

        val top5Stocks = stockList.sortedByDescending {
            maxOf(abs(it.buyDiffPercent), abs(it.sellDiffPercent))
        }.take(5)

        val top5Options = optionList.sortedByDescending {
            maxOf(
                maxOf(abs(it.buyDiffPercent), abs(it.sellDiffPercent)),
                abs(it.overAllOIChange)
            )
        }.take(5)

        val top5StockFluctuations = top5Stocks.joinToString("\n") {
            "→ ${it.name}: Buy=${"%.1f".format(it.buyDiffPercent)}%, Sell=${"%.1f".format(it.sellDiffPercent)}%"
        }

        val top5OptionFluctuations = top5Options.joinToString("\n") {
            "→ ${it.name}: Buy=${"%.1f".format(it.buyDiffPercent)}%, Sell=${"%.1f".format(it.sellDiffPercent)}%"
        }

        // Helper
        fun getSentimentLabel(value: Double): String = when {
            value > 0.5 -> "Bullish"
            value < -0.5 -> "Bearish"
            else -> "Neutral"
        }

        // Build Sentiment Summary
        val sentimentSummaryText = buildString {
            appendLine("🧠 Sentiment Summary (${intervalInMin}Min)")
            appendLine("• Point Diff: ${sentimentSummary.first().pointsChanged}")
            appendLine(
                "• Stock Overall: ${sentimentSummary.first().stockOverAllChange.roundTo2DecimalPlaces()} (${
                    getSentimentLabel(sentimentSummary.first().stockOverAllChange)
                })"
            )
            appendLine(
                "• Stock 1Min: ${sentimentSummary.first().stock1MinChange.roundTo2DecimalPlaces()} (${
                    getSentimentLabel(sentimentSummary.first().stock1MinChange)
                })"
            )
            appendLine(
                "• Option Overall: ${sentimentSummary.first().optionOverAllChange.roundTo2DecimalPlaces()} (${
                    getSentimentLabel(sentimentSummary.first().optionOverAllChange)
                })"
            )
            appendLine(
                "• Option 1Min: ${sentimentSummary.first().option1MinChange.roundTo2DecimalPlaces()} (${
                    getSentimentLabel(sentimentSummary.first().option1MinChange)
                })"
            )
            appendLine(
                "• OI Overall: ${sentimentSummary.first().oiOverAllChange.roundTo2DecimalPlaces()} (${
                    getSentimentLabel(sentimentSummary.first().oiOverAllChange)
                })"
            )
            appendLine(
                "• OI 1Min: ${sentimentSummary.first().oi1MinChange.roundTo2DecimalPlaces()} (${
                    getSentimentLabel(sentimentSummary.first().oi1MinChange)
                })"
            )
            appendLine("• Last Updated: ${sentimentSummary.first().lastUpdated}")
        }

        // Trading Hints
        val tradingHints = buildString {
            val stockOverAll = sentimentSummary.first().stockOverAllChange
            val optionOverAll = sentimentSummary.first().optionOverAllChange
            val oiOverAll = sentimentSummary.first().oiOverAllChange

            if (stockOverAll > MarketThresholds.strongBuyDominanceThreshold && optionOverAll > MarketThresholds.strongBuyDominanceThreshold) {
                append("🟢 Strong buying dominance across Stocks & Options — bias long.\n")
            } else if (stockOverAll < MarketThresholds.strongSellDominanceThreshold && optionOverAll < MarketThresholds.strongSellDominanceThreshold) {
                append("🔴 Strong selling dominance across Stocks & Options — bias short.\n")
            } else {
                append("🔄 Mixed signals, trade cautiously.\n")
            }

            if (oiOverAll > MarketThresholds.highOiBuildupThreshold) {
                append("⚠️ High OI buildup — watch for breakout/reversal.\n")
            } else if (oiOverAll < -MarketThresholds.highOiBuildupThreshold) {
                append("⚠️ Significant OI drop — watch carefully.\n")
            }
        }

        // Stock Group Summary
        val stockSummary = """
    📊 Stocks Summary (${intervalInMin}Min)
    • Time: ${latestStockSummary.lastUpdated}
    • LTP: ${sentimentSummary.first().ltp}
    • Buy Avg: ${"%.1f".format(latestStockSummary.buyAvg)}% | Sell Avg: ${
            "%.1f".format(latestStockSummary.sellAvg)
        }%
    • Buy Strength: ${"%.1f".format(latestStockSummary.stockBuyStr)}% | Sell Strength: ${
            "%.1f".format(latestStockSummary.stockSellStr)
        }%
    • Overall Sentiment: ${"%.1f".format(latestStockSummary.overAllSentiment)}
    """.trimIndent()

        // Option Group Summary
        val optionSummary = """
    📉 Options Summary (${intervalInMin}Min)
    • Time: ${latestOptionSummary.lastUpdated}
    • LTP: ${sentimentSummary.first().ltp}
    • Volume Traded: ${latestOptionSummary.volumeTraded}
    • Buy Avg: ${"%.1f".format(latestOptionSummary.buyAvg)}% | Sell Avg: ${
            "%.1f".format(latestOptionSummary.sellAvg)
        }%
    • Buy Strength: ${"%.1f".format(latestOptionSummary.optionsBuyStr)}% | Sell Strength: ${
            "%.1f".format(latestOptionSummary.optionsSellStr)
        }%
    • Overall Sentiment: ${"%.1f".format(latestOptionSummary.overAllSentiment)}
    • OI Qty: ${latestOptionSummary.oiQty}
    • OI Change: ${"%.1f".format(latestOptionSummary.oiChange)}%
    • 1Min OI Change: ${"%.1f".format(latestOptionSummary.lastMinOIChange)}%
    • Overall OI Change: ${"%.1f".format(latestOptionSummary.overAllOIChange)}%
    """.trimIndent()

        // Insert MarketInsightEntity
        val marketInsight = MarketInsightEntity(
            timestamp = sentimentSummary.first().lastUpdated,
            name = "NIFTY50",
            ltp = sentimentSummary.first().ltp,
            pointsChanged = sentimentSummary.first().pointsChanged,
            intervalMinutes = "${intervalInMin}Min",
            stockSummary = stockSummary,
            optionSummary = optionSummary,
            sentimentSummary = sentimentSummaryText,
            top5StockFluctuations = top5StockFluctuations,
            top5OptionFluctuations = top5OptionFluctuations,
            tradingHints = tradingHints
        )

        dao.insertMarketInsight(marketInsight)
        return marketInsight
    }


    // Separate public method to update Gemini Insights
    suspend fun updateGenAIInsights(context: Context, timestamp: String, prompt: String) {
        val database = MarketDatabase.getDatabase(context)
        val dao = database.marketDao()

        try {
            val genAIResult = generateContent(prompt)
            dao.updateGenAIInsights(timestamp, genAIResult)
        } catch (e: Exception) {
            e.printStackTrace()
            dao.updateGenAIInsights(timestamp, "⚠️ AI generation failed: ${e.message}")
        }
    }

    fun buildPromptForGemini(insight: MarketInsightEntity): String {
        return """
        You are an expert intraday scalper and market analyst focusing on NIFTY50 options.

The trader:
- Waits for the right entry within the next 5-10 minutes.
- Exits quickly to capture fast momentum moves.
- Is NOT looking for positional or long-term analysis — only immediate actionable insights.

Based on the current market snapshot below, provide a **concise, structured scalping analysis**.

⛔ Do not add extra explanation or lengthy market background.
✅ Focus only on **fast actionable signals** and **entry/exit hints** suitable for scalping within next 5-10 minutes.

Format the response in the following structure:

# Immediate Momentum
Direction (Up / Down / Sideways) with 1-line reasoning.

# Volatility & Unusual Activity
Mention any volatility spikes or sudden moves in stocks or options.

# Reversal / Breakout Alerts
Mention any sharp reversal signals or breakout levels to watch.

# Entry Triggers
List key price levels, option strikes, or signals to look for potential entry.

# Exit Cues & Warnings
Mention any quick exit signals or signs to avoid losses.

# Recommended Side
Recommend (Long / Short / Wait), with 1-line justification.


---

## Current Market Snapshot:
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

Please provide response strictly in the **structured format** mentioned above, suitable for a scalper deciding trades within next 5-10 minutes.
""".trimIndent()
    }


}

