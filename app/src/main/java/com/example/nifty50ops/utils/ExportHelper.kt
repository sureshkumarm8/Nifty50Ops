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
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
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
    fun exportSentimentSummary(context: Context, repo: MarketRepository) {
        CoroutineScope(Dispatchers.IO).launch {
            val rows = CSVDataFormatter.formatSentimentSummary(repo.getAllSentimentSummary())
            exportToCSV(context, "sentiment_summary.csv", rows)
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


    fun exportAllTablesToExcel(
        context: Context,
        stockRepo: StockRepository,
        optionRepo: OptionsRepository,
        marketRepo: MarketRepository
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val workbook = XSSFWorkbook()

            fun Sheet.addTable(headers: List<String>, data: List<List<String>>) {
                var rowIndex = 0
                val headerRow = createRow(rowIndex++)
                headers.forEachIndexed { col, text -> headerRow.createCell(col).setCellValue(text) }

                data.forEach { rowData ->
                    val row = createRow(rowIndex++)
                    rowData.forEachIndexed { col, cellData ->
                        row.createCell(col).setCellValue(cellData)
                    }
                }
            }

            // Sheet 1 - Stock Table
            val stockSheet = workbook.createSheet("Stocks")
            val stockData = CSVDataFormatter.formatStocks(stockRepo.getAllStocksExport())
            stockSheet.addTable(stockData.first(), stockData.drop(1))

            // Sheet 2 - Options Table
            val optionsSheet = workbook.createSheet("Options")
            val optionsData = CSVDataFormatter.formatOptions(optionRepo.getAllOptionsExport())
            optionsSheet.addTable(optionsData.first(), optionsData.drop(1))

            // Sheet 3 - Sentiment Summary
            val sentimentSheet = workbook.createSheet("Sentiment Summary")
            val sentimentData = CSVDataFormatter.formatSentimentSummary(marketRepo.getAllSentimentSummary())
            sentimentSheet.addTable(sentimentData.first(), sentimentData.drop(1))

            // Sheet 4 - Stock Summary
            val stockSummarySheet = workbook.createSheet("Stock Summary")
            val stockSummaryData = CSVDataFormatter.formatStockSummary(marketRepo.getAllStockSummary())
            stockSummarySheet.addTable(stockSummaryData.first(), stockSummaryData.drop(1))

            // Sheet 5 - Options Summary
            val optionsSummarySheet = workbook.createSheet("Options Summary")
            val optionsSummaryData = CSVDataFormatter.formatOptionsSummary(marketRepo.getAllOptionsSummary())
            optionsSummarySheet.addTable(optionsSummaryData.first(), optionsSummaryData.drop(1))

            // Save to file
            val time = timeStamp()
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "$time-AllTables.xlsx"
            )
            FileOutputStream(file).use { workbook.write(it) }
            Log.i("FILE PATH", "exportAllTables: " + file)
            workbook.close()
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
