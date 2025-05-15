package com.example.nifty50ops.utils

import com.example.nifty50ops.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

object CSVDataFormatter {

    suspend fun formatStocks(data: Flow<List<StockEntity>>): List<List<String>> {
        return data.first().map {
            listOf(
                it.timestamp, it.name, it.ltp.toString(), it.buyQty.toString(),
                it.sellQty.toString(), it.buyDiffPercent.toString(), it.sellDiffPercent.toString(),
                it.lastMinSentiment.toString(), it.buyStrengthPercent.toString(),
                it.sellStrengthPercent.toString(), it.overAllSentiment.toString()
            )
        }
    }

    suspend fun formatOptions(data: Flow<List<OptionsEntity>>): List<List<String>> {
        return data.first().map {
            listOf(
                it.timestamp, it.name, it.ltp.toString(), it.buyQty.toString(),
                it.sellQty.toString(), it.volTraded.toString(), it.buyDiffPercent.toString(),
                it.sellDiffPercent.toString(), it.lastMinSentiment.toString(),
                it.buyStrengthPercent.toString(), it.sellStrengthPercent.toString(),
                it.overAllSentiment.toString(), it.oiQty.toString(), it.oiChange.toString(),
                it.lastMinOIChange.toString(), it.overAllOIChange.toString()
            )
        }
    }

    suspend fun formatStockSummary(data: Flow<List<StockSummaryEntity>>): List<List<String>> {
        return data.first().map {
            listOf(
                it.lastUpdated, it.ltp.toString(), it.buyAvg.toString(), it.sellAvg.toString(),
                it.lastMinSentiment.toString(), it.stockBuyStr.toString(),
                it.stockSellStr.toString(), it.overAllSentiment.toString()
            )
        }
    }

    suspend fun formatOptionsSummary(data: Flow<List<OptionsSummaryEntity>>): List<List<String>> {
        return data.first().map {
            listOf(
                it.lastUpdated, it.ltp.toString(), it.volumeTraded.toString(), it.buyAvg.toString(),
                it.sellAvg.toString(), it.lastMinSentiment.toString(), it.optionsBuyStr.toString(),
                it.optionsSellStr.toString(), it.overAllSentiment.toString(), it.oiQty.toString(),
                it.oiChange.toString(), it.lastMinOIChange.toString(), it.overAllOIChange.toString()
            )
        }
    }
}
