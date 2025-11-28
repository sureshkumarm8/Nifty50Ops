package com.example.nifty50ops.ui.screens

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import com.example.nifty50ops.utils.ExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ExportDataScreen(context: Context = LocalContext.current) {
    val dao = MarketDatabase.getDatabase(context).marketDao()
    val stockRepo = remember { StockRepository(dao) }
    val optionRepo = remember { OptionsRepository(dao) }
    val marketRepo = remember { MarketRepository(dao) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Export Market Data", style = MaterialTheme.typography.headlineSmall)

        ExportActionButton("Export Stock Table") {
            scope.launch(Dispatchers.IO) {
                ExportHelper.exportStockTable(context, stockRepo)
                showToast(context, "Stock table exported successfully.")
            }
        }

        ExportActionButton("Export Options Table") {
            scope.launch(Dispatchers.IO) {
                ExportHelper.exportOptionsTable(context, optionRepo)
                showToast(context, "Options table exported successfully.")
            }
        }

        ExportActionButton("Export Sentiment Summary") {
            scope.launch(Dispatchers.IO) {
                ExportHelper.exportSentimentSummary(context, marketRepo)
                showToast(context, "Sentiment summary exported successfully.")
            }
        }

        ExportActionButton("Export Stock Summary") {
            scope.launch(Dispatchers.IO) {
                ExportHelper.exportStockSummary(context, marketRepo)
                showToast(context, "Stock summary exported successfully.")
            }
        }

        ExportActionButton("Export Options Summary") {
            scope.launch(Dispatchers.IO) {
                ExportHelper.exportOptionsSummary(context, marketRepo)
                showToast(context, "Options summary exported successfully.")
            }
        }

        ExportActionButton("Export ALL Tables") {
            scope.launch(Dispatchers.IO) {
                ExportHelper.exportAllTablesToExcel(context, stockRepo, optionRepo, marketRepo)
                showToast(context, "All tables exported successfully.")
            }
        }

        ExportActionButton("Copy All Exported Files to Downloads") {
            scope.launch(Dispatchers.IO) {
                ExportHelper.copyAllFilesToDownloads(context)
                withContext(Dispatchers.Main) {
                    showToast(context, "All files copied to Downloads.")
                }
            }
        }

    }
}

@Composable
fun ExportActionButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun showToast(context: Context, message: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}


