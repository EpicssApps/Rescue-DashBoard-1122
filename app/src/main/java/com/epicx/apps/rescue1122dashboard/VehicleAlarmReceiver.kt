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
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

class VehicleAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "VehicleTimerDebug"
        private const val ALARM_CHANNEL_ID = "vehicle_alarm_channel_sound_v2"
        private var mediaPlayer: MediaPlayer? = null

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
        if (intent.action?.startsWith("STOP_ALARM_ACTION") == true) {
            Log.d(TAG, "Notification dismissed by user. Stopping alarm ringtone.")
            stopAlarmSound()
            return
        }

        val vehicleName = intent.getStringExtra("vehicleName") ?: "Vehicle"
        val reason = intent.getStringExtra("reason") ?: "Maintenance"
        Log.d(TAG, "==> VehicleAlarmReceiver onReceive FIRED for vehicle: $vehicleName, reason: $reason")

        VehicleTimerService.removeTimer(vehicleName)

        wakeUpAndPlayAlarm(context, vehicleName, reason)
    }

    @SuppressLint("WakelockTimeout")
    private fun wakeUpAndPlayAlarm(context: Context, vehicleName: String, reason: String) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Rescue1122:VehicleAlarmWakeLock"
            )
            wakeLock.acquire(30000L)
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

        // Stop Alarm intent when user dismisses/swipes away notification
        val stopIntent = Intent(context, VehicleAlarmReceiver::class.java).apply {
            action = "STOP_ALARM_ACTION_${vehicleName.hashCode()}"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(context, notificationId + 2000, stopIntent, pendingFlags)

        val alarmNotification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ TIME UP: $vehicleName")
            .setContentText("Minor maintenance finished ($reason)! Move vehicle to active.")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setDeleteIntent(stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(null)
            .setAutoCancel(true)
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
            }, 30000)
        } catch (e: Exception) {
            Log.e(TAG, "VehicleAlarmReceiver playAlarm error: ${e.message}")
        }
    }
}
