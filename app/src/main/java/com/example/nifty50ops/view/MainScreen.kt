package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(context: Context) {
    val viewModel = remember { MainViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        MainHeader(uiState.currentTime, uiState.niftyPrice)
        Spacer(modifier = Modifier.height(12.dp))
        StockSummary(viewModel)
        Spacer(modifier = Modifier.height(12.dp))
        OptionsSummary(viewModel)
        Spacer(modifier = Modifier.height(20.dp))
        SnapshotSection("📦 Stocks Snapshot") { StockSnapshot(context) }
        Spacer(modifier = Modifier.height(16.dp))
        SnapshotSection("🧾 Options Snapshot") { OptionsSnapshot(context) }
    }
}

@Composable
fun MainHeader(time: String, nifty: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Time on the left
        Text(
            text = "🗓️ $time",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // Nifty at center
        Surface(
            color = Color(0x332196F3), // Light translucent blue background
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "📈 Nifty: ${String.format("%.0f", nifty)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun StockSummary(viewModel: MainViewModel) {
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
        )
    )
}

@Composable
fun OptionsSummary(viewModel: MainViewModel) {
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
        )
    )
}

fun formatLakhs(value: Int): String {
    return if (value >= 100000) "${"%.2f".format(value / 100000.0)}L" else value.toString()
}

@Composable
fun SummaryCard(title: String, summaryItems: List<Pair<String, String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(bottom = 5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
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
fun SnapshotSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@Composable
fun StockSnapshot(context: Context) {
    val stockDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = StockRepository(stockDao)
    val stockList by produceState(initialValue = emptyList<StockEntity>()) {
        repository.getAllStocks().collectLatest { value = it }
    }

    SnapshotCard(
        headers = listOf("Name", "LTP", "Buy", "Sell"),
        rows = stockList.map {
            listOf(it.name, it.ltp.toInt().toString(), it.buyQty.toString(), it.sellQty.toString())
        }
    )
}

@Composable
fun OptionsSnapshot(context: Context) {
    val optionsDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = OptionsRepository(optionsDao)
    val optionsList by produceState(initialValue = emptyList<OptionsEntity>()) {
        repository.getAllOptions().collectLatest { value = it }
    }

    SnapshotCard(
        headers = listOf("Name", "Vol", "Buy", "Sell"),
        rows = optionsList.map {
            listOf(
                it.name,
                formatLakhs(it.volTraded),
                formatLakhs(it.buyQty),
                formatLakhs(it.sellQty)
            )
        }
    )
}

@Composable
fun SnapshotCard(headers: List<String>, rows: List<List<String>>) {
    val cardColor = Color(0xFFBBDEFB) // lighter version of 0xFF2196F3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                }
            }

            Divider(color = Color.Gray, thickness = 1.dp)

            // Scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
            ) {
                items(rows) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { cell ->
                            Text(
                                text = cell,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(context: Context) {
    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings coming soon 🔧", fontSize = 18.sp)
    }
}

@Composable
fun AboutScreen(context: Context) {
    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Made with 💙 by You\nVersion 1.0", fontSize = 18.sp)
    }
}
