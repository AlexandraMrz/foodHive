package com.example.foodhive.ui.utils

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getExpirationColor(expDate: String): Color {
    if (expDate.isBlank()) return Color.LightGray

    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expiration = formatter.parse(expDate)
        val now = Date()

        val diff = (expiration.time - now.time) / (1000 * 60 * 60 * 24) // zile

        when {
            diff <= 3 -> Color(0xFFFFCDD2) // roșu
            diff <= 10 -> Color(0xFFFFF9C4) // galben
            else -> Color(0xFFC8E6C9) // verde
        }
    } catch (e: Exception) {
        Color.Gray
    }
}
