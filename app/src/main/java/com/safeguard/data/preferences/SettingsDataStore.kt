package com.safeguard.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val BLOCKING_ENABLED = booleanPreferencesKey("blocking_enabled")
        val CALL_BLOCKING_ENABLED = booleanPreferencesKey("call_blocking_enabled")
        val SMS_BLOCKING_ENABLED = booleanPreferencesKey("sms_blocking_enabled")
        val EMERGENCY_BREAKTHROUGH_ENABLED = booleanPreferencesKey("emergency_breakthrough_enabled")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        val THEME_MODE = androidx.datastore.preferences.core.intPreferencesKey("theme_mode")

        // Onboarding step tracking
        val ROLE_GRANTED = booleanPreferencesKey("role_granted")
        val PERMISSIONS_GRANTED = booleanPreferencesKey("permissions_granted")
        val AUTH_COMPLETED = booleanPreferencesKey("auth_completed")
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

    val themeMode: Flow<Int> = context.dataStore.data.map { prefs -> prefs[Keys.THEME_MODE] ?: 0 }

    // Onboarding step flows
    val isRoleGranted: Flow<Boolean> =
            context.dataStore.data.map { prefs -> prefs[Keys.ROLE_GRANTED] ?: false }

    val arePermissionsGranted: Flow<Boolean> =
            context.dataStore.data.map { prefs -> prefs[Keys.PERMISSIONS_GRANTED] ?: false }

    val isAuthCompleted: Flow<Boolean> =
            context.dataStore.data.map { prefs -> prefs[Keys.AUTH_COMPLETED] ?: false }

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
        return context.dataStore.data.map { prefs -> prefs[Keys.PIN_HASH] }.firstOrNull()
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { prefs -> prefs[Keys.FIRST_LAUNCH] = false }
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode }
    }

    // Onboarding step setters
    suspend fun setRoleGranted(granted: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.ROLE_GRANTED] = granted }
    }

    suspend fun setPermissionsGranted(granted: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.PERMISSIONS_GRANTED] = granted }
    }

    suspend fun setAuthCompleted(completed: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.AUTH_COMPLETED] = completed }
    }
}
