package com.example.nifty50ops.model

data class StockData(
    val name: String,
    val ltp: Double,
    val buyQty: Int,
    val sellQty: Int
)
