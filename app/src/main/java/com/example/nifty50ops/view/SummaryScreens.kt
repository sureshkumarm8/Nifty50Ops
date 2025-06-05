package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.SentimentSummaryEntity
import com.example.nifty50ops.model.StockSummaryEntity
import com.example.nifty50ops.repository.MarketRepository
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
import kotlinx.coroutines.launch

@Composable
fun SentimentSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

    var sentimentList by remember { mutableStateOf<List<SentimentSummaryEntity>>(emptyList()) }

    // Collect sentiment summary data
    LaunchedEffect(true) {
        repository.getLastSentimentSummary().collectLatest { newList ->
            sentimentList = newList.sortedBy { it.lastUpdated }
        }
    }

    if (sentimentList.isNotEmpty()) {
        val curr = sentimentList.last()
        val prev = sentimentList.getOrNull(sentimentList.lastIndex - 1)

        SummaryCard(
            title = "📊 Sentiment Summary",
            summaryItems = listOf(
                "PtsDiff" to "%.2f".format(curr.pointsChanged.toDouble()),
                "StAll" to "%.2f".format(curr.stockOverAllChange),
                "St1" to "%.2f".format(curr.stock1MinChange),
                "OpAll" to "%.2f".format(curr.optionOverAllChange),
                "Op1" to "%.2f".format(curr.option1MinChange),
                "OIChange" to "%.2f".format(curr.oiOverAllChange),
                "OI1" to "%.2f".format(curr.oi1MinChange)
            ),
            colorOverrides = listOf(
                setColorForBuyStr(curr.pointsChanged.toDouble(), prev?.pointsChanged?.toDouble() ?: curr.pointsChanged.toDouble()),
                setColorForBuyStr(curr.stockOverAllChange, prev?.stockOverAllChange ?: curr.stockOverAllChange),
                setColorForBuyStr(curr.stock1MinChange, prev?.stock1MinChange ?: curr.stock1MinChange),
                setColorForBuyStr(curr.optionOverAllChange, prev?.optionOverAllChange ?: curr.optionOverAllChange),
                setColorForBuyStr(curr.option1MinChange, prev?.option1MinChange ?: curr.option1MinChange),
                setColorForBuyStr(curr.oiOverAllChange, prev?.oiOverAllChange ?: curr.oiOverAllChange),
                setColorForBuyStr(curr.oi1MinChange, prev?.oi1MinChange ?: curr.oi1MinChange)

            ),
            onClick = {
                navController.navigate("sentiment_summary_history")
            }
        )
    }
}


@Composable
fun StockSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

//    var stockSummary by remember { mutableStateOf<StockSummaryEntity?>(null) }
    var stockSummary by remember { mutableStateOf<List<StockSummaryEntity>>(emptyList()) }

    LaunchedEffect(true) {
        repository.getAllStockSummary().collect { summaries ->
            stockSummary = summaries
        }
    }

    if (stockSummary.isNotEmpty()) {
        val curr = stockSummary.last()
        val prev = stockSummary.getOrNull(stockSummary.lastIndex - 1)
        SummaryCard(
            title = "📊 Stocks Summary",
            summaryItems = listOf(
                "Time" to curr.lastUpdated,
                "OverAll" to "%.1f".format(curr.overAllSentiment),
                "BuyStr" to "%.1f".format(curr.stockBuyStr),
                "SellStr" to "%.1f".format(curr.stockSellStr),
                "LastMin" to "%.1f".format(curr.lastMinSentiment),
                "Buy Avg %" to "%.1f".format(curr.buyAvg),
                "Sell Avg %" to "%.1f".format(curr.sellAvg)
            ),
            onClick = {
                navController.navigate("stock_summary_history")
            },
            colorOverrides = listOf(
                Color.Black,
                setColorForBuyStr(curr.overAllSentiment, prev?.overAllSentiment ?: curr.overAllSentiment),
                setColorForBuyStr(curr.stockBuyStr, prev?.stockBuyStr ?: curr.stockBuyStr),
                setColorForSellStr(curr.stockSellStr, prev?.stockSellStr ?: curr.stockSellStr),
                setColorForBuy(curr.lastMinSentiment),
                setColorForBuy(curr.buyAvg),
                setColorForSell(curr.sellAvg)
            )
        )
    }
}

@Composable
fun OptionsSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

//    var optionsSummary by remember { mutableStateOf<OptionsSummaryEntity?>(null) }
    var optionsSummary by remember { mutableStateOf<List<OptionsSummaryEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.getAllOptionsSummary().collect { summary ->
            optionsSummary = summary
        }
    }

    if (optionsSummary.isNotEmpty()) {
        val curr = optionsSummary.last()
        val prev = optionsSummary.getOrNull(optionsSummary.lastIndex - 1)
        SummaryCard(
            title = "📉 Options Summary",
            summaryItems = listOf(
                "Time" to curr.lastUpdated,
                "OverAll" to "%.1f".format(curr.overAllSentiment),
                "BuyStr " to "%.1f".format(curr.optionsBuyStr),
                "SellStr " to "%.1f".format(curr.optionsSellStr),
                "LastMin" to "%.1f".format(curr.lastMinSentiment),
                "Buy Avg %" to "%.1f".format(curr.buyAvg),
                "Sell Avg %" to "%.1f".format(curr.sellAvg),
            ),
            onClick = {
                navController.navigate("options_summary_history")
            },
            colorOverrides = listOf(
                Color.Black,
                setColorForBuyStr(curr.overAllSentiment, prev?.overAllSentiment ?: curr.overAllSentiment),
                setColorForBuyStr(curr.optionsBuyStr, prev?.optionsBuyStr ?: curr.optionsBuyStr),
                setColorForSellStr(curr.optionsSellStr, prev?.optionsSellStr ?: curr.optionsSellStr),
                setColorForBuy(curr.lastMinSentiment),
                setColorForBuy(curr.buyAvg),
                setColorForSell(curr.sellAvg)
            )
        )
    }
}

@Composable
fun OISummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

//    var optionsSummary by remember { mutableStateOf<OptionsSummaryEntity?>(null) }
    var optionsSummary by remember { mutableStateOf<List<OptionsSummaryEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.getAllOptionsSummary().collect { summary ->
            optionsSummary = summary
        }
    }

    if (optionsSummary.isNotEmpty()) {
        val curr = optionsSummary.last()
        val prev = optionsSummary.getOrNull(optionsSummary.lastIndex - 1)

        SummaryCard(
            title = "📉 OI Summary",
            summaryItems = listOf(
                "Time" to curr.lastUpdated,
                "OI" to convertToCrString(curr.oiQty.toInt()),
                "OI Change" to convertToLacsString(curr.oiChange.toInt()),
                "LastMin" to "%.2f".format(curr.lastMinOIChange),
                "OverAll" to "%.2f".format(curr.overAllOIChange)
            ),
            onClick = {
                navController.navigate("oi_summary_history")
            },
            colorOverrides = listOf(
                Color.Black,
                setColorForBuyStr(curr.oiQty.toDouble(), prev?.oiQty?.toDouble() ?: curr.oiQty.toDouble()),
                setColorForBuyStr(curr.oiChange, prev?.oiChange ?: curr.oiChange),
                setColorForBuyStr(curr.lastMinOIChange, prev?.lastMinOIChange ?: curr.lastMinOIChange),
                setColorForBuyStr(curr.overAllOIChange, prev?.overAllOIChange ?: curr.overAllOIChange)
            )
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    summaryItems: List<Pair<String, String>>,
    onClick: (() -> Unit)? = null,
    colorOverrides: List<Color> = emptyList()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                summaryItems.forEachIndexed { index, (label, value) ->
                    val valueColor = colorOverrides.getOrNull(index) ?: Color.Black
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = valueColor
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SentimentSummaryHistoryScreen_3Tables(context: Context) {
    val stockDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(stockDao)

    var stockList by remember { mutableStateOf<List<StockSummaryEntity>>(emptyList()) }
    var optionList by remember { mutableStateOf<List<OptionsSummaryEntity>>(emptyList()) }
    var marketList by remember { mutableStateOf<List<MarketsEntity>>(emptyList()) }

    val listState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()

    // Collect data
    LaunchedEffect(true) {
        launch {
            repository.getAllStockSummary().collectLatest { newList ->
                stockList = newList
            }
        }
        launch {
            repository.getAllOptionsSummary().collectLatest { newList ->
                optionList = newList
            }
        }
        launch {
            repository.getAllData().collectLatest { newList ->
                marketList = newList.reversed()
            }
        }
    }

    // Scroll to bottom after LazyColumn is fully composed and new item is added
    LaunchedEffect(marketList.size) {
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .filter { it > 0 }
            .firstOrNull()
        if (marketList.isNotEmpty()) {
            listState.animateScrollToItem(marketList.lastIndex)
        }
    }

    // Combine all 3 lists
    val combinedList = remember(stockList, optionList, marketList) {
        stockList
            .zip(optionList) { stock, option -> stock to option }
            .zip(marketList) { (stock, option), market -> Triple(stock, option, market) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
    ) {
        Column(
            modifier = Modifier
                .width(620.dp)
                .padding(horizontal = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("Time")
                TableHeaderCell("LTP")
                TableHeaderCell("LTP Change")
                TableHeaderCell("Stock 1Min")
                TableHeaderCell("Stock OverAll")
                TableHeaderCell("Options 1Min")
                TableHeaderCell("Options OverAll")
                TableHeaderCell("OI    1Min")
                TableHeaderCell("OI Change")
            }

            Divider(color = Color.Gray, thickness = 1.dp)

            LazyColumn(state = listState) {
                items(combinedList) { (stock, option, market) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(market.timestamp.take(5))
                        TableCell(twoDecimalDisplay(market.ltp).take(5))
                        TableCell(twoDecimalDisplay(market.pointsChanged.toDouble()))
                        TableCell(twoDecimalDisplay(stock.lastMinSentiment))
                        TableCell(twoDecimalDisplay(stock.overAllSentiment))
                        TableCell(twoDecimalDisplay(option.lastMinSentiment))
                        TableCell(twoDecimalDisplay(option.overAllSentiment))
                        TableCell(twoDecimalDisplay(option.lastMinOIChange))
                        TableCell(twoDecimalDisplay(option.overAllOIChange))
                    }
                }
            }
        }
    }
}


