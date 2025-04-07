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
import com.example.nifty50ops.model.StockEntity
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
    private val _stockBuyPercent = MutableStateFlow(0.0)
    val stockBuyPercent: StateFlow<Double> = _stockBuyPercent

    private val _stockSellPercent = MutableStateFlow(0.0)
    val stockSellPercent: StateFlow<Double> = _stockSellPercent

    private val _optionsBuyPercent = MutableStateFlow(0.0)
    val optionsBuyPercent: StateFlow<Double> = _optionsBuyPercent

    private val _optionsSellPercent = MutableStateFlow(0.0)
    val optionsSellPercent: StateFlow<Double> = _optionsSellPercent

    private val _optionsVol = MutableStateFlow(0)
    val optionsVol: StateFlow<Int> = _optionsVol

    private val _optionsOI = MutableStateFlow(0)
    val optionsOI: StateFlow<Int> = _optionsOI

    private val _optionsOIChange = MutableStateFlow(0.0)
    val optionsOIChange: StateFlow<Double> = _optionsOIChange

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            stockRepo.getAllStocks().combine(
                optionsRepo.getAllOptions()
            ) { stocks, options ->

                _stockBuyPercent.value = stocks.map { it.buyDiffPercent }.averageOrZero()
                _stockSellPercent.value = stocks.map { it.sellDiffPercent }.averageOrZero()

                _optionsBuyPercent.value = options.map { it.buyDiffPercent }.averageOrZero()
                _optionsSellPercent.value = options.map { it.sellDiffPercent }.averageOrZero()
                _optionsVol.value = options.sumOf { it.volTraded }
                _optionsOI.value = options.sumOf { it.oiQty }
                _optionsOIChange.value = options.sumOf { it.oiChange }

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

