//package com.example.nifty50ops.database
//
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import com.example.nifty50ops.model.OptionsEntity
//import com.example.nifty50ops.model.StockEntity
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface OptionsDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertOption(option: OptionsEntity)
//
//    @Query("SELECT * FROM options_table ORDER BY volTraded DESC")
//    fun getAllOptions(): Flow<List<OptionsEntity>>
//}
