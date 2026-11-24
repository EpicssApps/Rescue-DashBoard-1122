package com.epicx.apps.rescue1122dashboard

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class VehicleAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "VehicleTimerDebug"
        private const val ALARM_CHANNEL_ID = "vehicle_alarm_channel_sound_v2"
        private var mediaPlayer: MediaPlayer? = null

        const val ACTION_FORCE_MAKE_ACTIVE = "COM.EPICX.RESCUE.ACTION_FORCE_MAKE_ACTIVE"
        const val ACTION_MAKE_ACTIVE = "COM.EPICX.RESCUE.ACTION_MAKE_ACTIVE"
        const val ACTION_EXTEND_10_MINS = "COM.EPICX.RESCUE.ACTION_EXTEND_10_MINS"

        fun stopAlarmSound() {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.stop()
                    }
                    it.release()
                }
                mediaPlayer = null
            } catch (_: Exception) {}
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ""

        if (action.startsWith("STOP_ALARM_ACTION")) {
            Log.d(TAG, "Notification dismissed by user. Stopping alarm ringtone.")
            stopAlarmSound()
            return
        }

        if (action == ACTION_FORCE_MAKE_ACTIVE || action == ACTION_MAKE_ACTIVE) {
            val vehicleName = intent.getStringExtra("vehicleName") ?: "Vehicle"
            val vehicleId = intent.getStringExtra("vehicleId") ?: ""
            Log.i(TAG, "👉 [USER ACTION] User requested MAKE ACTIVE / IDLE for vehicle: $vehicleName (ID: $vehicleId)")

            stopAlarmSound()

            // Cancel running countdown timer service for this vehicle
            VehicleTimerManager.cancelTimer(context, vehicleName)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(vehicleName.hashCode() + 1000)

            if (vehicleId.isNotBlank()) {
                resetVehicleStatusToIdleOnServer(context, vehicleId, vehicleName)
            } else {
                Log.w(TAG, "⚠️ Cannot reset vehicle status to IDLE: vehicleId is empty.")
                android.widget.Toast.makeText(context, "Cannot reset $vehicleName: Vehicle ID missing!", android.widget.Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (action == ACTION_EXTEND_10_MINS) {
            val vehicleName = intent.getStringExtra("vehicleName") ?: "Vehicle"
            val vehicleId = intent.getStringExtra("vehicleId") ?: ""
            val reason = intent.getStringExtra("reason") ?: "Maintenance"
            Log.i(TAG, "👉 [USER ACTION] User requested EXTEND 10 MINS for vehicle: $vehicleName (ID: $vehicleId)")

            stopAlarmSound()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(vehicleName.hashCode() + 1000)

            // Start new 10-minute timer
            VehicleTimerManager.startTimer(context, vehicleName, reason, 10, vehicleId)
            android.widget.Toast.makeText(context, "⏳ Timer extended by 10 minutes for $vehicleName", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (action == VehicleSyncWorker.ACTION_OUT_OF_SYNC_ALARM) {
            val vehicles = intent.getStringArrayListExtra(VehicleSyncWorker.EXTRA_OUT_OF_SYNC_VEHICLES) ?: arrayListOf()
            val vehicleListStr = if (vehicles.isNotEmpty()) vehicles.joinToString(", ") else "Unknown Vehicle"
            Log.w("Rescue1122Sync", "🔊 [ALARM RECEIVER] OUT OF SYNC ALARM FIRED for vehicle(s): $vehicleListStr")

            wakeUpAndPlayOutOfSyncAlarm(context, vehicleListStr)
            return
        }

        val vehicleName = intent.getStringExtra("vehicleName") ?: "Vehicle"
        val reason = intent.getStringExtra("reason") ?: "Maintenance"
        val vehicleId = intent.getStringExtra("vehicleId") ?: ""
        Log.d(TAG, "==> VehicleAlarmReceiver onReceive FIRED for vehicle: $vehicleName, id: $vehicleId, reason: $reason")

        VehicleTimerService.removeTimer(vehicleName)

        wakeUpAndPlayAlarm(context, vehicleName, reason, vehicleId)
    }

    @SuppressLint("WakelockTimeout")
    private fun wakeUpAndPlayOutOfSyncAlarm(context: Context, vehicleListStr: String) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Rescue1122:OutOfSyncAlarmWakeLock"
            )
            wakeLock.acquire(60000L)
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock error: ${e.message}")
        }

        playAlarm(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 9999

        notificationManager.cancel(notificationId)

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, pendingFlags)

        val stopIntent = Intent(context, VehicleAlarmReceiver::class.java).apply {
            action = "STOP_ALARM_ACTION_OUT_OF_SYNC"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(context, notificationId + 2000, stopIntent, pendingFlags)

        val alarmNotification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("🚨 OUT OF SYNC ALARM: $vehicleListStr")
            .setContentText("Staff App is out of sync for vehicle(s): $vehicleListStr")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setDeleteIntent(stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(null)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, alarmNotification)
    }

    @SuppressLint("WakelockTimeout")
    private fun wakeUpAndPlayAlarm(context: Context, vehicleName: String, reason: String, vehicleId: String = "") {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Rescue1122:VehicleAlarmWakeLock"
            )
            wakeLock.acquire(60000L)
        } catch (e: Exception) {
            Log.e(TAG, "WakeLock error: ${e.message}")
        }

        playAlarm(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = vehicleName.hashCode()

        notificationManager.cancel(notificationId)

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val fullScreenPendingIntent = PendingIntent.getActivity(context, 0, fullScreenIntent, pendingFlags)

        // Action 1: Make Active / Idle
        val makeActiveIntent = Intent(context, VehicleAlarmReceiver::class.java).apply {
            action = ACTION_MAKE_ACTIVE
            putExtra("vehicleName", vehicleName)
            putExtra("vehicleId", vehicleId)
        }
        val makeActivePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 3000,
            makeActiveIntent,
            pendingFlags
        )

        // Action 2: Extend 10 Minutes
        val extendIntent = Intent(context, VehicleAlarmReceiver::class.java).apply {
            action = ACTION_EXTEND_10_MINS
            putExtra("vehicleName", vehicleName)
            putExtra("vehicleId", vehicleId)
            putExtra("reason", reason)
        }
        val extendPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 4000,
            extendIntent,
            pendingFlags
        )

        // Stop Alarm intent when user dismisses/swipes away notification
        val stopIntent = Intent(context, VehicleAlarmReceiver::class.java).apply {
            action = "STOP_ALARM_ACTION_${vehicleName.hashCode()}"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(context, notificationId + 2000, stopIntent, pendingFlags)

        val alarmNotification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ TIME UP: $vehicleName")
            .setContentText("Minor maintenance time up ($reason)! Do you want to set vehicle to IDLE / ACTIVE now?")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setDeleteIntent(stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(null)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_save, "🟢 Active / Idle", makeActivePendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "⏳ Wait 10 Mins", extendPendingIntent)
            .build()

        notificationManager.notify(notificationId + 1000, alarmNotification)
    }

    private fun playAlarm(context: Context) {
        try {
            stopAlarmSound()
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer = MediaPlayer().apply {
                setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setDataSource(context.applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            Log.d(TAG, "VehicleAlarmReceiver MediaPlayer alarm playing single tone")

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                stopAlarmSound()
            }, 60000)
        } catch (e: Exception) {
            Log.e(TAG, "VehicleAlarmReceiver playAlarm error: ${e.message}")
        }
    }

    private fun resetVehicleStatusToIdleOnServer(context: Context, vehicleId: String, vehicleName: String) {
        if (vehicleId.isBlank()) {
            Log.e(TAG, "❌ [STEP 4: ERROR] Cannot reset vehicle status to IDLE: vehicleId is EMPTY!")
            return
        }

        Log.i(TAG, "👉 [STEP 4: START AUTO-RESET] Starting background thread for Vehicle '$vehicleName' (ID: $vehicleId)...")

        Thread {
            try {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val cookies = prefs.getString("session_cookies", "") ?: ""

                if (cookies.isEmpty()) {
                    Log.e(TAG, "❌ [STEP 4: ERROR] Cannot reset vehicle status to IDLE on server: session cookies are MISSING!")
                    return@Thread
                }

                // 1. GET edit page to fetch CSRF token and current vehicle parameters
                val editUrlStr = "https://punjab.rescue1122.org/dashboard/vehicle/$vehicleId/edit"
                Log.i(TAG, "👉 [STEP 4: GET EDIT PAGE] Fetching form page from: $editUrlStr")

                val editUrl = URL(editUrlStr)
                val editConnection = (editUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Cookie", cookies)
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    connectTimeout = 15000
                    readTimeout = 15000
                    instanceFollowRedirects = true
                }

                val getResponseCode = editConnection.responseCode
                Log.i(TAG, "👉 [STEP 4: GET RESPONSE] HTTP Status Code: $getResponseCode for vehicleId: $vehicleId")

                if (getResponseCode != HttpURLConnection.HTTP_OK) {
                    editConnection.disconnect()
                    Log.e(TAG, "❌ [STEP 4: ERROR] Failed to fetch edit page for vehicle $vehicleId (HTTP $getResponseCode)")
                    return@Thread
                }

                val reader = BufferedReader(InputStreamReader(editConnection.inputStream))
                val htmlBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    htmlBuilder.append(line).append("\n")
                }
                reader.close()
                editConnection.disconnect()

                val editHtml = htmlBuilder.toString()
                val doc = Jsoup.parse(editHtml)

                var csrfToken = doc.select("input[name=_token]").`val`().trim()
                if (csrfToken.isEmpty()) {
                    val match = Regex("var _token = [\"']([^\"']+)[\"']").find(editHtml)
                    if (match != null) {
                        csrfToken = match.groupValues[1]
                    }
                }

                if (csrfToken.isEmpty()) {
                    Log.e(TAG, "❌ [STEP 4: ERROR] CSRF token not found on edit page for vehicle $vehicleId")
                    return@Thread
                }
                Log.i(TAG, "👉 [STEP 4: CSRF TOKEN] Extracted token: ${csrfToken.take(8)}...")

                // Build form parameters: extract all current form fields from edit form
                val postParams = StringBuilder()
                val form = doc.select("form#vehicle_form").first() ?: doc.select("form").first()

                if (form != null) {
                    val inputs = form.select("input, select, textarea")
                    for (element in inputs) {
                        val name = element.attr("name")
                        if (name.isBlank() || name == "_method" || name == "status" || name == "_token") continue

                        var value = ""
                        if (element.tagName() == "select") {
                            val selectedOption = element.select("option[selected]").first() ?: element.select("option").first()
                            if (selectedOption != null) {
                                value = selectedOption.`val`()
                            }
                        } else {
                            value = element.`val`()
                        }

                        if (postParams.isNotEmpty()) postParams.append("&")
                        postParams.append(URLEncoder.encode(name, "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(value, "UTF-8"))
                    }
                }

                if (postParams.isNotEmpty()) postParams.append("&")
                postParams.append("_token=").append(URLEncoder.encode(csrfToken, "UTF-8"))
                postParams.append("&_method=").append(URLEncoder.encode("PATCH", "UTF-8"))
                postParams.append("&status=").append(URLEncoder.encode("idle", "UTF-8"))

                Log.i(TAG, "👉 [STEP 4: SENDING POST PATCH] Sending request to https://punjab.rescue1122.org/dashboard/vehicle/$vehicleId")

                val postUrlStr = "https://punjab.rescue1122.org/dashboard/vehicle/$vehicleId"
                val postUrl = URL(postUrlStr)
                val postConnection = (postUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    setRequestProperty("Cookie", cookies)
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val os = postConnection.outputStream
                os.write(postParams.toString().toByteArray(Charsets.UTF_8))
                os.flush()
                os.close()

                val postResponseCode = postConnection.responseCode
                Log.i(TAG, "👉 [STEP 4: POST RESPONSE] HTTP Status Code: $postResponseCode for vehicle $vehicleName (ID: $vehicleId)")
                postConnection.disconnect()

                if (postResponseCode == HttpURLConnection.HTTP_OK || postResponseCode == HttpURLConnection.HTTP_MOVED_TEMP || postResponseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                    Log.i(TAG, "🎉 [STEP 4: SUCCESS!] Vehicle $vehicleName (ID: $vehicleId) successfully reset to IDLE on server!")
                    showAutoResetSuccessNotification(context, vehicleName)
                } else {
                    Log.e(TAG, "❌ [STEP 4: FAILED] Server responded with error status: $postResponseCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 [STEP 4: EXCEPTION] Exception in resetVehicleStatusToIdleOnServer: ${e.message}", e)
            }
        }.start()
    }

    private fun showAutoResetSuccessNotification(context: Context, vehicleName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingFlags)

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🟢 VEHICLE AUTO-ACTIVATED: $vehicleName")
            .setContentText("Timer finished (00:00). Vehicle has been automatically reset to IDLE status on Rescue 1122 Portal!")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(vehicleName.hashCode() + 5000, notification)
    }
}
