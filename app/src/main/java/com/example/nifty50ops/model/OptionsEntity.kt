package com.example.nifty50ops.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "options_table",
    primaryKeys = ["timestamp", "name"]
)
data class OptionsEntity(
    val timestamp: String,
    val name: String,
    val ltp: Double,
    val buyQty: Int,
    val sellQty: Int,
    val volTraded: Int,
    val buyDiffPercent: Double = 0.0,
    val sellDiffPercent: Double = 0.0,
    val buyStrengthPercent: Double,
    val sellStrengthPercent: Double,
    val oiQty: Int,
    val oiChange: Double =0.0,

)
