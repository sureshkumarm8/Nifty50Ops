package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.SentimentSummaryEntity
import com.example.nifty50ops.model.StockSummaryEntity
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.special_features.StockOptionsAggregator
import com.example.nifty50ops.utils.convertToCrString
import com.example.nifty50ops.utils.convertToLacsString
import com.example.nifty50ops.utils.oneDecimalDisplay
import com.example.nifty50ops.utils.setColorForBuy
import com.example.nifty50ops.utils.setColorForBuyStr
import com.example.nifty50ops.utils.setColorForSell
import com.example.nifty50ops.utils.setColorForSellStr
import com.example.nifty50ops.utils.twoDecimalDisplay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull


data class SentimentSummaryWithPrev(
    val current: SentimentSummaryEntity,
    val prev: SentimentSummaryEntity?
)

@Composable
fun SentimentSummaryHistoryScreen(context: Context, intervalTM: String) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

    var sentimentList by remember { mutableStateOf<List<SentimentSummaryEntity>>(emptyList()) }
    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()

    // Collect sentiment summary data
    LaunchedEffect(true) {
        repository.getAllSentimentSummary().collectLatest { newList ->
            sentimentList = newList.sortedBy { it.lastUpdated }
        }
    }

    // Scroll to bottom when new item is added
    LaunchedEffect(sentimentList.size) {
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .filter { it > 0 }
            .firstOrNull()
        if (sentimentList.isNotEmpty()) {
            listState.animateScrollToItem(sentimentList.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
    ) {
        Column(
            modifier = Modifier
                .width(580.dp)
                .padding(horizontal = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .background(Color(0xFF2196F3)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("Time", textAlign = TextAlign.Start)
                TableHeaderCell("LTP", textAlign = TextAlign.Center)
                TableHeaderCell("Point Diff")
                TableHeaderCell("Stock OverAll")
                TableHeaderCell("Stock 1Min")
                TableHeaderCell("Options OverAll")
                TableHeaderCell("Options 1Min")
                TableHeaderCell("OI Change")
                TableHeaderCell("OI 1Min")

            }

            val enrichedList = remember(sentimentList) {
                sentimentList.mapIndexed { index, curr ->
                    val prev = sentimentList.getOrNull(index - 1)
                    SentimentSummaryWithPrev(curr, prev)
                }
            }
            LazyColumn(state = listState) {
                items(enrichedList) { item ->
                    val curr = item.current
                    val prev = item.prev
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(
                            curr.lastUpdated.take(5),
                            textAlign = TextAlign.Start,
                            color = Color.Gray
                        )
                        TableCell(
                            twoDecimalDisplay(curr.ltp).take(5),
                            textAlign = TextAlign.Center,
                            color = setColorForBuyStr(curr.ltp.toDouble(), prev?.ltp ?: curr.ltp)
                        )
                        TableCell(
                            curr.pointsChanged.toString(),
                            color = setColorForBuy(
                                curr.pointsChanged.toDouble()
                            )
                        )
                        TableCell(
                            twoDecimalDisplay(curr.stockOverAllChange),
                            color = setColorForBuyStr(
                                curr.stockOverAllChange,
                                prev?.stockOverAllChange ?: curr.stockOverAllChange
                            )
                        )
                        TableCell(
                            twoDecimalDisplay(curr.stock1MinChange),
                            color = setColorForBuyStr(
                                curr.stock1MinChange,
                                prev?.stock1MinChange ?: curr.stock1MinChange
                            )
                        )
                        TableCell(
                            twoDecimalDisplay(curr.optionOverAllChange),
                            color = setColorForBuyStr(
                                curr.optionOverAllChange,
                                prev?.optionOverAllChange ?: curr.optionOverAllChange
                            )
                        )
                        TableCell(
                            twoDecimalDisplay(curr.option1MinChange),
                            color = setColorForBuyStr(
                                curr.option1MinChange,
                                prev?.option1MinChange ?: curr.option1MinChange
                            )
                        )
                        TableCell(
                            twoDecimalDisplay(curr.oiOverAllChange),
                            color = setColorForBuyStr(
                                curr.oiOverAllChange,
                                prev?.oiOverAllChange ?: curr.oiOverAllChange
                            )
                        )
                        TableCell(
                            twoDecimalDisplay(curr.oi1MinChange),
                            color = setColorForBuyStr(
                                curr.oi1MinChange,
                                prev?.oi1MinChange ?: curr.oi1MinChange
                            )
                        )
                    }
                }
            }
        }
    }
}

data class StockSummaryWithPrev(
    val current: StockSummaryEntity,
    val prev: StockSummaryEntity?
)

@Composable
fun StockSummaryHistoryScreen(context: Context, intervalTM: String) {
    val stockDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(stockDao)

    var stockList by remember { mutableStateOf<List<StockSummaryEntity>>(emptyList()) }
    val listState = rememberLazyListState()

    // Helper: Convert "1Min", "5Min" string to int
    fun parseInterval(intervalStr: String): Int {
        return intervalStr.removeSuffix("Min").toIntOrNull() ?: 1
    }

    LaunchedEffect(intervalTM) {
        val intervalInMin = parseInterval(intervalTM)
        val aggregator = StockOptionsAggregator()
        val groupedList = aggregator.getStockSummaryForIntervals(intervalInMin, repository)
        stockList = groupedList
        if (groupedList.isNotEmpty()) listState.animateScrollToItem(groupedList.lastIndex)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .background(Color(0xFF2196F3)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Time", textAlign = TextAlign.Center)
            TableHeaderCell("LTP ", textAlign = TextAlign.Center)
            TableHeaderCell("Over All%")
            TableHeaderCell("Buy  Str%")
            TableHeaderCell("Sell Str%")
            TableHeaderCell("1Min Diff")
            TableHeaderCell("Buy  Diff")
            TableHeaderCell("Sell Diff")
        }

        Divider(color = Color.Gray, thickness = 1.dp)

        // Compute enriched list for diff display, same as your existing logic
        val enrichedList = remember(stockList) {
            stockList.mapIndexed { index, curr ->
                val prev = stockList.getOrNull(index - 1)
                StockSummaryWithPrev(curr, prev)
            }
        }

        LazyColumn(state = listState) {
            items(enrichedList) { item ->
                val curr = item.current
                val prev = item.prev

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(
                        curr.lastUpdated.take(5),
                        weight = 1.5f,
                        textAlign = TextAlign.Start,
                        color = Color.Gray
                    )
                    TableCell(
                        oneDecimalDisplay(curr.ltp).take(5),
                        weight = 1.5f,
                        textAlign = TextAlign.Start,
                        color = setColorForBuy(curr.ltp)
                    )
                    TableCell(
                        oneDecimalDisplay(curr.overAllSentiment),
                        weight = 1.5f,
                        color = setColorForBuyStr(
                            curr.overAllSentiment,
                            prev?.overAllSentiment ?: curr.overAllSentiment
                        )
                    )
                    TableCell(
                        oneDecimalDisplay(curr.stockBuyStr),
                        weight = 1.5f,
                        color = setColorForBuyStr(
                            curr.stockBuyStr,
                            prev?.stockBuyStr ?: curr.stockBuyStr
                        )
                    )
                    TableCell(
                        oneDecimalDisplay(curr.stockSellStr),
                        weight = 1.5f,
                        color = setColorForSellStr(
                            curr.stockSellStr,
                            prev?.stockSellStr ?: curr.stockSellStr
                        )
                    )
                    TableCell(
                        oneDecimalDisplay(curr.lastMinSentiment),
                        weight = 1.5f,
                        color = setColorForBuy(
                            curr.lastMinSentiment
                        )
                    )
                    TableCell(
                        oneDecimalDisplay(curr.buyAvg),
                        weight = 1.5f,
                        color = setColorForBuy(curr.buyAvg)
                    )
                    TableCell(
                        oneDecimalDisplay(curr.sellAvg),
                        weight = 1.5f,
                        color = setColorForSell(curr.sellAvg)
                    )
                }
            }
        }
    }
}


data class OptionsSummaryWithPrev(
    val current: OptionsSummaryEntity,
    val prev: OptionsSummaryEntity?
)

@Composable
fun OptionsSummaryHistoryScreen(context: Context, intervalTM: String) {
    val optionsDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(optionsDao)

    var optionsList by remember { mutableStateOf<List<OptionsSummaryEntity>>(emptyList()) }
    val listState = rememberLazyListState()

    // Helper: Convert "1Min", "5Min" string to int
    fun parseInterval(intervalStr: String): Int {
        return intervalStr.removeSuffix("Min").toIntOrNull() ?: 1
    }

    LaunchedEffect(intervalTM) {
        val intervalInMin = parseInterval(intervalTM)
        val aggregator = StockOptionsAggregator()
        val groupedList = aggregator.getOptionsSummaryForIntervals(intervalInMin, repository)
        optionsList = groupedList
        if (groupedList.isNotEmpty()) listState.animateScrollToItem(groupedList.lastIndex)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .background(Color(0xFF2196F3)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Time", textAlign = TextAlign.Center)
            TableHeaderCell("LTP", textAlign = TextAlign.Center)
            TableHeaderCell("Over All%")
            TableHeaderCell("Buy  Str%")
            TableHeaderCell("Sell Str%")
            TableHeaderCell("1Min Diff")
            TableHeaderCell("Buy  Diff")
            TableHeaderCell("Sell Diff")
        }

        Divider(color = Color.Gray, thickness = 1.dp)
        val enrichedList = remember(optionsList) {
            optionsList.mapIndexed { index, curr ->
                val prev = optionsList.getOrNull(index - 1)
                OptionsSummaryWithPrev(curr, prev)
            }
        }
        LazyColumn(state = listState) {
            items(enrichedList) { item ->
                val curr = item.current
                val prev = item.prev

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(
                        curr.lastUpdated.take(5),
                        weight = 1.5f,
                        textAlign = TextAlign.Start,
                        color = Color.Gray
                    )
                    TableCell(
                        twoDecimalDisplay(curr.ltp).take(5),
                        weight = 1.5f,
                        color = setColorForBuy(curr.ltp)
                    )
                    TableCell(
                        oneDecimalDisplay(curr.overAllSentiment),
                        weight = 1.5f,
                        color = setColorForBuyStr(
                            curr.overAllSentiment,
                            prev?.overAllSentiment ?: curr.overAllSentiment
                        )
                    )
                    TableCell(
                        oneDecimalDisplay(curr.optionsBuyStr),
                        weight = 1.5f,
                        color = setColorForBuyStr(
                            curr.optionsBuyStr,
                            prev?.optionsBuyStr ?: curr.optionsBuyStr
                        )
                    )
                    TableCell(
                        oneDecimalDisplay(curr.optionsSellStr),
                        weight = 1.5f,
                        color = setColorForSellStr(
                            curr.optionsSellStr,
                            prev?.optionsSellStr ?: curr.optionsSellStr
                        )
                    )
                    TableCell(
                        oneDecimalDisplay(curr.lastMinSentiment),
                        weight = 1.5f,
                        color = setColorForBuy(
                            curr.lastMinSentiment
                        )
                    )
                    TableCell(
                        oneDecimalDisplay(curr.buyAvg),
                        weight = 1.5f,
                        color = setColorForBuy(curr.buyAvg)
                    )
                    TableCell(
                        oneDecimalDisplay(curr.sellAvg),
                        weight = 1.5f,
                        color = setColorForSell(curr.sellAvg)
                    )
                }
            }
        }
    }
}

@Composable
fun OISummaryHistoryScreen(context: Context, intervalTM: String) {
    val optionsDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(optionsDao)

    var optionsList by remember { mutableStateOf<List<OptionsSummaryEntity>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(true) {
        repository.getAllOptionsSummary().collectLatest { newList ->
            optionsList = newList

            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .filter { it > 0 }
                .first()

            listState.animateScrollToItem(newList.lastIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .background(Color(0xFF2196F3)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Time")
            TableHeaderCell("LTP")
            TableHeaderCell("OI")
            TableHeaderCell("OI Change")
            TableHeaderCell("1Min")
            TableHeaderCell("OverAll")
        }

        Divider(color = Color.Gray, thickness = 1.dp)
        val enrichedList = remember(optionsList) {
            optionsList.mapIndexed { index, curr ->
                val prev = optionsList.getOrNull(index - 1)
                OptionsSummaryWithPrev(curr, prev)
            }
        }
        LazyColumn(state = listState) {
            items(enrichedList) { item ->
                val curr = item.current
                val prev = item.prev
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(curr.lastUpdated.take(5), color = Color.Gray)
                    TableCell(
                        twoDecimalDisplay(curr.ltp).take(5),
                        color = setColorForBuyStr(curr.ltp, prev?.ltp ?: curr.ltp)
                    )
                    TableCell(
                        convertToCrString(curr.oiQty.toInt()),
                        color = setColorForBuyStr(
                            curr.oiQty.toDouble(),
                            prev?.oiQty?.toDouble() ?: curr.oiQty.toDouble()
                        )
                    )
                    TableCell(
                        convertToLacsString(curr.oiChange.toInt()),
                        color = setColorForBuyStr(
                            curr.oiChange.toDouble(),
                            prev?.oiChange?.toDouble() ?: curr.oiChange.toDouble()
                        )
                    )
                    TableCell(
                        twoDecimalDisplay(curr.lastMinOIChange),
                        color = setColorForBuyStr(
                            curr.lastMinOIChange,
                            prev?.lastMinOIChange ?: curr.lastMinOIChange
                        )
                    )
                    TableCell(
                        twoDecimalDisplay(curr.overAllOIChange),
                        color = setColorForBuyStr(
                            curr.overAllOIChange,
                            prev?.overAllOIChange ?: curr.overAllOIChange
                        )
                    )
                }
            }
        }
    }
}


