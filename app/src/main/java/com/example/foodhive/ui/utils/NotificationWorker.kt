package com.example.foodhive.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.foodhive.R

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val type = inputData.getString("type") ?: "generic"

        val message = when (type) {
            "expiration" -> {
                val productName = inputData.getString("productName") ?: "an item"
                val daysBefore = inputData.getInt("daysBefore", 0)
                if (daysBefore == 0) {
                    "$productName expires today! ⚠️"
                } else {
                    "$productName will expire in $daysBefore day(s). Check it soon! 🕐"
                }
            }

            "tip" -> "💡 Tip of the Day: Keep bananas away from other fruits to slow ripening."
            else -> "This is your FoodHive notification."
        }

        val title = when (type) {
            "expiration" -> "📦 Expiring Item"
            "tip" -> "💡 Daily Tip"
            else -> "FoodHive"
        }

        showNotification(title, message)
        return Result.success()
    }

    private fun showNotification(title: String, content: String) {
        val channelId = "foodhive_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "FoodHive Alerts"
            val descriptionText = "Expiration and tip reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                builder.build()
            )
        }
    }
}
