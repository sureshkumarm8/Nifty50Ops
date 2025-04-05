package com.example.nifty50ops.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nifty50ops.model.StockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Query("SELECT * FROM stock_table")
    fun getAllStocks(): Flow<List<StockEntity>>
}
