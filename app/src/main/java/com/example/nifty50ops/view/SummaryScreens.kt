package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
import com.example.nifty50ops.utils.convertToLacsString
import com.example.nifty50ops.utils.twoDecimalDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SentimentSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

    var stockSummary by remember { mutableStateOf<StockSummaryEntity?>(null) }
    var optionSummary by remember { mutableStateOf<OptionsSummaryEntity?>(null) }
    var marketSummary by remember { mutableStateOf<MarketsEntity?>(null) }

    LaunchedEffect(Unit) {
        launch {
            repository.getLatestStockSummary().collect { stockSummary = it }
        }
        launch {
            repository.getLatestOptionsSummary().collect { optionSummary = it }
        }
        launch {
            repository.getLatestData().collect { marketSummary = it }
        }
    }

    if (stockSummary != null && optionSummary != null && marketSummary != null) {
        SummaryCard(
            title = "📊 Sentiment Summary",
            summaryItems = listOf(
                "PtsDiff" to "%.2f".format(marketSummary!!.pointsChanged.toDouble()),
                "St1" to "%.2f".format(stockSummary!!.lastMinSentiment),
                "StAll" to "%.2f".format(stockSummary!!.overAllSentiment),
                "Op1" to "%.2f".format(optionSummary!!.lastMinSentiment),
                "OpAll" to "%.2f".format(optionSummary!!.overAllSentiment),
                "OI1" to "%.2f".format(optionSummary!!.lastMinOIChange),
                "OIChange" to "%.2f".format(optionSummary!!.overAllOIChange)
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

    var stockSummary by remember { mutableStateOf<StockSummaryEntity?>(null) }

    LaunchedEffect(Unit) {
        repository.getLatestStockSummary().collect { summary ->
            stockSummary = summary
        }
    }

    stockSummary?.let { summary ->
        SummaryCard(
            title = "📊 Stocks Summary",
            summaryItems = listOf(
                "Time" to summary.lastUpdated,
                "Buy Avg %" to "%.2f".format(summary.buyAvg),
                "Sell Avg %" to "%.2f".format(summary.sellAvg),
                "LastMin" to "%.2f".format(summary.lastMinSentiment),
                "BuyStr" to "%.2f".format(summary.stockBuyStr),
                "SellStr" to "%.2f".format(summary.stockSellStr),
                "OverAll" to "%.2f".format(summary.overAllSentiment)
            ),
            onClick = {
                navController.navigate("stock_summary_history")
            }
        )
    }
}

@Composable
fun OptionsSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

    var optionsSummary by remember { mutableStateOf<OptionsSummaryEntity?>(null) }

    LaunchedEffect(Unit) {
        repository.getLatestOptionsSummary().collect { summary ->
            optionsSummary = summary
        }
    }

    optionsSummary?.let { summary ->
        SummaryCard(
            title = "📉 Options Summary",
            summaryItems = listOf(
                "Time" to summary.lastUpdated,
                "Buy Avg %" to "%.2f".format(summary.buyAvg),
                "Sell Avg %" to "%.2f".format(summary.sellAvg),
                "LastMin" to "%.2f".format(summary.lastMinSentiment),
                "BuyStr " to "%.2f".format(summary.optionsBuyStr),
                "SellStr " to "%.2f".format(summary.optionsSellStr),
                "OverAll" to "%.2f".format(summary.lastMinSentiment)
            ),
            onClick = {
                navController.navigate("options_summary_history")
            }
        )
    }
}

@Composable
fun OISummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

    var optionsSummary by remember { mutableStateOf<OptionsSummaryEntity?>(null) }

    LaunchedEffect(Unit) {
        repository.getLatestOptionsSummary().collect { summary ->
            optionsSummary = summary
        }
    }

    optionsSummary?.let { summary ->
        SummaryCard(
            title = "📉 OI Summary",
            summaryItems = listOf(
                "Time" to summary.lastUpdated,
                "OI" to convertToLacsString(summary.oiQty.toInt()),
                "OI Change" to convertToLacsString(summary.oiChange.toInt()),
                "LastMin" to "%.2f".format(summary.lastMinOIChange),
                "OverAll" to "%.2f".format(summary.overAllOIChange)
            ),
            onClick = {
                navController.navigate("oi_summary_history")
            }
        )
    }
}

@Composable
fun SummaryCard(title: String, summaryItems: List<Pair<String, String>>, onClick: (() -> Unit)? = null) {
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
                summaryItems.forEach { (label, value) ->
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
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SentimentSummaryHistoryScreen(context: Context) {
    val stockDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(stockDao)

    var stockList by remember { mutableStateOf<List<StockSummaryEntity>>(emptyList()) }
    var optionList by remember { mutableStateOf<List<OptionsSummaryEntity>>(emptyList()) }
    var marketList by remember { mutableStateOf<List<MarketsEntity>>(emptyList()) }


    val listState = rememberLazyListState()

    LaunchedEffect(true) {
        launch {
            repository.getAllStockSummary().collectLatest { stockList = it }
        }
        launch {
            repository.getAllOptionsSummary().collectLatest { optionList = it }
        }
        launch {
            repository.getAllData().collectLatest { marketList = it }
        }
    }

    val horizontalScrollState = rememberScrollState()

    // Zip the three lists safely
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
                .width(600.dp) // Adjust width for more columns
                .padding(horizontal = 8.dp)
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

@Composable
fun StockSummaryHistoryScreen(context: Context) {
    val stockDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(stockDao)

    var stockList by remember { mutableStateOf<List<StockSummaryEntity>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(true) {
        repository.getAllStockSummary().collectLatest { newList ->
            stockList = newList

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
                .padding(vertical = 10.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Time")
            TableHeaderCell("LTP")
            TableHeaderCell("Buy %")
            TableHeaderCell("Sell %")
            TableHeaderCell("1Min")
            TableHeaderCell("BuyStr %")
            TableHeaderCell("SellStr %")
            TableHeaderCell("Over All")
        }

        Divider(color = Color.Gray, thickness = 1.dp)

        LazyColumn(state = listState) {
            items(stockList) { item ->
                val buyColor = when {
                    item.stockBuyStr > 0 -> Color(0xFF2E7D32) // Green
                    item.stockBuyStr < 0 -> Color(0xFFC62828) // Red
                    else -> Color.Gray
                }

                val sellColor = when {
                    item.stockSellStr > 0 -> Color(0xFFC62828) // Red
                    item.stockSellStr < 0 -> Color(0xFF2E7D32) // Green
                    else -> Color.Gray
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(item.lastUpdated.take(5))
                    TableCell(twoDecimalDisplay(item.ltp).take(5))
                    TableCell(twoDecimalDisplay(item.buyAvg), color = buyColor)
                    TableCell(twoDecimalDisplay(item.sellAvg), color = sellColor)
                    TableCell(twoDecimalDisplay(item.lastMinSentiment))
                    TableCell(twoDecimalDisplay(item.stockBuyStr), color = buyColor)
                    TableCell(twoDecimalDisplay(item.stockSellStr), color = sellColor)
                    TableCell(twoDecimalDisplay(item.overAllSentiment))
                }
            }
        }
    }
}

@Composable
fun OptionsSummaryHistoryScreen(context: Context) {
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
                .background(MaterialTheme.colorScheme.primaryContainer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Time")
            TableHeaderCell("LTP")
            TableHeaderCell("Buy %")
            TableHeaderCell("Sell %")
            TableHeaderCell("1Min")
            TableHeaderCell("BuyStr %")
            TableHeaderCell("SellStr %")
            TableHeaderCell("Over All")
        }

        Divider(color = Color.Gray, thickness = 1.dp)

        LazyColumn(state = listState) {
            items(optionsList) { item ->
                val buyColor = when {
                    item.optionsBuyStr > 0 -> Color(0xFF2E7D32) // Green
                    item.optionsBuyStr < 0 -> Color(0xFFC62828) // Red
                    else -> Color.Gray
                }

                val sellColor = when {
                    item.optionsSellStr > 0 -> Color(0xFFC62828) // Red
                    item.optionsSellStr < 0 -> Color(0xFF2E7D32) // Green
                    else -> Color.Gray
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(item.lastUpdated.take(5))
                    TableCell(twoDecimalDisplay(item.ltp).take(5))
                    TableCell(twoDecimalDisplay(item.buyAvg), color = buyColor)
                    TableCell(twoDecimalDisplay(item.sellAvg), color = sellColor)
                    TableCell(twoDecimalDisplay(item.lastMinSentiment))
                    TableCell(twoDecimalDisplay(item.optionsBuyStr), color = buyColor)
                    TableCell(twoDecimalDisplay(item.optionsSellStr), color = sellColor)
                    TableCell(twoDecimalDisplay(item.overAllSentiment))
                }
            }
        }
    }
}

@Composable
fun OISummaryHistoryScreen(context: Context) {
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
                .background(MaterialTheme.colorScheme.primaryContainer),
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

        LazyColumn(state = listState) {
            items(optionsList) { item ->
                val buyColor = when {
                    item.optionsBuyStr > 0 -> Color(0xFF2E7D32) // Green
                    item.optionsBuyStr < 0 -> Color(0xFFC62828) // Red
                    else -> Color.Gray
                }

                val sellColor = when {
                    item.optionsSellStr > 0 -> Color(0xFFC62828) // Red
                    item.optionsSellStr < 0 -> Color(0xFF2E7D32) // Green
                    else -> Color.Gray
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(item.lastUpdated.take(5))
                    TableCell(twoDecimalDisplay(item.ltp).take(5))
                    TableCell(convertToLacsString(item.oiQty.toInt()))
                    TableCell(convertToLacsString(item.oiChange.toInt()))
                    TableCell(twoDecimalDisplay(item.lastMinOIChange))
                    TableCell(twoDecimalDisplay(item.overAllOIChange))
                }
            }
        }
    }
}


