package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import com.example.nifty50ops.utils.convertToLacsString
import kotlinx.coroutines.flow.collectLatest

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
        headers = listOf("Name", "Buy", "Sell", "Buy%", "Sell%", "BuyStr%", "SellStr%"),
        rows = stockList.map {
            listOf(it.name.take(5), it.buyQty.toString(), it.sellQty.toString(),it.buyDiffPercent.toString(),it.sellDiffPercent.toString(),it.buyStrengthPercent.toString(),it.sellStrengthPercent.toString())
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
        headers = listOf("Name", "Buy", "Sell", "Buy%", "Sell%", "BuyStr%", "SellStr%"),
        rows = optionsList.map {
            listOf(
                it.name.take(6),
                convertToLacsString(it.buyQty),
                convertToLacsString(it.sellQty),
                it.buyDiffPercent.toString(),
                it.sellDiffPercent.toString(),
                it.buyStrengthPercent.toString(),
                it.sellStrengthPercent.toString()
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
