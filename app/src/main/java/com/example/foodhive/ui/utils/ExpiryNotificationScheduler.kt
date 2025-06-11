package com.example.foodhive.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object ExpiryNotificationScheduler {

    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun scheduleExpiryNotifications(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).collection("products")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val expDateStr = doc.getString("expDate") ?: continue
                    val name = doc.getString("name") ?: "Unnamed Item"

                    val expDate = try {
                        formatter.parse(expDateStr)
                    } catch (e: Exception) {
                        null
                    } ?: continue

                    val now = Date()
                    val millisInDay = 1000 * 60 * 60 * 24

                    listOf(5, 3, 1).forEach { daysBefore ->
                        val diff = expDate.time - daysBefore * millisInDay - now.time
                        if (diff > 0) {
                            val delay = diff
                            val tag = "${doc.id}_$daysBefore"

                            val work = OneTimeWorkRequestBuilder<NotificationWorker>()
                                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                .setInputData(workDataOf(
                                    "type" to "expiration",
                                    "productName" to name,
                                    "daysBefore" to daysBefore
                                ))
                                .addTag(tag)
                                .build()

                            WorkManager.getInstance(context).enqueueUniqueWork(
                                tag,
                                ExistingWorkPolicy.REPLACE,
                                work
                            )
                        }
                    }
                }
            }
    }
}
