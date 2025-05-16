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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nifty50ops.controller.OptionsController
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.utils.convertToCrString
import com.example.nifty50ops.utils.convertToLacsString
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OptionsScreen(context: Context, navController: NavController) {
    val optionsDao = MarketDatabase.getDatabase(context).marketDao()
    val repository = OptionsRepository(optionsDao)
    val controller = OptionsController(repository)

    var optionsList by remember { mutableStateOf<List<OptionsEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
            repository.getAllOptions().collectLatest { optionsList = it }
            delay(60 * 1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .background(Color(0xFF2196F3)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Name", weight = 1.5f, textAlign = TextAlign.Start)
            TableHeaderCell("Volume", weight = 1.5f, textAlign = TextAlign.Center)
            TableHeaderCell("Buy Qty")
            TableHeaderCell("Sell Qty")
            TableHeaderCell("Buy %")
            TableHeaderCell("Sell %")
            TableHeaderCell("BuyStr %")
            TableHeaderCell("SellStr %")
        }

        Divider(color = Color.Gray, thickness = 1.dp)

        // ✅ pass lambda to navigate on click
        OptionsTable(optionList = optionsList) { optionName ->
            navController.navigate("option_history/$optionName")
        }
    }
}

@Composable
fun OptionsTable(optionList: List<OptionsEntity>, onRowClick: (String) -> Unit) {
    LazyColumn {
        items(optionList) { option ->
            val buyColor = when {
                option.buyDiffPercent > 0 -> Color(0xFF2E7D32)
                option.buyDiffPercent < 0 -> Color(0xFFC62828)
                else -> Color.Gray
            }

            val sellColor = when {
                option.sellDiffPercent > 0 -> Color(0xFFC62828)
                option.sellDiffPercent < 0 -> Color(0xFF2E7D32)
                else -> Color.Gray
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRowClick(option.name) } // ✅ handle click here
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(option.name, color = Color(0xFF2196F3), weight = 1.5f, textAlign = TextAlign.Start)
                TableCell(convertToLacsString(option.volTraded), weight = 1.5f, textAlign = TextAlign.End)
                TableCell(convertToLacsString(option.buyQty), weight = 1.2f)
                TableCell(convertToLacsString(option.sellQty), weight = 1.2f)
                TableCell("%.1f".format(option.buyDiffPercent), color = buyColor)
                TableCell("%.1f".format(option.sellDiffPercent), color = sellColor)
                TableCell("%.1f".format(option.buyStrengthPercent), color = buyColor)
                TableCell("%.1f".format(option.sellStrengthPercent), color = sellColor)
            }

            Divider(color = Color.LightGray)
        }
    }
}

@Composable
fun OptionHistoryScreen(context: Context, optionName: String) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = OptionsRepository(dao)

    var optionHistory by remember { mutableStateOf<List<OptionsEntity>>(emptyList()) }
    val listState = rememberLazyListState()

    LaunchedEffect(optionName) {
        repository.getOptionHistory(optionName).collectLatest { newList ->
            optionHistory = newList

            // Wait for LazyColumn to recompute its size
            snapshotFlow { listState.layoutInfo.totalItemsCount }
                .filter { it > 0 }
                .first()

            // Scroll to the bottom item
            listState.animateScrollToItem(newList.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2196F3))
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeaderCell("Time")
            TableHeaderCell("Buy Qty")
            TableHeaderCell("Sell Qty")
            TableHeaderCell("Buy %")
            TableHeaderCell("Sell %")
            TableHeaderCell("BuyStr %")
            TableHeaderCell("SellStr %")
        }

        Divider(color = Color.Gray, thickness = 1.dp)

        LazyColumn(state = listState) {
            items(optionHistory) { option ->
                val timeStr = option.timestamp
                val buyColor = when {
                    option.buyDiffPercent > 0 -> Color(0xFF2E7D32)
                    option.buyDiffPercent < 0 -> Color(0xFFC62828)
                    else -> Color.Gray
                }
                val sellColor = when {
                    option.sellDiffPercent > 0 -> Color(0xFFC62828)
                    option.sellDiffPercent < 0 -> Color(0xFF2E7D32)
                    else -> Color.Gray
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(timeStr)
                    TableCell(convertToLacsString(option.buyQty))
                    TableCell(convertToLacsString(option.sellQty))
                    TableCell("%.1f".format(option.buyDiffPercent), color = buyColor)
                    TableCell("%.1f".format(option.sellDiffPercent), color = sellColor)
                    TableCell("%.1f".format(option.buyStrengthPercent), color = buyColor)
                    TableCell("%.1f".format(option.sellStrengthPercent), color = sellColor)
                }

                Divider(color = Color.LightGray)
            }
        }
    }
}


