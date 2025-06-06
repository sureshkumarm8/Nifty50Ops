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
    suspend fun generateMarketReviewSummary(context: Context) {
        withContext(Dispatchers.IO) {
            val database = MarketDatabase.getDatabase(context)
            val dao = database.marketDao()
            val repository = MarketRepository(dao)
            val stockRepository = StockRepository(dao)
            val optionRepository = OptionsRepository(dao)

            // Collect snapshot
            val stockList = stockRepository.getLastMinStocks().first()
            val optionList = optionRepository.getLastMinOptions().first()
            val sentiment = repository.getLastSentimentSummary().first()

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



            // Stock Summary
            val stockSummary = """
            📊 Stocks Summary
            • Time: ${sentiment.first().lastUpdated}
            • Top Stock: ${topStock?.name ?: "-"} | Buy: ${"%.1f".format(topStock?.buyDiffPercent ?: 0.0)}% | Sell: ${
                "%.1f".format(
                    topStock?.sellDiffPercent ?: 0.0
                )
            }
            • Buy Str: ${"%.1f".format(topStock?.buyStrengthPercent ?: 0.0)}% | Sell Str: ${
                "%.1f".format(
                    topStock?.sellStrengthPercent ?: 0.0
                )
            }%
        """.trimIndent()

            // Option Summary
            val optionSummary = """
            📉 Options Summary
            • Top Option: ${topOption?.name ?: "-"} | Buy: ${"%.1f".format(topOption?.buyDiffPercent ?: 0.0)}% | Sell: ${
                "%.1f".format(
                    topOption?.sellDiffPercent ?: 0.0
                )
            }
            • Buy Str: ${"%.1f".format(topOption?.buyStrengthPercent ?: 0.0)}% | Sell Str: ${
                "%.1f".format(
                    topOption?.sellStrengthPercent ?: 0.0
                )
            }%
            • OI Chg: ${"%.1f".format(topOption?.overAllOIChange ?: 0.0)}% | Last Min OI: ${
                "%.1f".format(
                    topOption?.lastMinOIChange ?: 0.0
                )
            }%
        """.trimIndent()

            // Helper function to get bullish/bearish/neutral label
            fun getSentimentLabel(value: Double): String =
                when {
                    value > 0.5 -> "Bullish"
                    value < -0.5 -> "Bearish"
                    else -> "Neutral"
                }

        // Build sentiment summary with extra insights
            val sentimentSummary = buildString {
                appendLine("🧠 Sentiment Summary")
                appendLine("• Point Diff: ${sentiment.first().pointsChanged}")
                appendLine("• Stock Overall: ${sentiment.first().stockOverAllChange.roundTo2DecimalPlaces()} (${getSentimentLabel(sentiment.first().stockOverAllChange)})")
                appendLine("• Stock 1Min: ${sentiment.first().stock1MinChange.roundTo2DecimalPlaces()} (${getSentimentLabel(sentiment.first().stock1MinChange)})")
                appendLine("• Option Overall: ${sentiment.first().optionOverAllChange.roundTo2DecimalPlaces()} (${getSentimentLabel(sentiment.first().optionOverAllChange)})")
                appendLine("• Option 1Min: ${sentiment.first().option1MinChange.roundTo2DecimalPlaces()} (${getSentimentLabel(sentiment.first().option1MinChange)})")
                appendLine("• OI Overall: ${sentiment.first().oiOverAllChange.roundTo2DecimalPlaces()} (${getSentimentLabel(sentiment.first().oiOverAllChange)})")
                appendLine("• OI 1Min: ${sentiment.first().oi1MinChange.roundTo2DecimalPlaces()} (${getSentimentLabel(sentiment.first().oi1MinChange)})")

                // Combined momentum
                val combinedMomentum = sentiment.first().stock1MinChange + sentiment.first().option1MinChange
                appendLine("• Combined Momentum (1Min): ${"%.2f".format(combinedMomentum)} (${getSentimentLabel(combinedMomentum)})")

                // Highlight strong momentum
                if (kotlin.math.abs(combinedMomentum) > 2.0) {
                    appendLine("⚡ Strong momentum detected in last minute.")
                }

                // OI buildup or drop
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

// Trading Hints — enhanced for intraday, threshold driven
            val tradingHints = buildString {
                val stock1Min = sentiment.first().stock1MinChange
                val option1Min = sentiment.first().option1MinChange
                val stockOverAll = sentiment.first().stockOverAllChange
                val optionOverAll = sentiment.first().optionOverAllChange
                val oi1Min = sentiment.first().oi1MinChange

                // Basic momentum
                if (stock1Min > MarketThresholds.bullishMomentumThreshold && option1Min > MarketThresholds.bullishMomentumThreshold) {
                    append("📈 Bullish momentum building.\n")
                } else if (stock1Min < MarketThresholds.bearishMomentumThreshold && option1Min < MarketThresholds.bearishMomentumThreshold) {
                    append("📉 Bearish momentum seen.\n")
                } else {
                    append("🔄 Mixed signals, trade cautiously.\n")
                }

                // High OI buildup
                if (oi1Min > MarketThresholds.highOiBuildupThreshold) {
                    append("⚠️ High OI buildup — watch for breakout/reversal.\n")
                }

                // Buy/Sell dominance
                if (stockOverAll > MarketThresholds.strongBuyDominanceThreshold && optionOverAll > MarketThresholds.strongBuyDominanceThreshold) {
                    append("🟢 Strong buying dominance across Stocks & Options — bias long.\n")
                } else if (stockOverAll < MarketThresholds.strongSellDominanceThreshold && optionOverAll < MarketThresholds.strongSellDominanceThreshold) {
                    append("🔴 Strong selling dominance across Stocks & Options — bias short.\n")
                }

                // Reversal warning
                if (stock1Min.sign != stockOverAll.sign) {
                    append("⚠️ Possible Stock reversal building — watch carefully.\n")
                }
                if (option1Min.sign != optionOverAll.sign) {
                    append("⚠️ Possible Option reversal building — monitor for traps.\n")
                }

                // Consolidation breakout alert
                if (abs(stock1Min) > MarketThresholds.breakoutMoveThreshold && abs(option1Min) > MarketThresholds.breakoutMoveThreshold) {
                    append("🚀 Sudden strong move detected — possible breakout underway.\n")
                }

                // OI + Price divergence alert
                if (option1Min > MarketThresholds.oiDivergenceThreshold && oi1Min < MarketThresholds.oiDivergenceThreshold) {
                    append("⚠️ Option price rising but OI dropping — potential bull trap.\n")
                } else if (option1Min < MarketThresholds.oiDivergenceThreshold && oi1Min < MarketThresholds.oiDivergenceThreshold) {
                    append("⚠️ Option price falling with falling OI — possible bear trap.\n")
                }
            }


            // Log full summary (optional)
            val fullSummary = """
            $stockSummary
            
            $optionSummary
            
            $sentimentSummary
            
            Top 5 Stocks:
            $top5StockFluctuations
            
            Top 5 Options:
            $top5OptionFluctuations
            
            Trading Hints:
            $tradingHints
        """.trimIndent()

            println("Generated Market Insights:\n$fullSummary")

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
        }
    }

    suspend fun generateAggregatedMarketInsight(
        context: Context,
        aggregator: StockOptionsAggregator,
        repository: MarketRepository,
        intervalInMin: Int
    ) {
        val dao = MarketDatabase.getDatabase(context).marketDao()

        // Get grouped summaries
        val stockSummaries = aggregator.getStockSummaryForIntervals(intervalInMin, repository)
        val optionSummaries = aggregator.getOptionsSummaryForIntervals(intervalInMin, repository)
        val sentimentSummary = repository.getLastSentimentSummary().first()

        // Get last group summary
        val latestStockSummary = stockSummaries.lastOrNull() ?: return
        val latestOptionSummary = optionSummaries.lastOrNull() ?: return

        val stockSummary = """
        📊 Stocks Summary (${intervalInMin}Min)
        • Time: ${latestStockSummary.lastUpdated}
        • Sentiment: ${"%.1f".format(latestStockSummary.overAllSentiment)}
        • Buy Str: ${"%.1f".format(latestStockSummary.stockBuyStr)}% | Sell Str: ${"%.1f".format(latestStockSummary.stockSellStr)}%
    """.trimIndent()

        val optionSummary = """
        📉 Options Summary (${intervalInMin}Min)
        • Time: ${latestOptionSummary.lastUpdated}
        • Sentiment: ${"%.1f".format(latestOptionSummary.overAllSentiment)}
        • Buy Str: ${"%.1f".format(latestOptionSummary.optionsBuyStr)}% | Sell Str: ${"%.1f".format(latestOptionSummary.optionsSellStr)}%
        • OI Chg: ${"%.1f".format(latestOptionSummary.overAllOIChange)}%
    """.trimIndent()

        val sentimentSummaryText = """
        🧠 Sentiment Summary (${intervalInMin}Min)
        • Point Diff: ${sentimentSummary.first().pointsChanged}
        • Stock: ${sentimentSummary.first().stockOverAllChange}, 1Min: ${sentimentSummary.first().stock1MinChange}
        • Option: ${sentimentSummary.first().optionOverAllChange}, 1Min: ${sentimentSummary.first().option1MinChange}
        • OI: ${sentimentSummary.first().oiOverAllChange}, 1Min OI: ${sentimentSummary.first().oi1MinChange}
    """.trimIndent()

        val tradingHints = buildString {
            if (sentimentSummary.first().stock1MinChange > 0 && sentimentSummary.first().option1MinChange > 0) {
                append("📈 Bullish momentum building.\n")
            } else if (sentimentSummary.first().stock1MinChange < 0 && sentimentSummary.first().option1MinChange < 0) {
                append("📉 Bearish momentum seen.\n")
            } else {
                append("🔄 Mixed signals, trade cautiously.\n")
            }

            if (sentimentSummary.first().oi1MinChange > 1.0) {
                append("⚠️ High OI buildup — watch for breakout/reversal.")
            }
        }

        val insightsEntity = MarketInsightEntity(
            timestamp = latestStockSummary.lastUpdated,
            name = "NIFTY50",
            ltp = sentimentSummary.first().ltp,
            pointsChanged = sentimentSummary.first().pointsChanged,
            intervalMinutes = "${intervalInMin}Min",
            stockSummary = stockSummary,
            optionSummary = optionSummary,
            sentimentSummary = sentimentSummaryText,
            top5StockFluctuations = "", // Optional — if needed, you can generate top5 again using same grouping logic
            top5OptionFluctuations = "", // Optional
            tradingHints = tradingHints
        )

        dao.insertMarketInsight(insightsEntity)

        println("✅ Inserted MarketInsight for ${intervalInMin}Min")
    }

    suspend fun debugData(context: Context) {
        val dao = MarketDatabase.getDatabase(context).marketDao()
        val repository = MarketRepository(dao)

        repository.getMarketInsightsByInterval("10Min").collect { list ->
            println("marketInsightsByInterval update: $list")
        }
    }

}
