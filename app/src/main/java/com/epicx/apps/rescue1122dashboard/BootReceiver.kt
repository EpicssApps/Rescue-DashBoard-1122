package com.epicx.apps.rescue1122dashboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i("Rescue1122Sync", "🔄 [BOOT COMPLETED] Phone restarted. Re-verifying WorkManager 15-minute periodic sync...")
            VehicleSyncWorker.schedulePeriodicSync(context)
        }
    }
}
