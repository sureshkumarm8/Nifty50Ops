package com.example.nifty50ops.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_table")
data class MarketsEntity(
    @PrimaryKey val timestamp: String,
    val name: String,
    val ltp: Double,
    val pointsChanged: Int,
    val summary: String
)

@Entity(tableName = "stocksSummary_table")
data class StockSummaryEntity(
    @PrimaryKey val lastUpdated: String,
    val ltp: Double,
    val ltpLastMin: Double,
    val ltpOverall: Double,
    val buyAvg: Double,
    val sellAvg: Double,
    val lastMinSentiment: Double,
    val stockBuyStr: Double,
    val stockSellStr: Double,
    val overAllSentiment: Double
)

@Entity(tableName = "optionsSummary_table")
data class OptionsSummaryEntity(
    @PrimaryKey val lastUpdated: String,
    val ltp: Double,
    val volumeTraded: Int,
    val buyAvg: Double,
    val sellAvg: Double,
    val lastMinSentiment: Double,
    val optionsBuyStr : Double,
    val optionsSellStr : Double,
    val overAllSentiment: Double,
    val oiQty : Long,
    val oiChange : Double,
    val lastMinOIChange : Double,
    val overAllOIChange : Double
)

@Entity(tableName = "sentimentSummary_table")
data class SentimentSummaryEntity(
    @PrimaryKey val lastUpdated: String,
    val ltp: Double,
    val pointsChanged: Int,
    val stock1MinChange : Double,
    val stockOverAllChange : Double,
    val option1MinChange : Double,
    val optionOverAllChange : Double,
    val oi1MinChange : Double,
    val oiOverAllChange : Double
)

@Entity(tableName = "market_insights_table")
data class MarketInsightEntity(
    @PrimaryKey val timestamp: String,
    val name: String = "NIFTY50",
    val ltp: Double,
    val pointsChanged: Int,
    val intervalMinutes : String,
    val stockSummary: String,
    val optionSummary: String,
    val sentimentSummary: String,
    val top5StockFluctuations: String,
    val top5OptionFluctuations: String,
    val tradingHints: String,
    val gen_ai_insights: String? = null  // NOW nullable
)
