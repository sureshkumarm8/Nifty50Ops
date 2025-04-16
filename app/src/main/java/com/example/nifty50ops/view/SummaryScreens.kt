package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.model.StockSummaryEntity
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import com.example.nifty50ops.utils.twoDecimalDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

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
                "BuyStr " to "%.2f".format(summary.stockBuyStr),
                "SellStr " to "%.2f".format(summary.stockSellStr)
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
                "BuyStr " to "%.2f".format(summary.optionsBuyStr),
                "SellStr " to "%.2f".format(summary.optionsSellStr)
            ),
            onClick = {
                navController.navigate("options_summary_history")
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
            TableHeaderCell("Buy %")
            TableHeaderCell("Sell %")
            TableHeaderCell("BuyStr %")
            TableHeaderCell("SellStr %")
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
                    TableCell(twoDecimalDisplay(item.buyAvg), color = buyColor)
                    TableCell(twoDecimalDisplay(item.sellAvg), color = sellColor)
                    TableCell(twoDecimalDisplay(item.stockBuyStr), color = buyColor)
                    TableCell(twoDecimalDisplay(item.stockSellStr), color = sellColor)
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
            TableHeaderCell("Buy %")
            TableHeaderCell("Sell %")
            TableHeaderCell("BuyStr %")
            TableHeaderCell("SellStr %")
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
                    TableCell(twoDecimalDisplay(item.buyAvg), color = buyColor)
                    TableCell(twoDecimalDisplay(item.sellAvg), color = sellColor)
                    TableCell(twoDecimalDisplay(item.optionsBuyStr), color = buyColor)
                    TableCell(twoDecimalDisplay(item.optionsSellStr), color = sellColor)
                }
            }
        }
    }
}


