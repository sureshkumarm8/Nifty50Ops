package com.example.nifty50ops.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nifty50ops.model.StockEntity

@Database(entities = [StockEntity::class], version = 1, exportSchema = false)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao

    companion object {
        @Volatile
        private var INSTANCE: StockDatabase? = null

        fun getDatabase(context: Context): StockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockDatabase::class.java,
                    "stock_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
