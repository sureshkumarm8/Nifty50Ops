package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nifty50ops.controller.OptionsController
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.repository.OptionsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OptionsScreen(context: Context) {
    val optionsDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = OptionsRepository(optionsDao)
    val controller = OptionsController(repository)

    var optionsList by remember { mutableStateOf<List<OptionsEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
//            controller.fetchOptionsData(context)
            repository.getAllOptions().collectLatest { optionsList = it }
            delay(60 * 1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
//        Text(
//            text = "📈 Weekly Nifty 50 Options",
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.primary,
//            modifier = Modifier.padding(vertical = 12.dp)
//        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Name", weight = 2f)
            TableHeaderCell("Volume")
            TableHeaderCell("Buy Qty")
            TableHeaderCell("Sell Qty")
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
fun OptionsTable(optionList: List<OptionsEntity>) {
    LazyColumn {
        items(optionList) { option ->
            val buyColor = when {
                option.buyDiffPercent > 0 -> Color(0xFF2E7D32) // Green
                option.buyDiffPercent < 0 -> Color(0xFFC62828) // Red
                else -> Color.Gray
            }

            val sellColor = when {
                option.sellDiffPercent > 0 -> Color(0xFFC62828) // Red
                option.sellDiffPercent < 0 -> Color(0xFF2E7D32) // Green
                else -> Color.Gray
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(option.name, color = Color.Blue, weight = 2f)
                TableCell("%.2f".format(option.volTraded.toDouble() / 100000))
                TableCell("%.2f".format(option.buyQty.toDouble() / 100000))
                TableCell("%.2f".format(option.sellQty.toDouble() / 100000))
                TableCell("%.0f%%".format(option.buyDiffPercent), color = buyColor)
                TableCell("%.0f%%".format(option.sellDiffPercent), color = sellColor)
                TableCell("%.0f%%".format(option.buyStrengthPercent), color = buyColor)
                TableCell("%.0f%%".format(option.sellStrengthPercent), color = sellColor)
            }

            Divider(color = Color.LightGray)
        }
    }
}
