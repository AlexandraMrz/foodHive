package com.example.foodhive.ui.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.foodhive.notifications.DailyTipWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

fun scheduleNotificationWorkers(context: Context, userId: String) {
    val expiryRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
        .setInputData(workDataOf("userId" to userId))
        .build()

    val tipRequest = PeriodicWorkRequestBuilder<DailyTipWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(getInitialDelay(), TimeUnit.MILLISECONDS)
        .build()

    val workManager = WorkManager.getInstance(context)
    workManager.enqueueUniquePeriodicWork("ExpiryCheck", ExistingPeriodicWorkPolicy.REPLACE, expiryRequest)
    workManager.enqueueUniquePeriodicWork("DailyTip", ExistingPeriodicWorkPolicy.REPLACE, tipRequest)
}

private fun getInitialDelay(): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 10) // 10 AM daily tip
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
    }
    return calendar.timeInMillis - System.currentTimeMillis()
}
