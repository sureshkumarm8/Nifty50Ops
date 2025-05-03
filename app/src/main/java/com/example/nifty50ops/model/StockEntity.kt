package com.example.nifty50ops.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_table",
    indices = [Index(value = ["timestamp", "name"], unique = true)]
)
data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: String,
    val name: String,
    val ltp: Double,
    val buyQty: Int,
    val sellQty: Int,
    val buyDiffPercent: Double = 0.0,
    val sellDiffPercent: Double = 0.0,
    val lastMinSentiment: Double = 0.0,
    val buyStrengthPercent : Double  = 0.0,
    val sellStrengthPercent : Double = 0.0,
    val overAllSentiment : Double = 0.0
)
