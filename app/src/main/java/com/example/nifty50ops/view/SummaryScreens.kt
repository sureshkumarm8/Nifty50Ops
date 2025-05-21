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
import com.example.nifty50ops.utils.setColorForBuyStr
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
                "St1" to "%.2f".format(curr.stock1MinChange),
                "StAll" to "%.2f".format(curr.stockOverAllChange),
                "Op1" to "%.2f".format(curr.option1MinChange),
                "OpAll" to "%.2f".format(curr.optionOverAllChange),
                "OI1" to "%.2f".format(curr.oi1MinChange),
                "OIChange" to "%.2f".format(curr.oiOverAllChange)
            ),
            colorOverrides = listOf(
                setColorForBuyStr(curr.pointsChanged.toDouble(), prev?.pointsChanged?.toDouble() ?: curr.pointsChanged.toDouble()),
                setColorForBuyStr(curr.stock1MinChange, prev?.stock1MinChange ?: curr.stock1MinChange),
                setColorForBuyStr(curr.stockOverAllChange, prev?.stockOverAllChange ?: curr.stockOverAllChange),
                setColorForBuyStr(curr.option1MinChange, prev?.option1MinChange ?: curr.option1MinChange),
                setColorForBuyStr(curr.optionOverAllChange, prev?.optionOverAllChange ?: curr.optionOverAllChange),
                setColorForBuyStr(curr.oi1MinChange, prev?.oi1MinChange ?: curr.oi1MinChange),
                setColorForBuyStr(curr.oiOverAllChange, prev?.oiOverAllChange ?: curr.oiOverAllChange)
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
                "Buy Avg %" to "%.1f".format(curr.buyAvg),
                "Sell Avg %" to "%.1f".format(curr.sellAvg),
                "LastMin" to "%.1f".format(curr.lastMinSentiment),
                "BuyStr" to "%.1f".format(curr.stockBuyStr),
                "SellStr" to "%.1f".format(curr.stockSellStr),
                "OverAll" to "%.1f".format(curr.overAllSentiment)
            ),
            onClick = {
                navController.navigate("stock_summary_history")
            },
            colorOverrides = listOf(
                Color.Black,
                setColorForBuyStr(curr.buyAvg, prev?.buyAvg ?: curr.buyAvg),
                setColorForSellStr(curr.sellAvg, prev?.sellAvg ?: curr.sellAvg),
                setColorForBuyStr(curr.lastMinSentiment, prev?.lastMinSentiment ?: curr.lastMinSentiment),
                setColorForBuyStr(curr.stockBuyStr, prev?.stockBuyStr ?: curr.stockBuyStr),
                setColorForBuyStr(curr.stockSellStr, prev?.stockSellStr ?: curr.stockSellStr),
                setColorForBuyStr(curr.overAllSentiment, prev?.overAllSentiment ?: curr.overAllSentiment),

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
                "Buy Avg %" to "%.1f".format(curr.buyAvg),
                "Sell Avg %" to "%.1f".format(curr.sellAvg),
                "LastMin" to "%.1f".format(curr.lastMinSentiment),
                "BuyStr " to "%.1f".format(curr.optionsBuyStr),
                "SellStr " to "%.1f".format(curr.optionsSellStr),
                "OverAll" to "%.1f".format(curr.overAllSentiment)
            ),
            onClick = {
                navController.navigate("options_summary_history")
            },
            colorOverrides = listOf(
                Color.Black,
                setColorForBuyStr(curr.buyAvg, prev?.buyAvg ?: curr.buyAvg),
                setColorForBuyStr(curr.sellAvg, prev?.sellAvg ?: curr.sellAvg),
                setColorForBuyStr(curr.lastMinSentiment, prev?.lastMinSentiment ?: curr.lastMinSentiment),
                setColorForBuyStr(curr.optionsBuyStr, prev?.optionsBuyStr ?: curr.optionsBuyStr),
                setColorForBuyStr(curr.optionsSellStr, prev?.optionsSellStr ?: curr.optionsSellStr),
                setColorForBuyStr(curr.overAllSentiment, prev?.overAllSentiment ?: curr.overAllSentiment)
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

data class SentimentSummaryWithPrev(
    val current: SentimentSummaryEntity,
    val prev: SentimentSummaryEntity?
)
@Composable
fun SentimentSummaryHistoryScreen(context: Context) {
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
                TableHeaderCell("Time",textAlign = TextAlign.Start)
                TableHeaderCell("LTP",textAlign = TextAlign.Center)
                TableHeaderCell("Point Diff")
                TableHeaderCell("Stock 1Min")
                TableHeaderCell("Stock OverAll")
                TableHeaderCell("Options 1Min")
                TableHeaderCell("Options OverAll")
                TableHeaderCell("OI 1Min")
                TableHeaderCell("OI Change")
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
                        TableCell(curr.lastUpdated.take(5), textAlign = TextAlign.Start, color = Color.Gray)
                        TableCell(twoDecimalDisplay(curr.ltp).take(5), textAlign = TextAlign.Center, color = setColorForBuyStr(curr.ltp.toDouble(), prev?.ltp ?: curr.ltp))
                        TableCell(curr.pointsChanged.toString(), color = setColorForBuyStr(curr.pointsChanged.toDouble(), prev?.pointsChanged?.toDouble() ?: curr.pointsChanged.toDouble()))
                        TableCell(twoDecimalDisplay(curr.stock1MinChange), color = setColorForBuyStr(curr.stock1MinChange, prev?.stock1MinChange ?: curr.stock1MinChange))
                        TableCell(twoDecimalDisplay(curr.stockOverAllChange), color = setColorForBuyStr(curr.stockOverAllChange, prev?.stockOverAllChange ?: curr.stockOverAllChange))
                        TableCell(twoDecimalDisplay(curr.option1MinChange), color = setColorForBuyStr(curr.option1MinChange, prev?.option1MinChange ?: curr.option1MinChange))
                        TableCell(twoDecimalDisplay(curr.optionOverAllChange), color = setColorForBuyStr(curr.optionOverAllChange, prev?.optionOverAllChange ?: curr.optionOverAllChange))
                        TableCell(twoDecimalDisplay(curr.oi1MinChange), color = setColorForBuyStr(curr.oi1MinChange, prev?.oi1MinChange ?: curr.oi1MinChange))
                        TableCell(twoDecimalDisplay(curr.oiOverAllChange), color = setColorForBuyStr(curr.oiOverAllChange, prev?.oiOverAllChange ?: curr.oiOverAllChange))
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
                .padding(vertical = 2.dp)
                .background(Color(0xFF2196F3)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Time", textAlign = TextAlign.Center)
            TableHeaderCell("LTP ", textAlign = TextAlign.Center)
            TableHeaderCell("1Min Diff")
            TableHeaderCell("Buy  Diff")
            TableHeaderCell("Sell Diff")
            TableHeaderCell("Over All%")
            TableHeaderCell("Buy  Str%")
            TableHeaderCell("Sell Str%")
        }

        Divider(color = Color.Gray, thickness = 1.dp)
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
                    TableCell(curr.lastUpdated.take(5), weight = 1.5f,textAlign = TextAlign.Start, color = Color.Gray)
                    TableCell(oneDecimalDisplay(curr.ltp).take(5), weight = 1.5f, textAlign = TextAlign.Start, color = setColorForBuyStr(curr.ltp, prev?.ltp ?: curr.ltp))
                    TableCell(oneDecimalDisplay(curr.lastMinSentiment), weight = 1.5f,color = setColorForBuyStr(curr.lastMinSentiment, prev?.lastMinSentiment ?: curr.lastMinSentiment))
                    TableCell(oneDecimalDisplay(curr.buyAvg), weight = 1.5f,color = setColorForBuyStr(curr.buyAvg, prev?.buyAvg ?: curr.buyAvg))
                    TableCell(oneDecimalDisplay(curr.sellAvg), weight = 1.5f,color = setColorForSellStr(curr.sellAvg, prev?.sellAvg ?: curr.sellAvg))
                    TableCell(oneDecimalDisplay(curr.overAllSentiment), weight = 1.5f, color = setColorForBuyStr(curr.overAllSentiment, prev?.overAllSentiment ?: curr.overAllSentiment))
                    TableCell(oneDecimalDisplay(curr.stockBuyStr), weight = 1.5f, color = setColorForBuyStr(curr.stockBuyStr, prev?.stockBuyStr ?: curr.stockBuyStr))
                    TableCell(oneDecimalDisplay(curr.stockSellStr), weight = 1.5f,color = setColorForSellStr(curr.stockSellStr, prev?.stockSellStr ?: curr.stockSellStr))

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
                .padding(vertical = 2.dp)
                .background(Color(0xFF2196F3)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Time", textAlign = TextAlign.Center)
            TableHeaderCell("LTP", textAlign = TextAlign.Center)
            TableHeaderCell("1Min Diff")
            TableHeaderCell("Buy  Diff")
            TableHeaderCell("Sell Diff")
            TableHeaderCell("Over All%")
            TableHeaderCell("Buy  Str%")
            TableHeaderCell("Sell Str%")
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
                    TableCell(curr.lastUpdated.take(5), weight = 1.5f, textAlign = TextAlign.Start, color = Color.Gray)
                    TableCell(twoDecimalDisplay(curr.ltp).take(5), weight = 1.5f, color = setColorForBuyStr(curr.ltp, prev?.ltp ?: curr.ltp))
                    TableCell(oneDecimalDisplay(curr.lastMinSentiment), weight = 1.5f,color = setColorForBuyStr(curr.lastMinSentiment, prev?.lastMinSentiment ?: curr.lastMinSentiment))
                    TableCell(oneDecimalDisplay(curr.buyAvg), weight = 1.5f,color = setColorForBuyStr(curr.buyAvg, prev?.buyAvg ?: curr.buyAvg))
                    TableCell(oneDecimalDisplay(curr.sellAvg), weight = 1.5f,color = setColorForSellStr(curr.sellAvg, prev?.sellAvg ?: curr.sellAvg))
                    TableCell(oneDecimalDisplay(curr.overAllSentiment), weight = 1.5f,color = setColorForBuyStr(curr.overAllSentiment, prev?.overAllSentiment ?: curr.overAllSentiment))
                    TableCell(oneDecimalDisplay(curr.optionsBuyStr), weight = 1.5f,color = setColorForBuyStr(curr.optionsBuyStr, prev?.optionsBuyStr ?: curr.optionsBuyStr))
                    TableCell(oneDecimalDisplay(curr.optionsSellStr), weight = 1.5f,color = setColorForSellStr(curr.optionsSellStr, prev?.optionsSellStr ?: curr.optionsSellStr))
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
                    TableCell(twoDecimalDisplay(curr.ltp).take(5), color = setColorForBuyStr(curr.ltp, prev?.ltp ?: curr.ltp))
                    TableCell(convertToCrString(curr.oiQty.toInt()), color = setColorForBuyStr(curr.oiQty.toDouble(), prev?.oiQty?.toDouble() ?: curr.oiQty.toDouble()))
                    TableCell(convertToLacsString(curr.oiChange.toInt()), color = setColorForBuyStr(curr.oiChange.toDouble(), prev?.oiChange?.toDouble() ?: curr.oiChange.toDouble()))
                    TableCell(twoDecimalDisplay(curr.lastMinOIChange), color = setColorForBuyStr(curr.lastMinOIChange, prev?.lastMinOIChange ?: curr.lastMinOIChange))
                    TableCell(twoDecimalDisplay(curr.overAllOIChange), color = setColorForBuyStr(curr.overAllOIChange, prev?.overAllOIChange ?: curr.overAllOIChange))
                }
            }
        }
    }
}


