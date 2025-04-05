package com.example.nifty50ops.repository

import com.example.nifty50ops.database.StockDao
import com.example.nifty50ops.model.StockEntity
import kotlinx.coroutines.flow.Flow

class StockRepository(private val stockDao: StockDao) {
    suspend fun insertStock(stock: StockEntity) = stockDao.insertStock(stock)
    fun getAllStocks(): Flow<List<StockEntity>> = stockDao.getAllStocks()
}
