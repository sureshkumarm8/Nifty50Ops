package com.example.nifty50ops.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_table",
    primaryKeys = ["timestamp", "name"]
)
data class StockEntity(
    val timestamp: String,
    val name: String,
    val ltp: Double,
    val buyQty: Int,
    val sellQty: Int,
    val buyDiffPercent: Double = 0.0,
    val sellDiffPercent: Double = 0.0,
    val buyStrengthPercent : Double  = 0.0,
    val sellStrengthPercent : Double = 0.0
)

