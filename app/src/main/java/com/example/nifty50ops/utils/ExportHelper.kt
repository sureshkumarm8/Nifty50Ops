package com.example.nifty50ops.utils


import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import com.opencsv.CSVWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExportHelper {

    fun exportStockTable(context: Context, repo: StockRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            val rows = CSVDataFormatter.formatStocks(repo.getAllStocksExport())
            exportToCSV(context, "stock_table.csv", rows)
        }
    }

    fun exportOptionsTable(context: Context, repo: OptionsRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            val rows = CSVDataFormatter.formatOptions(repo.getAllOptionsExport())
            exportToCSV(context, "options_table.csv", rows)
        }
    }

    fun exportStockSummary(context: Context, repo: MarketRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            val rows = CSVDataFormatter.formatStockSummary(repo.getAllStockSummary())
            exportToCSV(context, "stock_summary.csv", rows)
        }
    }

    fun exportOptionsSummary(context: Context, repo: MarketRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            val rows = CSVDataFormatter.formatOptionsSummary(repo.getAllOptionsSummary())
            exportToCSV(context, "options_summary.csv", rows)
        }
    }

    fun exportAllTables(
        context: Context,
        stockRepo: StockRepository,
        optionRepo: OptionsRepository,
        marketRepo: MarketRepository
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val time = timeStamp()
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "$time-AllTables.csv"
            )
            val writer = CSVWriter(FileWriter(file))

            writer.writeNext(arrayOf("=== Stock Table ==="))
            CSVDataFormatter.formatStocks(stockRepo.getAllStocks()).forEach { writer.writeNext(it.toTypedArray()) }

            writer.writeNext(arrayOf("")) // Blank line
            writer.writeNext(arrayOf("=== Options Table ==="))
            CSVDataFormatter.formatOptions(optionRepo.getAllOptions()).forEach { writer.writeNext(it.toTypedArray()) }

            writer.writeNext(arrayOf(""))
            writer.writeNext(arrayOf("=== Stock Summary Table ==="))
            CSVDataFormatter.formatStockSummary(marketRepo.getAllStockSummary()).forEach { writer.writeNext(it.toTypedArray()) }

            writer.writeNext(arrayOf(""))
            writer.writeNext(arrayOf("=== Options Summary Table ==="))
            CSVDataFormatter.formatOptionsSummary(marketRepo.getAllOptionsSummary()).forEach { writer.writeNext(it.toTypedArray()) }

            writer.close()
        }
    }

    private fun exportToCSV(context: Context, fileName: String, rows: List<List<String>>) {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "${timeStamp()}-$fileName"
        )
        val writer = CSVWriter(FileWriter(file))
        rows.forEach { writer.writeNext(it.toTypedArray()) }
        writer.close()
        Log.i("FILE PATH", "exportToCSV: " + file)
    }

    private fun timeStamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }
}
