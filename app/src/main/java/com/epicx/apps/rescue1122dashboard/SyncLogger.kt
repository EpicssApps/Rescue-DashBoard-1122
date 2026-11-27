package com.epicx.apps.rescue1122dashboard

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SyncLogger {
    private const val TAG = "Rescue1122Sync"
    private const val LOG_FILE_NAME = "vehicle_sync_logs.txt"
    private const val MAX_LOG_LINES = 500

    fun log(context: Context, message: String) {
        Log.i(TAG, message)
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            val timeStamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "[$timeStamp] $message"

            val existingLines = if (file.exists()) file.readLines() else emptyList()
            val newLines = existingLines + logEntry
            val trimmedLines = if (newLines.size > MAX_LOG_LINES) newLines.takeLast(MAX_LOG_LINES) else newLines

            file.writeText(trimmedLines.joinToString("\n") + "\n")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to log file: ${e.message}")
        }
    }

    fun getLogs(context: Context): String {
        return try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists() && file.length() > 0) {
                file.readText()
            } else {
                "No sync logs recorded yet."
            }
        } catch (e: Exception) {
            "Error reading logs: ${e.message}"
        }
    }

    fun clearLogs(context: Context) {
        try {
            val file = File(context.filesDir, LOG_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing logs: ${e.message}")
        }
    }
}
