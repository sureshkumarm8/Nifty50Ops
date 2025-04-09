package com.example.nifty50ops.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_table")
data class MarketsEntity(
    @PrimaryKey val timestamp: String,
    val name: String,
    val ltp: Double
)

@Entity(tableName = "stocksSummary_table")
data class StockSummaryEntity(
    @PrimaryKey val lastUpdated: String,
    val buyAvg: Double,
    val sellAvg: Double,
    val stockBuyStr : Double,
    val stockSellStr : Double
)

@Entity(tableName = "optionsSummary_table")
data class OptionsSummaryEntity(
    @PrimaryKey val lastUpdated: String,
    val volumeTraded: Int,
    val buyAvg: Double,
    val sellAvg: Double,
    val optionsBuyStr : Double,
    val optionsSellStr : Double
)
