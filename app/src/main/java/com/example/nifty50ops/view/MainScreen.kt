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
import androidx.navigation.NavController
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(context: Context, navController: NavController) {
    val viewModel = remember { MainViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            MainHeader(uiState.currentTime, uiState.niftyPrice)
            Spacer(modifier = Modifier.height(12.dp))
            SentimentSummary(context, navController)
            Spacer(modifier = Modifier.height(12.dp))
            StockSummary(context, navController)
            Spacer(modifier = Modifier.height(12.dp))
            OptionsSummary(context, navController)
            Spacer(modifier = Modifier.height(12.dp))
            OISummary(context, navController)
            Spacer(modifier = Modifier.height(20.dp))
            SnapshotSection("📦 Stocks Snapshot") { StockSnapshot(context) }
            Spacer(modifier = Modifier.height(16.dp))
            SnapshotSection("🧾 Options Snapshot") { OptionsSnapshot(context) }
        }
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
fun SettingsScreen(context: Context) {
    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings coming soon 🔧", fontSize = 18.sp)
    }
}

