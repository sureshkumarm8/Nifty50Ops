package com.example.nifty50ops.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.model.StockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarketData(entity : MarketsEntity)

    @Query("SELECT * FROM market_table ORDER BY timestamp DESC")
    fun getAllMarketData(): Flow<List<MarketsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Query("SELECT * FROM stock_table")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock_table WHERE timestamp = (SELECT MAX(timestamp) FROM stock_table) ORDER BY id ASC")
    fun getLatestStocks(): Flow<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOption(option: OptionsEntity)

    @Query("SELECT * FROM options_table ORDER BY volTraded DESC")
    fun getAllOptions(): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM options_table WHERE timestamp = (SELECT MAX(timestamp) FROM options_table) ORDER BY volTraded DESC")
    fun getLatestOptions(): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM stock_table WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getStocksFromLastMinute(startTime: String): Flow<List<StockEntity>>

    @Query("SELECT * FROM options_table WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getOptionsFromLastMinute(startTime: String): Flow<List<OptionsEntity>>

}
