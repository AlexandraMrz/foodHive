package com.example.foodhive.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "recipe_preferences")

class RecipePreferencesManager(private val context: Context) {

    companion object {
        private val DIET_KEY = stringPreferencesKey("diet")
        private val EXCLUSIONS_KEY = stringPreferencesKey("exclusions")
    }

    val dietFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DIET_KEY] ?: "none"
    }

    val exclusionsFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[EXCLUSIONS_KEY] ?: ""
    }

    suspend fun setDiet(diet: String) {
        context.dataStore.edit { preferences ->
            preferences[DIET_KEY] = diet
        }
    }

    suspend fun setExclusions(exclusions: String) {
        context.dataStore.edit { preferences ->
            preferences[EXCLUSIONS_KEY] = exclusions
        }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.remove(DIET_KEY)
            preferences.remove(EXCLUSIONS_KEY)
        }
    }
}
