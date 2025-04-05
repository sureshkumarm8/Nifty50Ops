package com.example.nifty50ops.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_table")
data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val ltp: Double,
    val buyQty: Int,
    val sellQty: Int,
    val buyDiffPercent: Double = 0.0,
    val sellDiffPercent: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
