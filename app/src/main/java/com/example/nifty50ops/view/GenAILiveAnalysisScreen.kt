package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.MarketInsightEntity
import com.example.nifty50ops.repository.MarketRepository

@Composable
fun MarketLiveGenAIAnalysisScreen(context: Context, intervalTM: String) {
    val dao = remember { MarketDatabase.getDatabase(context).marketDao() }
    val repository = remember { MarketRepository(dao) }

    var insightsList by remember { mutableStateOf(listOf<MarketInsightEntity>()) }
    val listState = rememberLazyListState()

    LaunchedEffect(intervalTM) {
        repository.getMarketInsightsByInterval(intervalTM).collect { list ->
            insightsList = list.sortedByDescending { it.timestamp }
        }
    }
    println("GenAI insightsList: " + insightsList.size)
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(insightsList) { insight ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header
                        Text(
                            text = "🕒 ${insight.timestamp}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "📈 ${insight.name} - ${insight.ltp} (${insight.pointsChanged})",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // <-- GenAI Insight here if available -->
                        insight.gen_ai_insights?.takeIf { it.isNotBlank() }?.let { genAIText ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✨ GenAI Insight:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = genAIText.trim(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Top 5 Stocks
                        Text("📊 Top 5 Stocks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        val stockLines = insight.top5StockFluctuations.trim().split("\n")
                        stockLines.forEach { line ->
                            Text("• $line", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Top 5 Options
                        Text("📉 Top 5 Options", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        val optionLines = insight.top5OptionFluctuations.trim().split("\n")
                        optionLines.forEach { line ->
                            Text("• $line", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sentiment Insights
                        Text("🧠 Sentiment Insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        val sentimentLines = insight.tradingHints.trim().split("\n")
                        sentimentLines.forEach { line ->
                            Text(line, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}



