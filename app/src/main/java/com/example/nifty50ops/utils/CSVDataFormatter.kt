package com.example.nifty50ops.utils

import com.example.nifty50ops.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

object CSVDataFormatter {

    suspend fun formatStocks(data: Flow<List<StockEntity>>): List<List<String>> {
        val headers = listOf(
            "Timestamp", "Name", "LTP", "Buy Qty", "Sell Qty",
            "Buy Diff %", "Sell Diff %", "1Min Sentiment",
            "Buy Strength %", "Sell Strength %", "Overall Sentiment"
        )
        val rows = data.first().map {
            listOf(
                it.timestamp, it.name, it.ltp.toString(), it.buyQty.toString(),
                it.sellQty.toString(), it.buyDiffPercent.toString(), it.sellDiffPercent.toString(),
                it.lastMinSentiment.toString(), it.buyStrengthPercent.toString(),
                it.sellStrengthPercent.toString(), it.overAllSentiment.toString()
            )
        }
        return listOf(headers) + rows
    }

    suspend fun formatOptions(data: Flow<List<OptionsEntity>>): List<List<String>> {
        val headers = listOf(
            "Timestamp", "Name", "LTP", "Buy Qty", "Sell Qty", "Vol Traded",
            "Buy Diff %", "Sell Diff %", "1Min Sentiment",
            "Buy Strength %", "Sell Strength %", "Overall Sentiment",
            "OI Qty", "OI Change", "Last Min OI Change", "Overall OI Change"
        )
        val rows = data.first().map {
            listOf(
                it.timestamp, it.name, it.ltp.toString(), it.buyQty.toString(),
                it.sellQty.toString(), it.volTraded.toString(), it.buyDiffPercent.toString(),
                it.sellDiffPercent.toString(), it.lastMinSentiment.toString(),
                it.buyStrengthPercent.toString(), it.sellStrengthPercent.toString(),
                it.overAllSentiment.toString(), it.oiQty.toString(), it.oiChange.toString(),
                it.lastMinOIChange.toString(), it.overAllOIChange.toString()
            )
        }
        return listOf(headers) + rows
    }

    suspend fun formatSentimentSummary(data: Flow<List<SentimentSummaryEntity>>): List<List<String>> {
        val headers = listOf(
            "Last Updated", "LTP", "Points Changed", "Stock 1Min Change",
            "Stock Overall Change", "Option 1Min Change", "Option Overall Change",
            "OI 1Min Change", "OI Overall Change"
        )
        val rows = data.first().map {
            listOf(
                it.lastUpdated, it.ltp.toString(), it.pointsChanged.toString(), it.stock1MinChange.toString(),
                it.stockOverAllChange.toString(), it.option1MinChange.toString(), it.optionOverAllChange.toString(),
                it.oi1MinChange.toString(), it.oiOverAllChange.toString()
            )
        }
        return listOf(headers) + rows
    }

    suspend fun formatStockSummary(data: Flow<List<StockSummaryEntity>>): List<List<String>> {
        val headers = listOf(
            "Last Updated", "LTP", "Buy Avg %", "Sell Avg %", "1Min Sentiment",
            "Buy Strength %", "Sell Strength %", "Overall Sentiment"
        )
        val rows = data.first().map {
            listOf(
                it.lastUpdated, it.ltp.toString(), it.buyAvg.toString(), it.sellAvg.toString(),
                it.lastMinSentiment.toString(), it.stockBuyStr.toString(),
                it.stockSellStr.toString(), it.overAllSentiment.toString()
            )
        }
        return listOf(headers) + rows
    }

    suspend fun formatOptionsSummary(data: Flow<List<OptionsSummaryEntity>>): List<List<String>> {
        val headers = listOf(
            "Last Updated", "LTP", "Vol Traded", "Buy Avg %", "Sell Avg %",
            "1Min Sentiment", "Buy Strength %", "Sell Strength %",
            "Overall Sentiment", "OI Qty", "OI Change",
            "Last Min OI Change", "Overall OI Change"
        )
        val rows = data.first().map {
            listOf(
                it.lastUpdated, it.ltp.toString(), it.volumeTraded.toString(), it.buyAvg.toString(),
                it.sellAvg.toString(), it.lastMinSentiment.toString(), it.optionsBuyStr.toString(),
                it.optionsSellStr.toString(), it.overAllSentiment.toString(), it.oiQty.toString(),
                it.oiChange.toString(), it.lastMinOIChange.toString(), it.overAllOIChange.toString()
            )
        }
        return listOf(headers) + rows
    }
}

