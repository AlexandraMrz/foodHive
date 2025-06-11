package com.example.foodhive.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LightThemeColors = lightColorScheme(
    primary = Color(0xFF388E3C),          // Fresh Green
    onPrimary = Color.White,
    secondary = Color(0xFFFF7043),        // Coral Orange
    onSecondary = Color.White,
    background = Color.Transparent,       // Transparent to show image
    onBackground = Color(0xFFF0F0F0),     // Off-white text for readability
    surface = Color(0xCC000000),          // Semi-transparent black overlay for surfaces
    onSurface = Color.White,
    error = Color(0xFFD32F2F),
    onError = Color.White,
    tertiary = Color(0xFF448AFF),
    outline = Color(0xFFBDBDBD)
)

val DarkThemeColors = darkColorScheme(
    primary = Color(0xFF81C784),           // Mint Green
    onPrimary = Color.Black,
    secondary = Color(0xFFFF8A65),         // Warm Orange
    onSecondary = Color.Black,
    background = Color.Transparent,        // Transparent to show image
    onBackground = Color(0xFFE0E0E0),      // Light gray text
    surface = Color(0xCC000000),           // Semi-transparent black overlay for surfaces
    onSurface = Color.White,
    error = Color(0xFFEF5350),
    onError = Color.Black,
    tertiary = Color(0xFF69F0AE),
    outline = Color(0xFF90A4AE)
)

@Composable
fun FoodHiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkThemeColors
        else -> LightThemeColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
