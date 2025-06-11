package com.example.foodhive.ui.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.foodhive.auth.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFERENCES_NAME = "notification_settings"
private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

//preferences for theme

class ThemePreferencesManager(private val context: Context) {

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeModeFlow: Flow<String> = context.themeDataStore.data
        .map { prefs -> prefs[THEME_MODE] ?: AppThemeMode.SYSTEM.name }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[THEME_MODE] = mode.name
        }
    }
}

// context extension
val Context.dataStore by preferencesDataStore(name = PREFERENCES_NAME)

class NotificationPreferencesManager(private val context: Context) {

    companion object {
        val EXPIRATION_ALERTS = booleanPreferencesKey("expiration_alerts")
        val DAILY_TIP = booleanPreferencesKey("daily_tip")
    }

    val expirationAlertsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[EXPIRATION_ALERTS] ?: true }

    val dailyTipFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DAILY_TIP] ?: true }

    suspend fun setExpirationAlerts(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[EXPIRATION_ALERTS] = enabled
        }
    }

    suspend fun setDailyTip(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_TIP] = enabled
        }
    }
}


