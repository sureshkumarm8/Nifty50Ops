package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nifty50ops.database.MarketDao
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.StockEntity
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.model.SentimentSummaryEntity
import com.example.nifty50ops.repository.MarketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MarketReviewScreen(context: Context ,navController: NavController) {
    val context = LocalContext.current
    val dao = remember { MarketDatabase.getDatabase(context).marketDao() }
    val repository = remember { MarketRepository(dao) }
    val marketReviews = remember { mutableStateListOf<MarketsEntity>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            repository.getAllData().collectLatest {
                marketReviews.clear()
                marketReviews.addAll(it)
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(marketReviews) { review ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🕒 ${review.timestamp}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "📈 ${review.name} - ${review.ltp} (${review.pointsChanged})",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(review.summary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}


