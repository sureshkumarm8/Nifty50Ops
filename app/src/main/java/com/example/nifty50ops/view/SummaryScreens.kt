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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StockSummary(viewModel: MainViewModel, navController: NavController) {
    val stockSummary by viewModel.stockSummary.collectAsState()
    val time by viewModel.uiState.collectAsState()

    SummaryCard(
        title = "📊 Stocks Summary",
        summaryItems = listOf(
            "Time" to time.currentTime,
            "Buy Avg %" to "%.2f".format(stockSummary.buyAvg),
            "Sell Avg %" to "%.2f".format(stockSummary.sellAvg),
            "BuyStr " to "%.2f".format(stockSummary.buyAvg),
            "SellStr " to "%.2f".format(stockSummary.sellAvg)
        ),
        onClick = {
            navController.navigate("stock_summary_history")
        }
    )
}

@Composable
fun OptionsSummary(viewModel: MainViewModel, navController: NavController) {
    val optionsSummary by viewModel.optionsSummary.collectAsState()
    val time by viewModel.uiState.collectAsState()

    SummaryCard(
        title = "📉 Options Summary",
        summaryItems = listOf(
            "Time" to time.currentTime,
            "Buy Avg %" to "%.2f".format(optionsSummary.buyAvg),
            "Sell Avg %" to "%.2f".format(optionsSummary.sellAvg),
            "BuyStr " to "%.2f".format(optionsSummary.buyAvg),
            "SellStr " to "%.2f".format(optionsSummary.sellAvg)
        ),
        onClick = {
            navController.navigate("options_summary_history")
        }
    )
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
fun OptionsSummaryHistoryScreen(context: Context) {
    val optionsDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(optionsDao)

    var optionsList by remember { mutableStateOf<List<OptionsSummaryEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
//            controller.fetchOptionsData(context)
            repository.getAllOptionsSummary().collectLatest { optionsList = it }
            delay(60 * 1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(WindowInsets.systemBars.asPaddingValues())
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

        OptionsTable(optionsList)
    }
}

@Composable
fun OptionsTable(optionList: List<OptionsSummaryEntity>) {
    LazyColumn {
        items(optionList) { option ->
            val buyColor = when {
                option.optionsBuyStr > 0 -> Color(0xFF2E7D32) // Green
                option.optionsBuyStr < 0 -> Color(0xFFC62828) // Red
                else -> Color.Gray
            }

            val sellColor = when {
                option.optionsSellStr > 0 -> Color(0xFFC62828) // Red
                option.optionsSellStr < 0 -> Color(0xFF2E7D32) // Green
                else -> Color.Gray
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(option.lastUpdated)
                TableCell("%.2f".format(option.buyAvg/ 100000))
                TableCell("%.2f".format(option.sellAvg/ 100000))
                TableCell("%.1f".format(option.optionsBuyStr), color = buyColor)
                TableCell("%.1f".format(option.optionsSellStr), color = sellColor)

            }

            Divider(color = Color.LightGray)
        }
    }
}

@Composable
fun StockSummaryHistoryScreen(context: Context) {
    val stockDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(stockDao)


    var stockList by remember { mutableStateOf<List<StockSummaryEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
//            controller.fetchStockData(context)
            repository.getAllStockSummary().collectLatest { stockList = it }
            delay(60 * 1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(WindowInsets.systemBars.asPaddingValues())
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

        StockTable(stockList)
    }
}

@Composable
fun StockTable(stockList: List<StockSummaryEntity>) {
    LazyColumn {
        items(stockList) { stock ->
            val buyColor = when {
                stock.stockBuyStr > 0 -> Color(0xFF2E7D32) // Green
                stock.stockBuyStr < 0 -> Color(0xFFC62828) // Red
                else -> Color.Gray
            }

            val sellColor = when {
                stock.stockSellStr > 0 -> Color(0xFFC62828) // Red
                stock.stockSellStr < 0 -> Color(0xFF2E7D32) // Green
                else -> Color.Gray
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(stock.lastUpdated)
                TableCell("%.2f".format(stock.buyAvg/ 100000))
                TableCell("%.2f".format(stock.sellAvg/ 100000))
                TableCell("%.1f".format(stock.stockBuyStr), color = buyColor)
                TableCell("%.1f".format(stock.stockSellStr), color = sellColor)
            }

            Divider(color = Color.LightGray)
        }
    }
}
