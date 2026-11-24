package com.epicx.apps.rescue1122dashboard

import android.content.Context

object VehicleTimerManager {
    fun startTimer(
        context: Context,
        vehicleName: String,
        reason: String,
        minutes: Int,
        vehicleId: String = "",
        startTimeMillis: Long = 0L,
        accumulatedMinutes: Int = 0
    ) {
        VehicleTimerService.startTimer(
            context,
            vehicleName,
            reason,
            minutes,
            vehicleId,
            startTimeMillis,
            accumulatedMinutes
        )
    }

    fun cancelTimer(context: Context, vehicleName: String): Boolean {
        if (VehicleTimerService.isTimerActive(vehicleName)) {
            VehicleTimerService.cancelTimer(context, vehicleName)
            return true
        }
        return false
    }
}
