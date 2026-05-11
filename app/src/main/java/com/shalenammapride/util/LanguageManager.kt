package com.shalenammapride.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_prefs")

class LanguageManager(private val context: Context) {
    private val IS_KANNADA = booleanPreferencesKey("is_kannada")

    val isKannada: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_KANNADA] ?: false
    }

    suspend fun setKannada(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[IS_KANNADA] = enabled }
    }
}
