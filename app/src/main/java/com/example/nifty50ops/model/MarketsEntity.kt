package com.example.nifty50ops.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_table")
data class MarketsEntity(
    @PrimaryKey val timestamp: String,
    val name: String,
    val ltp: Double
)

