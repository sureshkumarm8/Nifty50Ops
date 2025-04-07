package com.example.nifty50ops.repository

import com.example.nifty50ops.database.MarketDao
//import com.example.nifty50ops.database.OptionsDao
import com.example.nifty50ops.model.OptionsEntity
import kotlinx.coroutines.flow.Flow

class OptionsRepository(private val optionDao: MarketDao) {
    suspend fun insertOption(option: OptionsEntity) = optionDao.insertOption(option)
    fun getAllOptions(): Flow<List<OptionsEntity>> = optionDao.getLatestOptions()
}
