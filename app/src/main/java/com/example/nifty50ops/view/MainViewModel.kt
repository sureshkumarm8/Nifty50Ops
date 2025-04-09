package com.example.nifty50ops.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nifty50ops.controller.MarketController
import com.example.nifty50ops.controller.OptionsController
import com.example.nifty50ops.controller.StockController
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.model.StockSummaryEntity
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MainUiState(
    val stockList: List<StockEntity> = emptyList(),
    val optionsList: List<OptionsEntity> = emptyList(),
    val currentTime: String = "",
    val niftyPrice: Double = 0.0
)

class MainViewModel(context: Context) : ViewModel() {

    private val marketDao = MarketDatabase.getDatabase(context).marketDao()
    private val stockRepo = StockRepository(marketDao)
    private val optionsRepo = OptionsRepository(marketDao)
    private val marketRepo = MarketRepository(marketDao)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Summary StateFlows
    private val _stockSummary = MutableStateFlow(StockSummaryEntity("", 0.0, 0.0, 0.0, 0.0))
    val stockSummary: StateFlow<StockSummaryEntity> = _stockSummary

    private val _optionsSummary = MutableStateFlow(OptionsSummaryEntity("", 0, 0.0, 0.0, 0.0, 0.0))
    val optionsSummary: StateFlow<OptionsSummaryEntity> = _optionsSummary

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            stockRepo.getAllStocks().combine(
                optionsRepo.getAllOptions()
            ) { stocks, options ->

                val stockBuyAvg = stocks.map { it.buyDiffPercent }.averageOrZero()
                val stockSellAvg = stocks.map { it.sellDiffPercent }.averageOrZero()
                val stockBuyStr = stocks.map { it.buyStrengthPercent }.averageOrZero()
                val stockSellStr = stocks.map { it.sellStrengthPercent }.averageOrZero()

                val optionsBuyAvg = options.map { it.buyDiffPercent }.averageOrZero()
                val optionsSellAvg = options.map { it.sellDiffPercent }.averageOrZero()
                val optionsVolume = options.sumOf { it.volTraded }
                val optionsBuyStr = stocks.map { it.buyStrengthPercent }.averageOrZero()
                val optionsSellStr = stocks.map { it.sellStrengthPercent }.averageOrZero()

                val stockTime = stocks.maxByOrNull { it.timestamp }?.timestamp.orEmpty()
                val optionsTime = options.maxByOrNull { it.timestamp }?.timestamp.orEmpty()

                val stockSummaryEntity = StockSummaryEntity(
                    lastUpdated = stockTime,
                    buyAvg = stockBuyAvg,
                    sellAvg = stockSellAvg,
                    stockBuyStr = stockBuyStr,
                    stockSellStr = stockSellStr
                )

                val optionsSummaryEntity = OptionsSummaryEntity(
                    lastUpdated = optionsTime,
                    volumeTraded = optionsVolume,
                    buyAvg = optionsBuyAvg,
                    sellAvg = optionsSellAvg,
                    optionsBuyStr = optionsBuyStr,
                    optionsSellStr = optionsSellStr
                )
                marketRepo.insertStockSummary(stockSummaryEntity)
                marketRepo.insertOptionsSummary(optionsSummaryEntity)

                _stockSummary.value = stockSummaryEntity
                _optionsSummary.value = optionsSummaryEntity

                _uiState.update {
                    it.copy(
                        stockList = stocks,
                        optionsList = options
                    )
                }
            }.launchIn(this)

            marketRepo.getAllData().collect { markets ->
                if (markets.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            currentTime = markets[0].timestamp,
                            niftyPrice = markets[0].ltp
                        )
                    }
                }
            }
        }
    }

    private fun List<Double>.averageOrZero(): Double = if (isNotEmpty()) average() else 0.0
}

