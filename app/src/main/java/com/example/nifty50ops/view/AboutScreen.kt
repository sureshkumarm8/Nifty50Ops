package com.example.nifty50ops.view

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "ℹ️ About This App",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nifty 50 OPS — Market Insights & Analytics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
//            Text(
//                text = "Version 1.0.0\nDeveloped by Suresh Kumar M (sureshkumarM8@gmail.com)",
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
//            )

            Divider()

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🚀 Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = """
                • Live Nifty 50 Stock Updates with LTP, buy/sell strength, sentiment trends.
                • Options Summary with OI, OI change, buy/sell strength analysis.
                • Historical Stock, Options, Sentiment, and OI Summary views with 1min, 5min, 10min, 15min intervals.
                • Market Overview screen highlighting top movers and sentiment insights.
                • Auto-generated trading hints based on live buy/sell & OI trends.
                • CSV Export option to download and analyze your data.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⚙️ How it works",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = """
                • App fetches live stock & options data every minute during market hours.
                • Data is stored locally and aggregated into multiple intervals.
                • Provides visual insights and trends using your own historical data.
                • Helps you analyze market behavior & sentiment patterns.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⚠️ Disclaimer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = """
                This app is built by Suresh Kumar M for personal market analysis and learning purposes only. 
                It is not intended for commercial use or for providing any kind of financial advice.
                Please use it responsibly and always do your own research before making any trading decisions.
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Footer
        Text(
            text = "Made with ❤️ by Suresh Kumar M",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}

