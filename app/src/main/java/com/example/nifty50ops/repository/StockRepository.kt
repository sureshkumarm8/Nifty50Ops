package com.example.nifty50ops.repository

import com.example.nifty50ops.database.MarketDao
//import com.example.nifty50ops.database.StockDao
import com.example.nifty50ops.model.StockEntity
import kotlinx.coroutines.flow.Flow

class StockRepository(private val stockDao: MarketDao) {
    suspend fun insertStock(stock: StockEntity) = stockDao.insertStock(stock)
    fun getAllStocks(): Flow<List<StockEntity>> = stockDao.getLatestStocks()
    fun getStockHistory(name: String): Flow<List<StockEntity>> = stockDao.getStockHistory(name)
}
