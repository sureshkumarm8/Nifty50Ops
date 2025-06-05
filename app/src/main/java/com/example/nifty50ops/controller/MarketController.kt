package com.example.nifty50ops.controller

import android.content.Context
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.SentimentSummaryEntity
import com.example.nifty50ops.model.StockSummaryEntity
import com.example.nifty50ops.network.ApiService
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MarketController(private val marketRepository: MarketRepository) {


    suspend fun fetchMarketData(context: Context) {
        val prefs = "NSE:13:INDEX"
        ApiService.fetchData(context, prefs)?.let { responseBody ->
            saveToDatabase(parseMarketResponse(responseBody))
        }
    }

    var previousLtp: Double? = null

    private suspend fun parseMarketResponse(response: String): List<MarketsEntity> {
        val marketsList = mutableListOf<MarketsEntity>()
        val jsonObject = JSONObject(response)
        val dataArray: JSONArray = jsonObject.optJSONArray("data") ?: JSONArray()

        // Fetch latest LTP from DB once before the loop
        if (previousLtp == null) {
            previousLtp = marketRepository.getLatestData().firstOrNull()?.ltp
        }

        for (i in 0 until dataArray.length()) {
            val marketData = dataArray.getJSONObject(i)
            val securityId = marketData.optInt("security_id", -1)
            if (securityId == -1) continue

            val name = "Nifty 50"
            val ltp = marketData.optDouble("last_price", 0.0)
            val lastTradeTime = marketData.optLong("last_update_time", 0)
            val timestamp = formatToHourMinute(lastTradeTime)


            val pointsChanged = if (previousLtp != null) {
                ltp - previousLtp!!
            } else {
                0.0 // First value, so no change
            }

            val marketsEntity = MarketsEntity(
                timestamp = timestamp,
                name = name,
                ltp = ltp,
                pointsChanged = pointsChanged.toInt(),
                summary = ""
            )

            marketsList.add(marketsEntity)
            previousLtp = ltp
        }

        return marketsList
    }

    private suspend fun saveToDatabase(marketsEntities: List<MarketsEntity>) {
        withContext(Dispatchers.IO) {
            for (marketsEntity in marketsEntities) {
                marketRepository.insertEntity(marketsEntity)
            }
        }
    }

    fun formatToHourMinute(unixTimestamp: Long): String {
        return if (unixTimestamp > 0) {
            val date = Date(unixTimestamp * 1000) // Convert to milliseconds
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } else {
            "--:--"
        }
    }

    suspend fun saveSummaries(context: Context) {
        val marketDao = MarketDatabase.getDatabase(context).marketDao()
        val marketRepo = MarketRepository(marketDao)
        val stockRepo = StockRepository(marketDao)
        val optionsRepo = OptionsRepository(marketDao)

        val markets = marketRepo.getLatestData()
        val stocks = stockRepo.getLastMinStocks().first()
        val options = optionsRepo.getLastMinOptions().first()

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

        val marketValue = markets.first()

        val stockSummaryEntity = StockSummaryEntity(
            lastUpdated = stockTime,
            ltp = marketValue.ltp,
            buyAvg = stockBuyAvg,
            sellAvg = stockSellAvg,
            lastMinSentiment = stockLastMinSentiment,
            stockBuyStr = stockBuyStr,
            stockSellStr = stockSellStr,
            overAllSentiment = stocksOverAllSentiment
        )

        val optionsSummaryEntity = OptionsSummaryEntity(
            lastUpdated = optionsTime,
            ltp = marketValue.ltp,
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
    }

    private fun List<Double>.averageOrZero(): Double = if (isNotEmpty()) average() else 0.0

    suspend fun saveSentimentSummary(context: Context) {
        try {
            val dao = MarketDatabase.getDatabase(context)
            val repository = MarketRepository(dao.marketDao())
            val latestStock = repository.getLatestStockSummary().first()
            val latestOption = repository.getLatestOptionsSummary().first()
            val latestMarket = repository.getLatestData().first()

            if (latestStock != null && latestOption != null && latestMarket != null) {
                val summary = SentimentSummaryEntity(
                    lastUpdated = latestStock.lastUpdated,
                    ltp = latestMarket.ltp,
                    pointsChanged = latestMarket.pointsChanged,
                    stock1MinChange = latestStock.lastMinSentiment,
                    stockOverAllChange = latestStock.overAllSentiment,
                    option1MinChange = latestOption.lastMinSentiment,
                    optionOverAllChange = latestOption.overAllSentiment,
                    oi1MinChange = latestOption.lastMinOIChange,
                    oiOverAllChange = latestOption.overAllOIChange
                )

                repository.insertSentimentSummary(summary)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun generateMarketReviewSummary(context: Context) {
        withContext(Dispatchers.IO) {
            val database = MarketDatabase.getDatabase(context)
            val dao = database.marketDao()
            val repository = MarketRepository(dao)
            val stockRepository = StockRepository(dao)
            val optionRepository = OptionsRepository(dao)

            // Collect one-time snapshot from Flow
            val stockList = stockRepository.getLastMinStocks().first()
            val optionList = optionRepository.getLastMinOptions().first()
            val sentiment = repository.getLastSentimentSummary().first()

            val topStock = stockList.maxByOrNull {
                abs(it.buyDiffPercent) + abs(it.sellDiffPercent)
            }

            val topOption = optionList.maxByOrNull {
                abs(it.buyDiffPercent) + abs(it.sellDiffPercent)
            }

            val stockSummary = """
            📊 Stocks Summary
            • Time: %s
            • Buy Str: %.1f | Sell Str: %.1f
            • Buy Avg %%: %.1f | Sell Avg %%: %.1f
            • Major Move: %s (Buy %%: %.1f, Sell %%: %.1f)
        """.trimIndent().format(
                sentiment.first().lastUpdated,
                topStock?.buyStrengthPercent ?: 0.0,
                topStock?.sellStrengthPercent ?: 0.0,
                topStock?.buyDiffPercent ?: 0.0,
                topStock?.sellDiffPercent ?: 0.0,
                topStock?.name ?: "-",
                topStock?.buyDiffPercent ?: 0.0,
                topStock?.sellDiffPercent ?: 0.0
            )

            val optionSummary = """
            📉 Options Summary
            • Buy Str: %.1f | Sell Str: %.1f
            • Buy Avg %%: %.1f | Sell Avg %%: %.1f
            • OI Chg: %.1f | Last Min OI: %.1f
            • Major Move: %s (Buy %%: %.1f, Sell %%: %.1f)
        """.trimIndent().format(
                topOption?.buyStrengthPercent ?: 0.0,
                topOption?.sellStrengthPercent ?: 0.0,
                topOption?.buyDiffPercent ?: 0.0,
                topOption?.sellDiffPercent ?: 0.0,
                topOption?.overAllOIChange ?: 0.0,
                topOption?.lastMinOIChange ?: 0.0,
                topOption?.name ?: "-",
                topOption?.buyDiffPercent ?: 0.0,
                topOption?.sellDiffPercent ?: 0.0
            )

            val sentimentSummary = """
            🧠 Sentiment Summary
            • Point Diff: ${sentiment.first().pointsChanged}
            • Stock Δ: ${sentiment.first().stockOverAllChange}, 1Min Δ: ${sentiment.first().stock1MinChange}
            • Option Δ: ${sentiment.first().optionOverAllChange}, 1Min Δ: ${sentiment.first().option1MinChange}
            • OI Δ: ${sentiment.first().oiOverAllChange}, 1Min OI Δ: ${sentiment.first().oi1MinChange}
        """.trimIndent()

            val summary = "$stockSummary\n\n$optionSummary\n\n$sentimentSummary"
            println("Review Summary:\n$summary")

            val existing = dao.getDataByTimestamp(sentiment.first().lastUpdated)
            val entity = if (existing != null) {
                existing.copy(summary = summary)
            } else {
                MarketsEntity(
                    timestamp = sentiment.first().lastUpdated,
                    name = "NIFTY50",
                    ltp = sentiment.first().ltp,
                    pointsChanged = sentiment.first().pointsChanged,
                    summary = summary
                )
            }
            dao.insertMarketData(entity)
        }
    }


}
