package com.example.nifty50ops.utils

import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.StockSummaryEntity
import com.example.nifty50ops.repository.MarketRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.collections.component1
import kotlin.collections.component2

class StockOptionsAggregator {

    suspend fun getStockSummaryForIntervals(
        intervalInMin: Int,
        repository: MarketRepository
    ): List<StockSummaryEntity> {
        val allSummaries = repository.getAllStockSummary().first()
        return groupStockSummaries(allSummaries, intervalInMin)
    }

    fun groupStockSummaries(
        summaries: List<StockSummaryEntity>,
        intervalInMin: Int
    ): List<StockSummaryEntity> {
        if (summaries.isEmpty()) return emptyList()

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val dynamicStart = summaries.minOfOrNull { parseTime(it.lastUpdated) } ?: LocalTime.of(9, 15)

        return summaries
            .groupBy {
                val time = parseTime(it.lastUpdated)
                val minutesOffset = Duration.between(dynamicStart, time).toMinutes().toInt()
                dynamicStart.plusMinutes((minutesOffset / intervalInMin * intervalInMin).toLong())
            }
            .map { (startTime, items) ->
                StockSummaryEntity(
                    lastUpdated = startTime.format(formatter),  // used as group key
                    ltp = items.maxByOrNull { it.lastUpdated }?.ltp ?: 0.0,
                    overAllSentiment = items.sumOf { it.overAllSentiment },
                    stockBuyStr = items.sumOf { it.stockBuyStr },
                    stockSellStr = items.sumOf { it.stockSellStr },
                    lastMinSentiment = items.sumOf { it.lastMinSentiment },
                    buyAvg = items.sumOf { it.buyAvg },
                    sellAvg = items.sumOf { it.sellAvg },
                )
            }
            .sortedBy { parseTime(it.lastUpdated) }
    }

    fun parseTime(timeStr: String): LocalTime =
        LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))

    suspend fun getOptionsSummaryForIntervals(
        intervalInMin: Int,
        repository: MarketRepository
    ): List<OptionsSummaryEntity> {
        val allSummaries = repository.getAllOptionsSummary().first()
        return groupOptionsSummaries(allSummaries, intervalInMin)
    }

    fun groupOptionsSummaries(
        summaries: List<OptionsSummaryEntity>,
        intervalInMin: Int
    ): List<OptionsSummaryEntity> {
        if (summaries.isEmpty()) return emptyList()

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val dynamicStart = summaries.minOfOrNull { parseTime(it.lastUpdated) } ?: LocalTime.of(9, 15)

        return summaries
            .groupBy {
                val time = parseTime(it.lastUpdated)
                val minutesOffset = Duration.between(dynamicStart, time).toMinutes().toInt()
                dynamicStart.plusMinutes((minutesOffset / intervalInMin * intervalInMin).toLong())
            }
            .map { (startTime, items) ->
                val latest = items.maxByOrNull { it.lastUpdated }

                OptionsSummaryEntity(
                    lastUpdated = startTime.format(formatter),
                    ltp = latest?.ltp ?: 0.0,
                    buyAvg = items.map { it.buyAvg }.average(),
                    sellAvg = items.map { it.sellAvg }.average(),
                    lastMinSentiment = items.map { it.lastMinSentiment }.average(),
                    optionsBuyStr = items.map { it.optionsBuyStr }.average(),
                    optionsSellStr = items.map { it.optionsSellStr }.average(),
                    overAllSentiment = items.map { it.overAllSentiment }.average(),
                    volumeTraded = items.sumOf { it.volumeTraded },
                    oiQty = latest?.oiQty ?: 0L,
                    oiChange = items.map { it.oiChange }.average(),
                    lastMinOIChange = items.map { it.lastMinOIChange }.average(),
                    overAllOIChange = items.map { it.overAllOIChange }.average()
                )
            }
            .sortedBy { parseTime(it.lastUpdated) }
    }
}