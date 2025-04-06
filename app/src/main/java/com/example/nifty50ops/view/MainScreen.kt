package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(context: Context) {
    val viewModel = remember { MainViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MainHeader(uiState.currentTime, uiState.niftyPrice)
        Spacer(modifier = Modifier.height(16.dp))
        StockSummary(viewModel)
        Spacer(modifier = Modifier.height(20.dp))
        OptionsSummary(viewModel)
    }
}

@Composable
fun MainHeader(time: String, nifty: Double) {
    Text(
        text = "🗓️ Time: $time",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.DarkGray,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Text(
        text = "📈 Nifty Live : ${String.format("%.0f", nifty)}",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun StockSummary(viewModel: MainViewModel) {
    Text(
        text = "📊 Stocks Summary",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    SummaryRow("Buy %", "${String.format("%.2f", viewModel.getStockBuyPercent())}%")
    SummaryRow("Sell %", "${String.format("%.2f", viewModel.getStockSellPercent())}%")
}

@Composable
fun OptionsSummary(viewModel: MainViewModel) {
    Text(
        text = "📉 Options Summary",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    SummaryRow("Volume Traded", viewModel.getOptionsVol().toString())
    SummaryRow("Buy %", "${String.format("%.2f", viewModel.getOptionsBuyPercent())}%")
    SummaryRow("Sell %", "${String.format("%.2f", viewModel.getOptionsSellPercent())}%")
    SummaryRow("Open Interest", viewModel.getOptionsOI().toString())
    SummaryRow("OI Change", viewModel.getOptionsOIChange().toString())
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SettingsScreen(context: Context) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Settings coming soon 🔧", fontSize = 18.sp)
    }
}

@Composable
fun AboutScreen(context: Context) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Made with 💙 by You\nVersion 1.0", fontSize = 18.sp)
    }
}
