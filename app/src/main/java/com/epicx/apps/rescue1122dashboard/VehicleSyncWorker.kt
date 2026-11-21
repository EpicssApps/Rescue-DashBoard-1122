package com.epicx.apps.rescue1122dashboard

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class VehicleSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "Rescue1122Sync"
        const val WORK_NAME = "vehicle_out_of_sync_worker"
        const val ACTION_OUT_OF_SYNC_ALARM = "COM.EPICX.RESCUE.ACTION_OUT_OF_SYNC_ALARM"
        const val EXTRA_OUT_OF_SYNC_VEHICLES = "extra_out_of_sync_vehicles"

        fun schedulePeriodicSync(context: Context) {
            try {
                val workManager = WorkManager.getInstance(context)
                val workInfoList = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()

                var isAlreadyScheduled = false
                if (!workInfoList.isNullOrEmpty()) {
                    for (workInfo in workInfoList) {
                        if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                            isAlreadyScheduled = true
                            break
                        }
                    }
                }

                if (isAlreadyScheduled) {
                    Log.d(TAG, "🟢 [BACKGROUND WORKER] 15-minute VehicleSyncWorker is ALREADY active. Timer was NOT reset.")
                    return
                }

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val syncRequest = PeriodicWorkRequestBuilder<VehicleSyncWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
                Log.i(TAG, "🟢 [BACKGROUND WORKER] Periodic 15-minute VehicleSyncWorker scheduled successfully!")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking or scheduling WorkManager: ${e.message}")
            }
        }

        fun triggerOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<VehicleSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
            Log.i(TAG, "⚡ [MANUAL SYNC] One-time VehicleSyncWorker triggered by user!")
        }
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "==================================================")
        Log.i(TAG, "🔍 [WORKMANAGER SYNC START] VehicleSyncWorker started checking out-of-sync vehicles (Interval: 15 mins)...")
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val cookies = prefs.getString("session_cookies", "") ?: ""

        if (cookies.isEmpty()) {
            Log.w(TAG, "⚠️ [COOKIE MISSING] No session cookies found in SharedPreferences! User must log in via WebView first.")
            Log.i(TAG, "==================================================")
            return Result.success()
        }

        Log.d(TAG, "🔑 [COOKIE OK] Using saved session cookies")

        try {
            val urlString = "https://punjab.rescue1122.org/dashboard/vehicle/diagnose"
            Log.d(TAG, "🌐 [HTTP GET] Connecting to: $urlString")

            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Cookie", cookies)
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                connectTimeout = 15000
                readTimeout = 15000
                instanceFollowRedirects = true
            }

            val responseCode = connection.responseCode
            Log.i(TAG, "📡 [HTTP RESPONSE] Status Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val htmlBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    htmlBuilder.append(line).append("\n")
                }
                reader.close()

                val html = htmlBuilder.toString()
                Log.d(TAG, "📄 [HTML RECEIVED] Response HTML length: ${html.length} characters")

                val outOfSyncVehicles = parseOutOfSyncVehicles(html)

                if (outOfSyncVehicles.isNotEmpty()) {
                    Log.w(TAG, "🚨 [OUT OF SYNC DETECTED] Found ${outOfSyncVehicles.size} Out-of-Sync Vehicle(s): $outOfSyncVehicles")
                    triggerAlarm(context, outOfSyncVehicles)
                } else {
                    Log.i(TAG, "✅ [STATUS OK] All active vehicles are IN SYNC! No out-of-sync vehicle found.")
                }
            } else if (responseCode == 401 || responseCode == 419) {
                Log.e(TAG, "❌ [AUTH ERROR] Session expired or invalid CSRF/Session token ($responseCode). User needs to open app & re-login.")
            } else {
                Log.e(TAG, "⚠️ [HTTP ERROR] Unexpected response status code: $responseCode")
            }
            connection.disconnect()
            Log.i(TAG, "==================================================")
            return Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "💥 [EXCEPTION] Error in VehicleSyncWorker execution: ${e.message}", e)
            Log.i(TAG, "==================================================")
            return Result.retry()
        }
    }

    private fun parseOutOfSyncVehicles(html: String): List<String> {
        val outOfSyncList = mutableListOf<String>()
        val doc = Jsoup.parse(html)
        val rows = doc.select("table#vehicle_table tbody tr")

        Log.d(TAG, "📊 [PARSING] Scanning ${rows.size} vehicle rows from #vehicle_table...")

        var rowIndex = 1
        for (row in rows) {
            val rowText = row.text()
            val rowHtml = row.html()

            // Check if "Staff App is out of sync" is present in the row
            val isOutOfSync = rowText.contains("Staff App is out of sync", ignoreCase = true) ||
                    rowHtml.contains("Staff App is out of sync", ignoreCase = true)

            // Extract Registration Number from span attribute
            var vehicleName = ""
            val regSpan = row.select("span[data-original-title=Registration Number]").first()
            if (regSpan != null) {
                vehicleName = regSpan.text().trim()
            }

            if (vehicleName.isEmpty()) {
                val firstCell = row.select("td.sorting_1, td:nth-child(2)").first()
                if (firstCell != null) {
                    val spans = firstCell.select("span")
                    if (spans.isNotEmpty()) {
                        vehicleName = spans.first()?.text()?.trim() ?: ""
                    }
                }
            }

            if (vehicleName.isEmpty()) {
                vehicleName = "Vehicle#$rowIndex"
            }

            if (isOutOfSync) {
                Log.w(TAG, "   ⚠️ Row $rowIndex: Vehicle [$vehicleName] -> STAFF APP IS OUT OF SYNC!")
                outOfSyncList.add(vehicleName)
            } else {
                Log.d(TAG, "   ✓ Row $rowIndex: Vehicle [$vehicleName] -> Normal")
            }
            rowIndex++
        }
        return outOfSyncList.distinct()
    }

    private fun triggerAlarm(context: Context, outOfSyncVehicles: List<String>) {
        Log.w(TAG, "🔔 [TRIGGER ALARM] Broadcast sent to VehicleAlarmReceiver for: $outOfSyncVehicles")
        val intent = Intent(context, VehicleAlarmReceiver::class.java).apply {
            action = ACTION_OUT_OF_SYNC_ALARM
            putStringArrayListExtra(EXTRA_OUT_OF_SYNC_VEHICLES, ArrayList(outOfSyncVehicles))
        }
        context.sendBroadcast(intent)
    }
}
