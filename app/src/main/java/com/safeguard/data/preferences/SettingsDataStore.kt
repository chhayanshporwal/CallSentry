package com.safeguard.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(private val context: Context) {
    private object Keys {
        val BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
        val CALL_BLOCKING_ENABLED = booleanPreferencesKey("call_blocking_enabled")
        val SMS_BLOCKING_ENABLED = booleanPreferencesKey("sms_blocking_enabled")
        val EMERGENCY_BREAKTHROUGH_ENABLED = booleanPreferencesKey("emergency_breakthrough_enabled")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    val isBlockingEnabled: Flow<Boolean> =
            context.dataStore.data.map { prefs -> prefs[Keys.BLOCKING_ENABLED] ?: true }

    val isCallBlockingEnabled: Flow<Boolean> =
            context.dataStore.data.map { prefs -> prefs[Keys.CALL_BLOCKING_ENABLED] ?: true }

    val isSmsBlockingEnabled: Flow<Boolean> =
            context.dataStore.data.map { prefs -> prefs[Keys.SMS_BLOCKING_ENABLED] ?: true }

    val isEmergencyBreakthroughEnabled: Flow<Boolean> =
            context.dataStore.data.map { prefs ->
                prefs[Keys.EMERGENCY_BREAKTHROUGH_ENABLED] ?: false
            }

    val isPinEnabled: Flow<Boolean> =
            context.dataStore.data.map { prefs -> prefs[Keys.PIN_ENABLED] ?: false }

    val isFirstLaunch: Flow<Boolean> =
            context.dataStore.data.map { prefs -> prefs[Keys.FIRST_LAUNCH] ?: true }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.BLOCKING_ENABLED] = enabled }
    }

    suspend fun setCallBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.CALL_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setSmsBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SMS_BLOCKING_ENABLED] = enabled }
    }

    suspend fun setEmergencyBreakthroughEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.EMERGENCY_BREAKTHROUGH_ENABLED] = enabled }
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.PIN_ENABLED] = enabled }
    }

    suspend fun setPinHash(hash: String) {
        context.dataStore.edit { prefs -> prefs[Keys.PIN_HASH] = hash }
    }

    suspend fun getPinHash(): String? {
        var hash: String? = null
        context.dataStore.edit { prefs -> hash = prefs[Keys.PIN_HASH] }
        return hash
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { prefs -> prefs[Keys.FIRST_LAUNCH] = false }
    }
}
