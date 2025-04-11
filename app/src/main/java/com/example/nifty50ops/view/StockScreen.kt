package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nifty50ops.controller.StockController
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.model.StockSummaryEntity
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.repository.StockRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun StockScreen(context: Context, navController: NavController) {
    val stockDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = StockRepository(stockDao)
    val controller = StockController(repository)

    var stockList by remember { mutableStateOf<List<StockEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
//            controller.fetchStockData(context)
            repository.getAllStocks().collectLatest { stockList = it }
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
            TableHeaderCell("Name", weight = 2f)
            TableHeaderCell("LTP")
            TableHeaderCell("Buy Qty")
            TableHeaderCell("Sell Qty")
            TableHeaderCell("Buy %")
            TableHeaderCell("Sell %")
            TableHeaderCell("BuyStr %")
            TableHeaderCell("SellStr %")
        }

        Divider(color = Color.Gray, thickness = 1.dp)
        StockTable(stockList = stockList) { stockName ->
            navController.navigate("stock_history/$stockName")
        }
//        StockTable(stockList)
    }
}

@Composable
fun StockTable(stockList: List<StockEntity>, onRowClick: (String) -> Unit) {
    LazyColumn {
        items(stockList) { stock ->
            val buyColor = when {
                stock.buyDiffPercent > 0 -> Color(0xFF2E7D32) // Green
                stock.buyDiffPercent < 0 -> Color(0xFFC62828) // Red
                else -> Color.Gray
            }

            val sellColor = when {
                stock.sellDiffPercent > 0 -> Color(0xFFC62828) // Red
                stock.sellDiffPercent < 0 -> Color(0xFF2E7D32) // Green
                else -> Color.Gray
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRowClick(stock.name) }
                    .padding(vertical = 2.dp),

                verticalAlignment = Alignment.CenterVertically,
            ) {
                TableCell(stock.name, color = Color.Blue, weight = 2f)
                TableCell("%.0f".format(stock.ltp), fontSize = 12.sp)
                TableCell("%.2f".format(stock.buyQty.toDouble() / 100000))
                TableCell("%.2f".format(stock.sellQty.toDouble() / 100000))
                TableCell("%.1f".format(stock.buyDiffPercent), color = buyColor)
                TableCell("%.1f".format(stock.sellDiffPercent), color = sellColor)
                TableCell("%.1f".format(stock.buyStrengthPercent), color = buyColor)
                TableCell("%.1f".format(stock.sellStrengthPercent), color = sellColor)
            }

            Divider(color = Color.LightGray)

        }
    }
}

@Composable
fun RowScope.TableHeaderCell(text: String, weight: Float = 1f) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(6.dp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun RowScope.TableCell(text: String, color: Color = Color.Unspecified, weight: Float = 1f, fontSize: TextUnit = 12.sp) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(6.dp),
        color = color,
        fontSize = fontSize,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
fun StockHistoryScreen(context: Context, stockName: String) {
    val stockDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = StockRepository(stockDao)

    var stockList by remember { mutableStateOf<List<StockEntity>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(stockName) {
        repository.getStockHistory(stockName).collectLatest { newList ->
            stockList = newList

            // Wait until LazyColumn is recomposed
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .filter { it > 0 }
                .first()

            // Scroll to last item
            listState.animateScrollToItem(newList.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            TableHeaderCell("Buy Qty")
            TableHeaderCell("Sell Qty")
            TableHeaderCell("Buy %")
            TableHeaderCell("Sell %")
            TableHeaderCell("BuyStr %")
            TableHeaderCell("SellStr %")
        }

        Divider(color = Color.Gray, thickness = 1.dp)

        LazyColumn(state = listState) {
            items(stockList) { stock ->
                val buyColor = when {
                    stock.buyDiffPercent > 0 -> Color(0xFF2E7D32)
                    stock.buyDiffPercent < 0 -> Color(0xFFC62828)
                    else -> Color.Gray
                }

                val sellColor = when {
                    stock.sellDiffPercent > 0 -> Color(0xFFC62828)
                    stock.sellDiffPercent < 0 -> Color(0xFF2E7D32)
                    else -> Color.Gray
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(stock.timestamp)
                    TableCell(stock.ltp.toString())
                    TableCell(stock.buyQty.toString())
                    TableCell(stock.sellQty.toString())
                    TableCell("%.1f".format(stock.buyDiffPercent), color = buyColor)
                    TableCell("%.1f".format(stock.sellDiffPercent), color = sellColor)
                    TableCell("%.1f".format(stock.buyStrengthPercent), color = buyColor)
                    TableCell("%.1f".format(stock.sellStrengthPercent), color = sellColor)
                }

                Divider(color = Color.LightGray)
            }
        }
    }
}