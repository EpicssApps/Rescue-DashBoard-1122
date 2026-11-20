package com.epicx.apps.rescue1122dashboard

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object VehicleTimerManager {
    private val activeTimers = ConcurrentHashMap<String, CountDownTimer>()
    private const val CHANNEL_ID = "vehicle_timer_channel"

    @SuppressLint("MissingPermission")
    fun startTimer(context: Context, vehicleName: String, reason: String, minutes: Int) {
        cancelTimer(vehicleName)

        createNotificationChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val durationMillis = minutes * 60 * 1000L
        val notificationId = vehicleName.hashCode()

        val timer = object : CountDownTimer(durationMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 1000) / 60
                val secs = (millisUntilFinished / 1000) % 60
                val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle("Minor Maintenance: $vehicleName")
                    .setContentText("Reason: $reason | Remaining: $timeFormatted")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()

                notificationManager.notify(notificationId, notification)
            }

            override fun onFinish() {
                activeTimers.remove(vehicleName)

                // Play Alarm
                playAlarm(context)

                val alarmNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle("⏰ TIME UP: $vehicleName")
                    .setContentText("Minor maintenance time finished! Move vehicle to active.")
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build()

                notificationManager.notify(notificationId + 1000, alarmNotification)
            }
        }.start()

        activeTimers[vehicleName] = timer
    }

    private fun cancelTimer(vehicleName: String) {
        activeTimers[vehicleName]?.cancel()
        activeTimers.remove(vehicleName)
    }

    private fun playAlarm(context: Context) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()

            // Stop ringtone after 30 seconds automatically
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    if (ringtone?.isPlaying == true) {
                        ringtone.stop()
                    }
                } catch (_: Exception) {}
            }, 30000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Vehicle Maintenance Timers"
            val descriptionText = "Notifications and alarms for vehicle minor maintenance countdowns"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
