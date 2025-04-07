package com.example.nifty50ops.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "options_table",
    indices = [Index(value = ["timestamp", "name"], unique = true)]
)
data class OptionsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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
