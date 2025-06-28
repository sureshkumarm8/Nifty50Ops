package com.example.nifty50ops.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

//    private val _stockSummary = MutableStateFlow(StockSummaryEntity("", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0))
//    val stockSummary: StateFlow<StockSummaryEntity> = _stockSummary
//
//    private val _optionsSummary = MutableStateFlow(OptionsSummaryEntity("", 0.0,0, 0.0, 0.0, 0.0, 0.0,0.0, 0.0,0,0.0,0.0,0.0))
//    val optionsSummary: StateFlow<OptionsSummaryEntity> = _optionsSummary

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                stockRepo.getAllStocks(),
                optionsRepo.getAllOptions(),
                marketRepo.getAllData()
            ) { stocks, options, markets ->
//                computeAndSaveSummary(stocks, options, markets)
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

/*
    private suspend fun computeAndSaveSummary(
        stocks: List<StockEntity>,
        options: List<OptionsEntity>,
        markets : List<MarketsEntity>
    ) {
        val stockBuyAvg = stocks.map { it.buyDiffPercent }.averageOrZero()
        val stockSellAvg = stocks.map { it.sellDiffPercent }.averageOrZero()
        val stockLastMinSentiment = stocks.map { it.lastMinSentiment }.averageOrZero()
        val stockBuyStr = stocks.map { it.buyStrengthPercent }.averageOrZero()
        val stockSellStr = stocks.map { it.sellStrengthPercent }.averageOrZero()
        val stocksOverAllSentiment = stocks.map { it.overAllSentiment }.averageOrZero()

        val optionsBuyAvg = options.map { it.buyDiffPercent }.averageOrZero()
        val optionsSellAvg = options.map { it.sellDiffPercent }.averageOrZero()
        val optionsVolume = options.sumOf { it.volTraded }
        val optionsLastMinSentiment = options.map { it.lastMinSentiment }.averageOrZero()
        val optionsBuyStr = options.map { it.buyStrengthPercent }.averageOrZero()
        val optionsSellStr = options.map { it.sellStrengthPercent }.averageOrZero()
        val optionsOverAllSentiment = options.map { it.overAllSentiment }.averageOrZero()
        val oiQty = options.sumOf { it.oiQty }
        val oiChange = options.map { it.oiChange }.averageOrZero()
        val lastMinOIChange = options.map { it.lastMinOIChange }.averageOrZero()
        val overAllOIChange = options.map { it.overAllOIChange }.averageOrZero()

        val stockTime = stocks.maxByOrNull { it.timestamp }?.timestamp.orEmpty()
        val optionsTime = options.maxByOrNull { it.timestamp }?.timestamp.orEmpty()

        val marketValue = markets.maxByOrNull { it.timestamp }

        val stockSummaryEntity = StockSummaryEntity(
            lastUpdated = stockTime,
            ltp = marketValue?.ltp ?: 0.0,
            buyAvg = stockBuyAvg,
            sellAvg = stockSellAvg,
            lastMinSentiment = stockLastMinSentiment,
            stockBuyStr = stockBuyStr,
            stockSellStr = stockSellStr,
            overAllSentiment = stocksOverAllSentiment
        )

        val optionsSummaryEntity = OptionsSummaryEntity(
            lastUpdated = optionsTime,
            ltp = marketValue?.ltp ?: 0.0,
            volumeTraded = optionsVolume,
            buyAvg = optionsBuyAvg,
            sellAvg = optionsSellAvg,
            lastMinSentiment = optionsLastMinSentiment,
            optionsBuyStr = optionsBuyStr,
            optionsSellStr = optionsSellStr,
            overAllSentiment = optionsOverAllSentiment,
            oiQty = oiQty.toLong(),
            oiChange = oiChange,
            lastMinOIChange = lastMinOIChange,
            overAllOIChange = overAllOIChange
        )

        // Persist to database
        marketRepo.insertStockSummary(stockSummaryEntity)
        marketRepo.insertOptionsSummary(optionsSummaryEntity)

        // Update flows
        _stockSummary.value = stockSummaryEntity
        _optionsSummary.value = optionsSummaryEntity

        _uiState.update {
            it.copy(
                stockList = stocks,
                optionsList = options
            )
        }

        // Debug log
//        Log.d("MainViewModel", "Stock BuyAvg: $stockBuyAvg | SellAvg: $stockSellAvg")
//        Log.d("MainViewModel", "Options Volume: $optionsVolume | BuyAvg: $optionsBuyAvg | SellAvg: $optionsSellAvg")
    }
*/
    private fun List<Double>.averageOrZero(): Double = if (isNotEmpty()) average() else 0.0
}
