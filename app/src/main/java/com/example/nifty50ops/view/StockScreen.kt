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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.nifty50ops.utils.convertToLacsString
import com.example.nifty50ops.utils.setColorForBuy
import com.example.nifty50ops.utils.setColorForBuyStr
import com.example.nifty50ops.utils.setColorForSell
import com.example.nifty50ops.utils.setColorForSellStr
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
                .background(Color(0xFF2196F3)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Name", weight = 2f, textAlign = TextAlign.Start)
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
    val enrichedList = remember(stockList) {
        stockList.mapIndexed { index, curr ->
            val prev = stockList.getOrNull(index - 1)
            StocksWithPrevious(curr, prev)
        }
    }
    LazyColumn {
        items(enrichedList) { item ->
            val curr = item.current
            val prev = item.prev
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRowClick(curr.name) }
                    .padding(vertical = 2.dp),

                verticalAlignment = Alignment.CenterVertically,
            ) {
                TableCell(curr.name, color = Color(0xFF2196F3), weight = 2f, textAlign = TextAlign.Start)
                TableCell("%.0f".format(curr.ltp), fontSize = 12.sp, color = setColorForBuyStr(curr.ltp, prev?.ltp ?: curr.ltp))
                TableCell(convertToLacsString(curr.buyQty), color = setColorForBuyStr(curr.buyQty.toDouble(), prev?.buyQty?.toDouble() ?: curr.buyQty.toDouble()))
                TableCell(convertToLacsString(curr.sellQty), color = setColorForSellStr(curr.sellQty.toDouble(), prev?.sellQty?.toDouble() ?: curr.sellQty.toDouble()))
                TableCell("%.1f".format(curr.buyDiffPercent), color = setColorForBuyStr(curr.buyDiffPercent, prev?.buyDiffPercent ?: curr.buyDiffPercent))
                TableCell("%.1f".format(curr.sellDiffPercent), color = setColorForSellStr(curr.sellDiffPercent, prev?.sellDiffPercent ?: curr.sellDiffPercent))
                TableCell("%.1f".format(curr.buyStrengthPercent), color = setColorForBuyStr(curr.buyStrengthPercent, prev?.buyStrengthPercent ?: curr.buyStrengthPercent))
                TableCell("%.1f".format(curr.sellStrengthPercent), color = setColorForSellStr(curr.sellStrengthPercent, prev?.sellStrengthPercent ?: curr.sellStrengthPercent))
            }

            Divider(color = Color.LightGray)

        }
    }
}

@Composable
fun RowScope.TableHeaderCell(text: String, weight: Float = 1f, textAlign: TextAlign = TextAlign.End) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(6.dp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.labelLarge,
        textAlign = textAlign
    )
}

@Composable
fun RowScope.TableCell(
    text: String,
    color: Color = Color.Unspecified,
    weight: Float = 1f,
    fontSize: TextUnit = 12.sp,
    textAlign: TextAlign = TextAlign.End
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(6.dp),
        color = color,
        fontSize = fontSize,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = textAlign
    )
}

data class StocksWithPrevious(
    val current: StockEntity,
    val prev: StockEntity?
)
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
                .background(Color(0xFF2196F3)),
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
        val enrichedList = remember(stockList) {
            stockList.mapIndexed { index, curr ->
                val prev = stockList.getOrNull(index - 1)
                StocksWithPrevious(curr, prev)
            }
        }
        LazyColumn(state = listState) {
            items(enrichedList) { item ->
                val curr = item.current
                val prev = item.prev
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(curr.timestamp)
                    TableCell(curr.ltp.toString(), color = setColorForBuyStr(curr.ltp, prev?.ltp ?: curr.ltp))
                    TableCell(convertToLacsString(curr.buyQty), color = setColorForBuyStr(curr.buyQty.toDouble(), prev?.buyQty?.toDouble() ?: curr.buyQty.toDouble()))
                    TableCell(convertToLacsString(curr.sellQty), color = setColorForSellStr(curr.sellQty.toDouble(), prev?.sellQty?.toDouble() ?: curr.sellQty.toDouble()))
                    TableCell("%.1f".format(curr.buyDiffPercent), color = setColorForBuyStr(curr.buyDiffPercent, prev?.buyDiffPercent ?: curr.buyDiffPercent))
                    TableCell("%.1f".format(curr.sellDiffPercent), color = setColorForSellStr(curr.sellDiffPercent, prev?.sellDiffPercent ?: curr.sellDiffPercent))
                    TableCell("%.1f".format(curr.buyStrengthPercent), color = setColorForBuyStr(curr.buyStrengthPercent, prev?.buyStrengthPercent ?: curr.buyStrengthPercent))
                    TableCell("%.1f".format(curr.sellStrengthPercent), color = setColorForSellStr(curr.sellStrengthPercent, prev?.sellStrengthPercent ?: curr.sellStrengthPercent))
                }

                Divider(color = Color.LightGray)
            }
        }
    }
}
