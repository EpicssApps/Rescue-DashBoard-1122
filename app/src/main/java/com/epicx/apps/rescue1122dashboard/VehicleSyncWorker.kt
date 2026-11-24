package com.epicx.apps.rescue1122dashboard

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
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

            val inputData = workDataOf("is_manual_sync" to true)

            val syncRequest = OneTimeWorkRequestBuilder<VehicleSyncWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
            Log.i(TAG, "⚡ [MANUAL SYNC] One-time VehicleSyncWorker triggered by user!")
        }
    }

    override suspend fun doWork(): Result {
        val isManualSync = inputData.getBoolean("is_manual_sync", false)
        Log.i(TAG, "==================================================")
        Log.i(TAG, "🔍 [WORKMANAGER SYNC START] Checking out-of-sync vehicles (isManualSync=$isManualSync, Scanning up to 100 entries)...")
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val cookies = prefs.getString("session_cookies", "") ?: ""

        if (cookies.isEmpty()) {
            Log.w(TAG, "⚠️ [COOKIE MISSING] No session cookies found in SharedPreferences! Showing notification to re-login.")
            showSessionExpiredNotification(context)
            Log.i(TAG, "==================================================")
            return Result.success()
        }

        try {
            // Step 1: Initial GET request to retrieve CSRF Token
            val getUrl = URL("https://punjab.rescue1122.org/dashboard/vehicle/diagnose")
            val getConnection = (getUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Cookie", cookies)
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                connectTimeout = 15000
                readTimeout = 15000
                instanceFollowRedirects = false
            }

            val responseCode = getConnection.responseCode
            val locationHeader = getConnection.getHeaderField("Location") ?: ""
            Log.i(TAG, "📡 [HTTP GET RESPONSE] Status Code: $responseCode, Location: $locationHeader")

            if (responseCode == 401 || responseCode == 419 ||
                ((responseCode in 300..399) && locationHeader.contains("login", ignoreCase = true))
            ) {
                Log.e(TAG, "❌ [AUTH ERROR] Session expired ($responseCode, Redirect Location: $locationHeader). Showing notification to re-login.")
                showSessionExpiredNotification(context)
                getConnection.disconnect()
                return Result.success()
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "⚠️ [HTTP ERROR] Unexpected response status code: $responseCode")
                getConnection.disconnect()
                return Result.success()
            }

            val reader = BufferedReader(InputStreamReader(getConnection.inputStream))
            val htmlBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                htmlBuilder.append(line).append("\n")
            }
            reader.close()
            getConnection.disconnect()

            val getHtml = htmlBuilder.toString()

            val isLoginPageHtml = getHtml.contains("action=\"/login\"", ignoreCase = true) ||
                    getHtml.contains("action=\"https://punjab.rescue1122.org/login\"", ignoreCase = true) ||
                    getHtml.contains("name=\"password\"", ignoreCase = true) ||
                    getHtml.contains("type=\"password\"", ignoreCase = true)

            if (isLoginPageHtml) {
                Log.e(TAG, "❌ [AUTH ERROR] Login page form detected. Session expired. Showing notification to re-login.")
                showSessionExpiredNotification(context)
                return Result.success()
            }

            // Extract CSRF Token from HTML page
            val doc = Jsoup.parse(getHtml)
            var csrfToken = doc.select("input[name=_token]").`val`().trim()
            if (csrfToken.isEmpty()) {
                val match = Regex("var _token = [\"']([^\"']+)[\"']").find(getHtml)
                if (match != null) {
                    csrfToken = match.groupValues[1]
                }
            }

            Log.d(TAG, "🔑 [CSRF TOKEN] Extracted _token: ${if (csrfToken.isNotEmpty()) csrfToken.take(8) + "..." else "EMPTY"}")

            // Load user-configured vehicle exclusion preferences
            val defaultExcluded = setOf("AIR_AMB", "GDV", "BOAT")
            val excludedCategories = prefs.getStringSet("excluded_vehicle_categories", defaultExcluded) ?: defaultExcluded
            val customPrefixesStr = prefs.getString("custom_excluded_prefixes", "") ?: ""

            var outOfSyncVehicles = emptyList<String>()

            // Step 2: AJAX POST for 100 entries with complete DataTables schema (Primary & most accurate method)
            if (csrfToken.isNotEmpty()) {
                outOfSyncVehicles = fetchAll100EntriesViaAjax(cookies, csrfToken, excludedCategories, customPrefixesStr)
            }

            // Step 3: Fallback - Parsing initial GET HTML if AJAX failed
            if (outOfSyncVehicles.isEmpty() && getHtml.isNotEmpty()) {
                Log.d(TAG, "Parsing initial HTML fallback...")
                outOfSyncVehicles = parseOutOfSyncVehicles(getHtml, excludedCategories, customPrefixesStr)
            }

            if (outOfSyncVehicles.isNotEmpty()) {
                Log.w(TAG, "🚨 [OUT OF SYNC DETECTED] Found ${outOfSyncVehicles.size} Active Out-of-Sync Vehicle(s): $outOfSyncVehicles")
                triggerAlarm(context, outOfSyncVehicles)
            } else {
                Log.i(TAG, "✅ [STATUS OK] All active on-duty emergency vehicles are IN SYNC! No action required.")
                if (isManualSync) {
                    showAllVehiclesInSyncNotification(context)
                }
            }

            Log.i(TAG, "==================================================")
            return Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "💥 [EXCEPTION] Error in VehicleSyncWorker execution: ${e.message}", e)
            Log.i(TAG, "==================================================")
            return Result.retry()
        }
    }

    private fun isExcludedVehicle(
        vehicleName: String,
        excludedCategories: Set<String>,
        customPrefixesStr: String
    ): Boolean {
        val nameUpper = vehicleName.trim().uppercase()

        // 1. Check selected categories with District Codes & Category Keywords
        if (excludedCategories.contains("AIR_AMB") && nameUpper.contains("AIR-AMB")) return true
        if (excludedCategories.contains("AMBULANCE") && (nameUpper.contains("BNA-") || (nameUpper.contains("AMB") && !nameUpper.contains("AIR-AMB")))) return true
        if (excludedCategories.contains("BIKE") && (nameUpper.contains("BNB-") || nameUpper.contains("BIKE") || nameUpper.contains("-BK") || nameUpper.contains("MB-"))) return true
        if (excludedCategories.contains("FIRE") && (nameUpper.contains("BNF-") || nameUpper.contains("FIRE") || nameUpper.contains("-FT"))) return true
        if (excludedCategories.contains("GDV") && (nameUpper.contains("BNG-") || nameUpper.contains("GDV") || nameUpper.contains("VAN"))) return true
        if (excludedCategories.contains("BOAT") && (nameUpper.contains("BWN-BT") || nameUpper.contains("BOAT") || nameUpper.contains("-BT"))) return true
        if (excludedCategories.contains("RESCUE") && (nameUpper.contains("BNR-") || nameUpper.contains("RESCUE") || nameUpper.contains("-RV"))) return true
        if (excludedCategories.contains("SPECIALIZED") && (nameUpper.contains("SPEC") || nameUpper.contains("SPECIALIZED"))) return true

        // 2. Check custom user-entered prefixes
        if (customPrefixesStr.isNotBlank()) {
            val prefixes = customPrefixesStr.split(",").map { it.trim().uppercase() }.filter { it.isNotEmpty() }
            for (prefix in prefixes) {
                if (nameUpper.contains(prefix)) {
                    return true
                }
            }
        }

        return false
    }

    private fun isBusyMaintenanceOrOffDuty(rowContent: String): Boolean {
        return rowContent.contains("Attendance is not marked", ignoreCase = true) ||
                rowContent.contains("No Rescuer Associated", ignoreCase = true) ||
                rowContent.contains("Vehicle status is On Emergency", ignoreCase = true) ||
                rowContent.contains("On Emergency", ignoreCase = true) ||
                rowContent.contains("Vehicle status is Maintenance", ignoreCase = true) ||
                rowContent.contains("Maintenance", ignoreCase = true) ||
                rowContent.contains("Vehicle status is On Protocol", ignoreCase = true) ||
                rowContent.contains("On Protocol", ignoreCase = true)
    }

    private fun extractCleanVehicleName(col1Html: String, fallbackIndex: Int): String {
        try {
            val doc = Jsoup.parse(col1Html)
            val regSpan = doc.select("span[data-original-title=Registration Number]").first()
            if (regSpan != null) {
                val text = regSpan.text().trim()
                if (text.isNotEmpty() && !text.contains("Staff App", ignoreCase = true)) {
                    return text
                }
            }

            val spans = doc.select("span")
            for (span in spans) {
                val spanText = span.text().trim()
                if (spanText.isNotEmpty() &&
                    !spanText.contains("N/A", ignoreCase = true) &&
                    !spanText.contains("Bahawalnagar", ignoreCase = true) &&
                    !spanText.contains("Staff App", ignoreCase = true) &&
                    !spanText.contains("Station", ignoreCase = true) &&
                    !spanText.contains("District", ignoreCase = true)) {
                    return spanText
                }
            }

            val rawText = doc.text().trim()
            val firstWord = rawText.split(" ").firstOrNull { it.isNotBlank() && !it.contains("Staff", ignoreCase = true) }
            if (!firstWord.isNullOrEmpty()) {
                return firstWord
            }
        } catch (_: Exception) {}

        return "Vehicle#$fallbackIndex"
    }

    private fun fetchAll100EntriesViaAjax(
        cookies: String,
        csrfToken: String,
        excludedCategories: Set<String>,
        customPrefixesStr: String
    ): List<String> {
        val outOfSyncList = mutableListOf<String>()
        try {
            val postUrl = URL("https://punjab.rescue1122.org/dashboard/vehicle/diagnose/fetch")
            val postConnection = (postUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Cookie", cookies)
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val postData = buildDataTablesPostParams(csrfToken, 100)
            val os = postConnection.outputStream
            os.write(postData.toByteArray(Charsets.UTF_8))
            os.flush()
            os.close()

            val postResponseCode = postConnection.responseCode
            Log.i(TAG, "📡 [AJAX POST RESPONSE] Status Code: $postResponseCode for 100 entries")

            if (postResponseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(postConnection.inputStream))
                val jsonBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    jsonBuilder.append(line).append("\n")
                }
                reader.close()

                val jsonStr = jsonBuilder.toString()
                val gson = Gson()
                val jsonObject = gson.fromJson(jsonStr, JsonObject::class.java)

                if (jsonObject != null && jsonObject.has("data")) {
                    val dataArray = jsonObject.getAsJsonArray("data")
                    Log.i(TAG, "📊 [DATA TABLE FULL SCAN] Total vehicle entries scanned from server: ${dataArray.size()} (Requested max: 100)")

                    for (i in 0 until dataArray.size()) {
                        val rowElement = dataArray.get(i)
                        var vehicleName = ""
                        val fullRowText = rowElement.toString()

                        if (rowElement.isJsonArray) {
                            val rowCols = rowElement.asJsonArray
                            // Column 1 is Vehicle Info
                            if (rowCols.size() > 1) {
                                val col1Html = rowCols.get(1).asString
                                vehicleName = extractCleanVehicleName(col1Html, i + 1)
                            }
                        }

                        if (vehicleName.isEmpty() || vehicleName.contains("Staff App", ignoreCase = true)) {
                            vehicleName = "Vehicle#${i + 1}"
                        }

                        // 1. Check if vehicle is excluded by user settings (skip completely if excluded)
                        if (isExcludedVehicle(vehicleName, excludedCategories, customPrefixesStr)) {
                            Log.i(TAG, "   ⏭️ Entry ${i + 1}: Vehicle [$vehicleName] -> EXCLUDED BY USER")
                            continue
                        }

                        // 2. Must have "Staff App is out of sync"
                        val isStaffAppOutOfSync = fullRowText.contains("Staff App is out of sync", ignoreCase = true)

                        // 3. Must NOT be On Emergency, Maintenance, Protocol, or Off-duty (unmarked attendance/no rescuer)
                        val isIgnoredState = isBusyMaintenanceOrOffDuty(fullRowText)

                        if (isStaffAppOutOfSync) {
                            if (isIgnoredState) {
                                Log.i(TAG, "   ⏭️ Entry ${i + 1}: Vehicle [$vehicleName] -> OUT OF SYNC BUT IGNORED (On Emergency / Maintenance / Off-duty)")
                            } else {
                                Log.w(TAG, "   ⚠️ Entry ${i + 1}: Vehicle [$vehicleName] -> REAL OUT OF SYNC! (Idle & Staff on Duty & App Out of Sync)")
                                outOfSyncList.add(vehicleName)
                            }
                        } else {
                            Log.d(TAG, "   ✓ Entry ${i + 1}: Vehicle [$vehicleName] -> Normal")
                        }
                    }
                }
            }
            postConnection.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching 100 entries via AJAX POST: ${e.message}")
        }
        return outOfSyncList.distinct()
    }

    private fun buildDataTablesPostParams(csrfToken: String, length: Int): String {
        val sb = StringBuilder()
        sb.append("_token=").append(URLEncoder.encode(csrfToken, "UTF-8"))
        sb.append("&draw=1")
        sb.append("&start=0")
        sb.append("&length=").append(length)
        sb.append("&search%5Bvalue%5D=")
        sb.append("&search%5Bregex%5D=false")
        sb.append("&order%5B0%5D%5Bcolumn%5D=1")
        sb.append("&order%5B0%5D%5Bdir%5D=asc")

        for (i in 0..6) {
            sb.append("&columns%5B").append(i).append("%5D%5Bdata%5D=").append(i)
            sb.append("&columns%5B").append(i).append("%5D%5Bname%5D=")
            sb.append("&columns%5B").append(i).append("%5D%5Bsearchable%5D=true")
            sb.append("&columns%5B").append(i).append("%5D%5Borderable%5D=").append(if (i < 4) "true" else "false")
            sb.append("&columns%5B").append(i).append("%5D%5Bsearch%5D%5Bvalue%5D=")
            sb.append("&columns%5B").append(i).append("%5D%5Bsearch%5D%5Bregex%5D=false")
        }
        return sb.toString()
    }

    private fun parseOutOfSyncVehicles(
        html: String,
        excludedCategories: Set<String>,
        customPrefixesStr: String
    ): List<String> {
        val outOfSyncList = mutableListOf<String>()
        val doc = Jsoup.parse(html)
        val rows = doc.select("table#vehicle_table tbody tr")

        Log.d(TAG, "📊 [PARSING FALLBACK] Scanning ${rows.size} vehicle rows from #vehicle_table...")

        var rowIndex = 1
        for (row in rows) {
            val cells = row.select("td")
            var vehicleName = ""

            if (cells.size > 1) {
                vehicleName = extractCleanVehicleName(cells[1].html(), rowIndex)
            }

            if (vehicleName.isEmpty() || vehicleName.contains("Staff App", ignoreCase = true)) {
                vehicleName = "Vehicle#$rowIndex"
            }

            val rowText = row.text()
            val rowHtml = row.html()

            if (isExcludedVehicle(vehicleName, excludedCategories, customPrefixesStr)) {
                Log.i(TAG, "   ⏭️ Row $rowIndex: Vehicle [$vehicleName] -> EXCLUDED BY USER")
                rowIndex++
                continue
            }

            val isStaffAppOutOfSync = rowText.contains("Staff App is out of sync", ignoreCase = true) ||
                    rowHtml.contains("Staff App is out of sync", ignoreCase = true)

            val isIgnoredState = isBusyMaintenanceOrOffDuty(rowText) || isBusyMaintenanceOrOffDuty(rowHtml)

            if (isStaffAppOutOfSync) {
                if (isIgnoredState) {
                    Log.i(TAG, "   ⏭️ Row $rowIndex: Vehicle [$vehicleName] -> OUT OF SYNC BUT IGNORED (On Emergency / Maintenance / Off-duty)")
                } else {
                    Log.w(TAG, "   ⚠️ Row $rowIndex: Vehicle [$vehicleName] -> REAL OUT OF SYNC!")
                    outOfSyncList.add(vehicleName)
                }
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

    private fun showSessionExpiredNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = android.app.PendingIntent.getActivity(context, 0, intent, pendingFlags)

        val notification = androidx.core.app.NotificationCompat.Builder(context, "vehicle_alarm_channel_sound_v2")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Rescue 1122 Session Expired")
            .setContentText("Your web session expired. Tap here to re-login.")
            .setContentIntent(pendingIntent)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(7777, notification)
    }

    private fun showAllVehiclesInSyncNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = android.app.PendingIntent.getActivity(context, 0, intent, pendingFlags)

        val notification = androidx.core.app.NotificationCompat.Builder(context, "vehicle_alarm_channel_sound_v2")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("✅ VEHICLES SYNC STATUS: ALL OK")
            .setContentText("All active on-duty emergency vehicles are IN SYNC!")
            .setContentIntent(pendingIntent)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(8888, notification)
    }
}
