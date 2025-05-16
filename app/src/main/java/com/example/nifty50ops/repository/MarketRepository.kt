package com.example.nifty50ops.repository

import com.example.nifty50ops.database.MarketDao
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.SentimentSummaryEntity
import com.example.nifty50ops.model.StockSummaryEntity
import kotlinx.coroutines.flow.Flow

class MarketRepository(private val marketDao: MarketDao) {
    suspend fun insertEntity(option: MarketsEntity) = marketDao.insertMarketData(option)
    fun getAllData(): Flow<List<MarketsEntity>> = marketDao.getAllMarketData()
    fun getLatestData(): Flow<MarketsEntity> = marketDao.getLatestMarketData()

    suspend fun insertStockSummary(stocksummary: StockSummaryEntity) = marketDao.insertStockSummary(stocksummary)
    fun getLatestStockSummary(): Flow<StockSummaryEntity> = marketDao.getLatestStockSummary()
    suspend fun insertOptionsSummary(optionsummary: OptionsSummaryEntity) = marketDao.insertOptionsSummary(optionsummary)
    fun getLatestOptionsSummary(): Flow<OptionsSummaryEntity> = marketDao.getLatestOptionsSummary()

    fun getAllStockSummary(): Flow<List<StockSummaryEntity>> = marketDao.getAllStockSummary()
    fun getAllOptionsSummary(): Flow<List<OptionsSummaryEntity>> = marketDao.getAllOptionsSummary()

    suspend fun insertSentimentSummary(sentimentSummary: SentimentSummaryEntity) = marketDao.insertSentimentSummary(sentimentSummary)
    fun getAllSentimentSummary(): Flow<List<SentimentSummaryEntity>> = marketDao.getAllSentimentSummary()
}
