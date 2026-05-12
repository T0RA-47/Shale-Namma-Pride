package com.shalenammapride.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

class LanguageManager(private val context: Context) {
    private val IS_KANNADA = booleanPreferencesKey("is_kannada")
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")

    val isKannada: Flow<Boolean> = context.dataStore.data.map { it[IS_KANNADA] ?: false }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[IS_DARK_MODE] ?: false }

    suspend fun setKannada(enabled: Boolean) {
        context.dataStore.edit { it[IS_KANNADA] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[IS_DARK_MODE] = enabled }
    }
}
