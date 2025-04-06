//package com.example.nifty50ops.database
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.example.nifty50ops.model.OptionsEntity
//import com.example.nifty50ops.model.StockEntity
//
//@Database(entities = [OptionsEntity::class], version = 1, exportSchema = false)
//abstract class OptionsDatabase : RoomDatabase() {
//    abstract fun optionsDao(): OptionsDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: OptionsDatabase? = null
//
//        fun getDatabase(context: Context): OptionsDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    OptionsDatabase::class.java,
//                    "options_database"
//                ).build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}
