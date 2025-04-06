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

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            stockRepo.getAllStocks().combine(
                optionsRepo.getAllOptions()
            ) { stocks, options ->
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

    fun getStockBuyPercent(): Double =
        _uiState.value.stockList.map { it.buyDiffPercent }.averageOrZero()

    fun getStockSellPercent(): Double =
        _uiState.value.stockList.map { it.sellDiffPercent }.averageOrZero()

    fun getOptionsBuyPercent(): Double =
        _uiState.value.optionsList.map { it.buyDiffPercent }.averageOrZero()

    fun getOptionsSellPercent(): Double =
        _uiState.value.optionsList.map { it.sellDiffPercent }.averageOrZero()

    fun getOptionsVol(): Int = _uiState.value.optionsList.sumOf { it.volTraded }

    fun getOptionsOI(): Int = _uiState.value.optionsList.sumOf { it.oiQty }

    fun getOptionsOIChange(): Double = _uiState.value.optionsList.sumOf { it.oiChange }

    private fun List<Double>.averageOrZero(): Double = if (isNotEmpty()) average() else 0.0
}
