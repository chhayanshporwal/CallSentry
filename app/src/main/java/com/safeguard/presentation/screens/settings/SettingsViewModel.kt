package com.safeguard.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.safeguard.data.preferences.SettingsDataStore
import com.safeguard.data.repository.UserRepository
import com.safeguard.domain.repository.BlockedLogRepository
import com.safeguard.domain.repository.WhitelistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
        private val settingsDataStore: SettingsDataStore,
        private val whitelistRepository: WhitelistRepository,
        private val blockedLogRepository: BlockedLogRepository,
        private val firebaseAuth: FirebaseAuth,
        private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settingsFlow =
                    combine(
                            settingsDataStore.isBlockingEnabled,
                            settingsDataStore.isCallBlockingEnabled,
                            settingsDataStore.isSmsBlockingEnabled,
                            settingsDataStore.isEmergencyBreakthroughEnabled,
                            settingsDataStore.isPinEnabled
                    ) { blocking, calls, sms, emergency, pin ->
                        SettingsUiState(
                                isBlockingEnabled = blocking,
                                isCallBlockingEnabled = calls,
                                isSmsBlockingEnabled = sms,
                                isEmergencyBreakthroughEnabled = emergency,
                                isPinEnabled = pin
                        )
                    }

            combine(settingsFlow, settingsDataStore.themeMode) { uiState, theme ->
                uiState.copy(themeMode = theme, isLoading = false)
            }
                    .collect { state -> _uiState.value = state }
        }
    }

    fun toggleBlocking() {
        viewModelScope.launch {
            settingsDataStore.setBlockingEnabled(!_uiState.value.isBlockingEnabled)
        }
    }

    fun toggleCallBlocking() {
        viewModelScope.launch {
            settingsDataStore.setCallBlockingEnabled(!_uiState.value.isCallBlockingEnabled)
        }
    }

    fun toggleSmsBlocking() {
        viewModelScope.launch {
            settingsDataStore.setSmsBlockingEnabled(!_uiState.value.isSmsBlockingEnabled)
        }
    }

    fun toggleEmergencyBreakthrough() {
        viewModelScope.launch {
            settingsDataStore.setEmergencyBreakthroughEnabled(
                    !_uiState.value.isEmergencyBreakthroughEnabled
            )
        }
    }

    fun togglePinEnabled() {
        viewModelScope.launch { settingsDataStore.setPinEnabled(!_uiState.value.isPinEnabled) }
    }

    fun clearBlockedLogs() {
        viewModelScope.launch { blockedLogRepository.clearAll() }
    }

    fun clearWhitelist() {
        viewModelScope.launch { whitelistRepository.clearAll() }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch { settingsDataStore.setThemeMode(mode) }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                // Clear local data
                clearBlockedLogs()
                clearWhitelist()
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Logout error", e)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val user = firebaseAuth.currentUser
                val userId = user?.uid

                // Delete from Firestore via repository
                if (userId != null) {
                    userRepository.deleteUserProfile(userId)
                }

                // Clear local data
                clearBlockedLogs()
                clearWhitelist()

                // Reset settings
                settingsDataStore.setBlockingEnabled(false)
                settingsDataStore.setCallBlockingEnabled(false)
                settingsDataStore.setSmsBlockingEnabled(false)
                settingsDataStore.setEmergencyBreakthroughEnabled(false)
                settingsDataStore.setAuthCompleted(false)

                // Delete Firebase auth account last
                user?.delete()?.await()
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Delete account error", e)
            }
        }
    }
}
