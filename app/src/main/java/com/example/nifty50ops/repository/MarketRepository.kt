package com.example.nifty50ops.repository

import com.example.nifty50ops.database.MarketDao
import com.example.nifty50ops.model.MarketsEntity
import kotlinx.coroutines.flow.Flow

class MarketRepository(private val marketDao: MarketDao) {
    suspend fun insertEntity(option: MarketsEntity) = marketDao.insertMarketData(option)
    fun getAllData(): Flow<List<MarketsEntity>> = marketDao.getAllMarketData()
}
