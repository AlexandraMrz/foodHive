package com.example.foodhive.ui.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class ExpiryCheckWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val userId = inputData.getString("userId")

        if (userId.isNullOrEmpty()) {
            Log.e("ExpiryCheckWorker", "❌ userId is null or empty.")
            return Result.failure()
        }

        val db = FirebaseFirestore.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Date()

        try {
            Log.d("ExpiryCheckWorker", "✅ Starting check for userId = $userId")

            val snapshot = db.collection("users").document(userId).collection("products").get().await()

            for (doc in snapshot.documents) {
                val name = doc.getString("name") ?: continue
                val expDateStr = doc.getString("expDate") ?: continue
                val expDate = formatter.parse(expDateStr) ?: continue

                val diff = ((expDate.time - now.time).toDouble() / (1000 * 60 * 60 * 24)).roundToInt()
                Log.d("ExpiryCheckWorker", "🔍 Product: $name | ExpDate: $expDateStr | Diff: $diff")

                when (diff) {
                    5 -> showNotification(applicationContext, "🕔 5 days left", "$name expires in 5 days", name.hashCode())
                    3 -> showNotification(applicationContext, "⚠️ 3 days left", "$name expires in 3 days", name.hashCode())
                    1 -> showNotification(applicationContext, "⏰ Tomorrow!", "$name expires tomorrow!", name.hashCode())
                }
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e("ExpiryCheckWorker", "❌ Worker failed: ${e.message}", e)
            return Result.failure()
        }
    }
}
