package com.example.nifty50ops.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.model.StockEntity

@Database(entities = [OptionsEntity::class, StockEntity::class, MarketsEntity::class], version = 1, exportSchema = false)
abstract class MarketDatabase : RoomDatabase() {
    abstract fun marketDao(): MarketDao

    companion object {
        @Volatile
        private var INSTANCE: MarketDatabase? = null

        fun getDatabase(context: Context): MarketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarketDatabase::class.java,
                    "market_database"
                )
                    .fallbackToDestructiveMigration() // Optional: dev use only
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

