package com.example.nifty50ops.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.nifty50ops.network.ApiService
import java.io.File

fun readSecurityIdToSymbolMap(context: Context): Map<Int, String> {
    val file = File(context.filesDir, "option_data/NiftyScrips.txt")
    val map = mutableMapOf<Int, String>()

    if (!file.exists()) {
        Log.e("MapLoader", "File not found: ${file.absolutePath}")
        return emptyMap()
    }

    val lines = file.readLines()

    for (line in lines.dropLast(1)) {
        val cleanedLine = line.trim().removeSuffix(",")
        val parts = cleanedLine.split(":").map { it.trim().removeSurrounding("\"") }

        if (parts.size == 2) {
            val key = parts[0].toIntOrNull()
            val value = parts[1]
            if (key != null) {
                map[key] = value
            }
        }
    }

    return map
}

fun readJwtToken(context: Context) {
    val file = File(context.filesDir, "option_data/NiftyScrips.txt")

    if (!file.exists()) {
        Log.e("JWTLoader", "File not found: ${file.absolutePath}")
        return
    }

    val lines = file.readLines()
    val lastLine = lines.lastOrNull()

    if (!lastLine.isNullOrBlank()) {
        ApiService.jwtToken = lastLine.trim()
        Log.d("JWT", "JWT Token assigned: ${ApiService.jwtToken?.take(10)}...")
    }
}

fun copyFromDownloadsToInternal(context: Context): Boolean {
    val downloadFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NiftyScrips.txt")
    val internalDir = File(context.filesDir, "option_data")
    if (!internalDir.exists()) internalDir.mkdirs()
    val internalFile = File(internalDir, "NiftyScrips.txt")

    return try {
        if (downloadFile.exists()) {
            downloadFile.copyTo(internalFile, overwrite = true)
            Log.d("FileCopy", "Copied from Downloads to ${internalFile.absolutePath}")
            true
        } else {
            Log.e("FileCopy", "NiftyScrips.txt not found in Downloads")
            false
        }
    } catch (e: Exception) {
        Log.e("FileCopy", "Failed to copy: ${e.message}", e)
        false
    }
}

fun convertToLacsString(value: Int): String {
    return if (value >= 100000) "${"%.2f".format(value / 100000.0)}L" else value.toString()
}

fun twoDecimalDisplay(value: Double): String {
    return("%.2f".format(value))
}
