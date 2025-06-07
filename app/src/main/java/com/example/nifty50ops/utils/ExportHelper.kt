package com.example.nifty50ops.utils


import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
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
import java.io.FileInputStream
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

    fun copyAllFilesToDownloads(context: Context) {
        val documentsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "")
        if (!documentsDir.exists() || !documentsDir.isDirectory) {
            Log.e("FILE COPY", "Documents folder not found.")
            return
        }

        val files = documentsDir.listFiles() ?: emptyArray()
        Log.i("FILE COPY", "Found ${files.size} files to copy to Downloads.")

        files.forEach { file ->
            if (file.isFile) {
                copyFileToDownloads(context, file)
            }
        }

        Log.i("FILE COPY", "All files copied to Downloads successfully.")
    }

    private fun copyFileToDownloads(context: Context, sourceFile: File) {
        try {
            val mimeType = when (sourceFile.extension.lowercase(Locale.ROOT)) {
                "csv" -> "text/csv"
                "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                else -> "application/octet-stream"
            }

            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, sourceFile.name)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val contentResolver = context.contentResolver
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
            } else {
                TODO("VERSION.SDK_INT < Q")
            }

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.i("FILE COPY", "Copied ${sourceFile.name} to Downloads.")
        } catch (e: Exception) {
            Log.e("FILE COPY", "Failed to copy ${sourceFile.name}: ${e.message}", e)
        }
    }

}
