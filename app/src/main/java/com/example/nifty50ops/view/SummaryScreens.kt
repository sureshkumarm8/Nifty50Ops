package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.model.MarketInsightEntity
import com.example.nifty50ops.model.MarketsEntity
import com.example.nifty50ops.model.OptionsSummaryEntity
import com.example.nifty50ops.model.SentimentSummaryEntity
import com.example.nifty50ops.model.StockSummaryEntity
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.utils.convertToCrString
import com.example.nifty50ops.utils.convertToLacsString
import com.example.nifty50ops.utils.oneDecimalDisplay
import com.example.nifty50ops.utils.setColorForBuy
import com.example.nifty50ops.utils.setColorForBuyStr
import com.example.nifty50ops.utils.setColorForSell
import com.example.nifty50ops.utils.setColorForSellStr
import com.example.nifty50ops.utils.twoDecimalDisplay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun SentimentSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

    var sentimentList by remember { mutableStateOf<List<SentimentSummaryEntity>>(emptyList()) }

    // Collect sentiment summary data
    LaunchedEffect(true) {
        repository.getLastSentimentSummary().collectLatest { newList ->
            sentimentList = newList.sortedBy { it.lastUpdated }
        }
    }

    if (sentimentList.isNotEmpty()) {
        val curr = sentimentList.last()
        val prev = sentimentList.getOrNull(sentimentList.lastIndex - 1)

        SummaryCard(
            title = "📊 Sentiment Summary",
            summaryItems = listOf(
                "PtsDiff" to "%.2f".format(curr.pointsChanged.toDouble()),
                "StAll" to "%.2f".format(curr.stockOverAllChange),
                "St1" to "%.2f".format(curr.stock1MinChange),
                "OpAll" to "%.2f".format(curr.optionOverAllChange),
                "Op1" to "%.2f".format(curr.option1MinChange),
                "OIChange" to "%.2f".format(curr.oiOverAllChange),
                "OI1" to "%.2f".format(curr.oi1MinChange)
            ),
            colorOverrides = listOf(
                setColorForBuyStr(curr.pointsChanged.toDouble(), prev?.pointsChanged?.toDouble() ?: curr.pointsChanged.toDouble()),
                setColorForBuyStr(curr.stockOverAllChange, prev?.stockOverAllChange ?: curr.stockOverAllChange),
                setColorForBuyStr(curr.stock1MinChange, prev?.stock1MinChange ?: curr.stock1MinChange),
                setColorForBuyStr(curr.optionOverAllChange, prev?.optionOverAllChange ?: curr.optionOverAllChange),
                setColorForBuyStr(curr.option1MinChange, prev?.option1MinChange ?: curr.option1MinChange),
                setColorForBuyStr(curr.oiOverAllChange, prev?.oiOverAllChange ?: curr.oiOverAllChange),
                setColorForBuyStr(curr.oi1MinChange, prev?.oi1MinChange ?: curr.oi1MinChange)

            ),
            onClick = {
                navController.navigate("sentiment_summary_history")
            }
        )
    }
}

@Composable
fun StockSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

//    var stockSummary by remember { mutableStateOf<StockSummaryEntity?>(null) }
    var stockSummary by remember { mutableStateOf<List<StockSummaryEntity>>(emptyList()) }

    LaunchedEffect(true) {
        repository.getAllStockSummary().collect { summaries ->
            stockSummary = summaries
        }
    }

    if (stockSummary.isNotEmpty()) {
        val curr = stockSummary.last()
        val prev = stockSummary.getOrNull(stockSummary.lastIndex - 1)
        SummaryCard(
            title = "📊 Stocks Summary",
            summaryItems = listOf(
                "Time" to curr.lastUpdated,
                "OverAll" to "%.1f".format(curr.overAllSentiment),
                "BuyStr" to "%.1f".format(curr.stockBuyStr),
                "SellStr" to "%.1f".format(curr.stockSellStr),
                "LastMin" to "%.1f".format(curr.lastMinSentiment),
                "LTPStr" to "%.1f".format(curr.ltpOverall),
                "LTP1Min" to "%.1f".format(curr.ltpLastMin)
            ),
            onClick = {
                navController.navigate("stock_summary_history")
            },
            colorOverrides = listOf(
                Color.Black,
                setColorForBuyStr(curr.overAllSentiment, prev?.overAllSentiment ?: curr.overAllSentiment),
                setColorForBuyStr(curr.stockBuyStr, prev?.stockBuyStr ?: curr.stockBuyStr),
                setColorForSellStr(curr.stockSellStr, prev?.stockSellStr ?: curr.stockSellStr),
                setColorForBuy(curr.lastMinSentiment),
                setColorForBuy(curr.buyAvg),
                setColorForSell(curr.sellAvg)
            )
        )
    }
}

@Composable
fun OptionsSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

//    var optionsSummary by remember { mutableStateOf<OptionsSummaryEntity?>(null) }
    var optionsSummary by remember { mutableStateOf<List<OptionsSummaryEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.getAllOptionsSummary().collect { summary ->
            optionsSummary = summary
        }
    }

    if (optionsSummary.isNotEmpty()) {
        val curr = optionsSummary.last()
        val prev = optionsSummary.getOrNull(optionsSummary.lastIndex - 1)
        SummaryCard(
            title = "📉 Options Summary",
            summaryItems = listOf(
                "Time" to curr.lastUpdated,
                "OverAll" to "%.1f".format(curr.overAllSentiment),
                "BuyStr " to "%.1f".format(curr.optionsBuyStr),
                "SellStr " to "%.1f".format(curr.optionsSellStr),
                "LastMin" to "%.1f".format(curr.lastMinSentiment),
                "Buy Avg %" to "%.1f".format(curr.buyAvg),
                "Sell Avg %" to "%.1f".format(curr.sellAvg),
            ),
            onClick = {
                navController.navigate("options_summary_history")
            },
            colorOverrides = listOf(
                Color.Black,
                setColorForBuyStr(curr.overAllSentiment, prev?.overAllSentiment ?: curr.overAllSentiment),
                setColorForBuyStr(curr.optionsBuyStr, prev?.optionsBuyStr ?: curr.optionsBuyStr),
                setColorForSellStr(curr.optionsSellStr, prev?.optionsSellStr ?: curr.optionsSellStr),
                setColorForBuy(curr.lastMinSentiment),
                setColorForBuy(curr.buyAvg),
                setColorForSell(curr.sellAvg)
            )
        )
    }
}

@Composable
fun GenAIInsightSummary(context: Context, navController: NavController) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val repository = MarketRepository(dao)

    var latestInsight by remember { mutableStateOf<MarketInsightEntity?>(null) }

    LaunchedEffect(Unit) {
        repository.getLatestMarketInsight().collect { insight ->
            latestInsight = insight
        }
    }

    latestInsight?.let { insight ->
        // Extract all sections
        val momentum = extractSection(insight.gen_ai_insights ?: "", "Immediate Momentum")
        val volatility = extractSection(insight.gen_ai_insights ?: "", "Volatility & Unusual Activity")
        val reversal = extractSection(insight.gen_ai_insights ?: "", "Reversal / Breakout Alerts")
        val entryTriggers = extractSection(insight.gen_ai_insights ?: "", "Entry Triggers")
        val exitCues = extractSection(insight.gen_ai_insights ?: "", "Exit Cues & Warnings")
        val recommendedSide = extractSection(insight.gen_ai_insights ?: "", "Recommended Side")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("market_live_gen_ai_analysis") }
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "✨ GenAI Insight - ${insight.timestamp}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF673AB7),
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LabelValueRow("Momentum", momentum.ifBlank { "N/A" })
                LabelValueRow("Volatility", volatility.ifBlank { "N/A" })
                LabelValueRow("Reversal", reversal.ifBlank { "N/A" })
                LabelValueRow("Entry", entryTriggers.ifBlank { "N/A" })
                LabelValueRow("Exit", exitCues.ifBlank { "N/A" })
                LabelValueRowWithColor("Side", recommendedSide.ifBlank { "N/A" })
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    summaryItems: List<Pair<String, String>>,
    onClick: (() -> Unit)? = null,
    colorOverrides: List<Color> = emptyList()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBBDEFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                summaryItems.forEachIndexed { index, (label, value) ->
                    val valueColor = colorOverrides.getOrNull(index) ?: Color.Black
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
                            color = valueColor
                        )
                    }
                }
            }
        }
    }
}

fun extractSection(text: String, sectionTitle: String): String {
    val pattern = """
        (?m)^#\s*$sectionTitle\s*[\r\n]+(.*?)(?=^#\s|\z)
    """.trimIndent()
    val regex = Regex(pattern, RegexOption.DOT_MATCHES_ALL)
    val match = regex.find(text)
    return match?.groups?.get(1)?.value
        ?.trim()
        ?.replace(Regex("""\s+"""), " ") // optional: collapse extra spaces
        ?: ""
}

@Composable
fun LabelValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top // align texts at top
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            modifier = Modifier
                .weight(0.3f)   // about 30% width for label
                .padding(end = 4.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.7f)  // about 70% width for value, allows wrapping
        )
    }
}

@Composable
fun LabelValueRowWithColor(label: String, value: String) {
    val valueColor = when {
        value.contains("Long", ignoreCase = true) -> Color(0xFF388E3C)  // Green
        value.contains("Short", ignoreCase = true) -> Color(0xFFD32F2F)  // Red
        value.contains("Wait", ignoreCase = true) -> Color(0xFF757575)   // Grey
        else -> Color.Black
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            modifier = Modifier
                .weight(0.3f)
                .padding(end = 4.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.7f)
        )
    }
}



