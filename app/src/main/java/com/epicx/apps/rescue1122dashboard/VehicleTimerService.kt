package com.epicx.apps.rescue1122dashboard

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.concurrent.ConcurrentHashMap

class VehicleTimerService : Service() {

    companion object {
        private const val TAG = "VehicleTimerDebug"
        private const val TIMER_CHANNEL_ID = "vehicle_timer_channel_silent"
        private const val ALARM_CHANNEL_ID = "vehicle_alarm_channel_sound_v2"

        private val activeTimers = ConcurrentHashMap<String, Long>()

        fun isTimerActive(vehicleName: String): Boolean {
            return activeTimers.containsKey(vehicleName)
        }

        fun removeTimer(vehicleName: String) {
            activeTimers.remove(vehicleName)
        }

        fun startTimer(
            context: Context,
            vehicleName: String,
            reason: String,
            minutes: Int,
            vehicleId: String = "",
            startTimeMillis: Long = 0L,
            accumulatedMinutes: Int = 0
        ) {
            val actualStartTime = if (startTimeMillis > 0L) startTimeMillis else System.currentTimeMillis()
            val actualAccumulatedMins = if (accumulatedMinutes > 0) accumulatedMinutes else minutes

            val intent = Intent(context, VehicleTimerService::class.java).apply {
                action = "START_TIMER"
                putExtra("vehicleName", vehicleName)
                putExtra("reason", reason)
                putExtra("minutes", minutes)
                putExtra("vehicleId", vehicleId)
                putExtra("startTimeMillis", actualStartTime)
                putExtra("accumulatedMinutes", actualAccumulatedMins)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun cancelTimer(context: Context, vehicleName: String) {
            val intent = Intent(context, VehicleTimerService::class.java).apply {
                action = "CANCEL_TIMER"
                putExtra("vehicleName", vehicleName)
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("InlinedApi")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannels()

        val action = intent?.action
        val vehicleName = intent?.getStringExtra("vehicleName") ?: "Vehicle"

        if (action == "CANCEL_TIMER") {
            stopVehicleTimer(vehicleName)
        } else if (action == "START_TIMER") {
            val reason = intent.getStringExtra("reason") ?: "Maintenance"
            val minutes = intent.getIntExtra("minutes", 10)
            val vehicleId = intent.getStringExtra("vehicleId") ?: ""
            val startTimeMillis = intent.getLongExtra("startTimeMillis", System.currentTimeMillis())
            val accumulatedMinutes = intent.getIntExtra("accumulatedMinutes", minutes)

            val notificationId = vehicleName.hashCode()
            val durationMillis = minutes * 60 * 1000L
            val targetTimeWallClock = System.currentTimeMillis() + durationMillis

            val initialNotification = buildNativeChronometerNotification(
                vehicleName,
                reason,
                targetTimeWallClock,
                vehicleId,
                startTimeMillis,
                accumulatedMinutes
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(notificationId, initialNotification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } else {
                    startForeground(notificationId, initialNotification)
                }
            } catch (e: Exception) {
                Log.e(TAG, "startForeground error: ${e.message}")
            }

            startVehicleTimer(vehicleName, reason, minutes, vehicleId, startTimeMillis, accumulatedMinutes)
        }

        return START_STICKY
    }

    private fun startVehicleTimer(
        vehicleName: String,
        reason: String,
        minutes: Int,
        vehicleId: String = "",
        startTimeMillis: Long = System.currentTimeMillis(),
        accumulatedMinutes: Int = minutes
    ) {
        Log.i(TAG, "👉 [STEP 2: SERVICE TIMER STARTED] Vehicle: '$vehicleName', VehicleID: '$vehicleId', Reason: '$reason', Duration: $minutes min(s), TotalAccumulated: $accumulatedMinutes min(s)")

        val notificationId = vehicleName.hashCode()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId + 1000)

        val durationMillis = minutes * 60 * 1000L
        val targetTimeWallClock = System.currentTimeMillis() + durationMillis
        val endRealtime = SystemClock.elapsedRealtime() + durationMillis

        // 1. Post native Chronometer Countdown notification (SystemUI ticks natively, NEVER freezes!)
        val notification = buildNativeChronometerNotification(
            vehicleName,
            reason,
            targetTimeWallClock,
            vehicleId,
            startTimeMillis,
            accumulatedMinutes
        )
        notificationManager.notify(notificationId, notification)

        activeTimers[vehicleName] = endRealtime

        // 2. Schedule exact AlarmManager wakeup at endRealtime for time-up alarm
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, VehicleAlarmReceiver::class.java).apply {
            action = "COM.EPICX.RESCUE.ALARM_${vehicleName.hashCode()}"
            putExtra("vehicleName", vehicleName)
            putExtra("reason", reason)
            putExtra("vehicleId", vehicleId)
            putExtra("startTimeMillis", startTimeMillis)
            putExtra("accumulatedMinutes", accumulatedMinutes)
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(this, notificationId, alarmIntent, pendingFlags)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(targetTimeWallClock, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                Log.d(TAG, "Scheduled setAlarmClock successfully for $vehicleName")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, endRealtime, pendingIntent)
                Log.d(TAG, "Scheduled setExactAndAllowWhileIdle successfully for $vehicleName")
            } else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, endRealtime, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AlarmManager schedule error: ${e.message}")
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, endRealtime, pendingIntent)
            } catch (_: Exception) {}
        }
    }

    private fun stopVehicleTimer(vehicleName: String) {
        Log.d(TAG, "ForegroundService stopVehicleTimer for: $vehicleName")
        activeTimers.remove(vehicleName)

        VehicleAlarmReceiver.stopAlarmSound()

        val notificationId = vehicleName.hashCode()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, VehicleAlarmReceiver::class.java).apply {
            action = "COM.EPICX.RESCUE.ALARM_${vehicleName.hashCode()}"
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(this, notificationId, alarmIntent, pendingFlags)
        try {
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {}

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
        notificationManager.cancel(notificationId + 1000)

        if (activeTimers.isEmpty()) {
            stopSelf()
        }
    }

    private fun buildNativeChronometerNotification(
        vehicleName: String,
        reason: String,
        targetTimeWallClock: Long,
        vehicleId: String = "",
        startTimeMillis: Long = System.currentTimeMillis(),
        accumulatedMinutes: Int = 0
    ): Notification {
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val forceMakeActiveIntent = Intent(this, VehicleAlarmReceiver::class.java).apply {
            action = VehicleAlarmReceiver.ACTION_FORCE_MAKE_ACTIVE
            putExtra("vehicleName", vehicleName)
            putExtra("vehicleId", vehicleId)
            putExtra("startTimeMillis", startTimeMillis)
        }
        val forceMakeActivePendingIntent = PendingIntent.getBroadcast(
            this,
            vehicleName.hashCode() + 5000,
            forceMakeActiveIntent,
            pendingFlags
        )

        val totalText = if (accumulatedMinutes > 0) "Total Time Allocated: ${accumulatedMinutes} Mins | Reason: $reason" else "Reason: $reason"

        return NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Minor Maintenance: $vehicleName")
            .setContentText(totalText)
            .setWhen(targetTimeWallClock)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSound(null)
            .addAction(android.R.drawable.ic_menu_save, "🟢 Force Active/idle", forceMakeActivePendingIntent)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Vehicle Countdown Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent channel for live countdown ticks"
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(timerChannel)

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Vehicle Maintenance Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm channel when vehicle maintenance timer finishes"
                enableVibration(true)
                setSound(null, null) // Pure single-tone playback via MediaPlayer!
            }
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    override fun onDestroy() {
        VehicleAlarmReceiver.stopAlarmSound()
        super.onDestroy()
    }
}
