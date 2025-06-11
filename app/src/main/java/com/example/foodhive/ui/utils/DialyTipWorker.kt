package com.example.foodhive.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.foodhive.ui.utils.showNotification

class DailyTipWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val tips = listOf(
            "🧊 Organize your fridge by expiration date.",
            "🍽️ Try a recipe using leftovers today!",
            "🌿 Freeze herbs in olive oil to preserve freshness.",
            "📅 Plan your meals to reduce waste.",
            "🥫 Use opened canned goods within 3 days."
        )
        val tip = tips.random()
        showNotification(applicationContext, "💡 Tip of the Day", tip, 1001)
        return Result.success()
    }
}
