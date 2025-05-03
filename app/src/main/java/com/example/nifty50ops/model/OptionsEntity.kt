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
    val lastMinSentiment: Double = 0.0,
    val buyStrengthPercent : Double  = 0.0,
    val sellStrengthPercent : Double = 0.0,
    val overAllSentiment : Double = 0.0,
    val oiQty: Int,
    val oiChange: Double =0.0,
    val lastMinOIChange: Double =0.0,
    val overAllOIChange: Double =0.0

)
