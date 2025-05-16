package com.example.nifty50ops.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.SentimentSummaryEntity
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.model.StockSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarketData(entity : MarketsEntity)

    @Query("SELECT * FROM market_table ORDER BY timestamp DESC")
    fun getAllMarketData(): Flow<List<MarketsEntity>>

    @Query("SELECT * FROM market_table ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMarketData(): Flow<MarketsEntity>


//StocksScreen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockSummary(summary: StockSummaryEntity)

    @Query("SELECT * FROM stock_table")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock_table WHERE timestamp = (SELECT MAX(timestamp) FROM stock_table) ORDER BY id ASC")
    fun getLatestStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock_table WHERE (name, timestamp) IN (SELECT name, MIN(timestamp) FROM stock_table GROUP BY name)")
    fun getFirstMinStocks(): List<StockEntity>

    @Query("SELECT * FROM stock_table WHERE timestamp = (SELECT MAX(timestamp) FROM stock_table)")
    fun getLastMinStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock_table WHERE name = :name ORDER BY timestamp ASC")
    fun getStockHistory(name: String): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock_table WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getStocksFromLastMinute(startTime: String): Flow<List<StockEntity>>

    @Query("SELECT * FROM stocksSummary_table ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestStockSummary(): Flow<StockSummaryEntity>

    @Query("SELECT * FROM stocksSummary_table ORDER BY lastUpdated")
    fun getAllStockSummary(): Flow<List<StockSummaryEntity>>



    //OptionsScreen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOption(option: OptionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptionsSummary(summary: OptionsSummaryEntity)

    @Query("SELECT * FROM options_table ORDER BY volTraded DESC")
    fun getAllOptions(): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM options_table WHERE timestamp = (SELECT MAX(timestamp) FROM options_table) ORDER BY volTraded DESC")
    fun getLatestOptions(): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM options_table WHERE (name, timestamp) IN (SELECT name, MIN(timestamp) FROM options_table GROUP BY name)")
    fun getFirstMinOptions(): List<OptionsEntity>

    @Query("SELECT * FROM options_table WHERE timestamp = (SELECT MAX(timestamp) FROM options_table)")
    fun getLastMinOptions(): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM options_table WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getOptionsFromLastMinute(startTime: String): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM options_table WHERE name = :name ORDER BY timestamp ASC")
    fun getOptionHistory(name: String): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM optionsSummary_table ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestOptionsSummary(): Flow<OptionsSummaryEntity>

    @Query("SELECT * FROM optionsSummary_table ORDER BY lastUpdated")
    fun getAllOptionsSummary(): Flow<List<OptionsSummaryEntity>>

    //SentimentSummaryScreen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentimentSummary(summary: SentimentSummaryEntity)

    @Query("SELECT * FROM sentimentSummary_table ORDER BY lastUpdated DESC")
    fun getAllSentimentSummary(): Flow<List<SentimentSummaryEntity>>

}
