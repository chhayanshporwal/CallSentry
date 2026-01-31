package com.safeguard.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.data.preferences.SettingsDataStore
import com.safeguard.domain.repository.BlockedLogRepository
import com.safeguard.domain.repository.WhitelistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
        private val settingsDataStore: SettingsDataStore,
        private val whitelistRepository: WhitelistRepository,
        private val blockedLogRepository: BlockedLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                            settingsDataStore.isBlockingEnabled,
                            settingsDataStore.isCallBlockingEnabled,
                            settingsDataStore.isSmsBlockingEnabled,
                            settingsDataStore.isPinEnabled
                    ) { blocking, calls, sms, pin ->
                SettingsUiState(
                        isBlockingEnabled = blocking,
                        isCallBlockingEnabled = calls,
                        isSmsBlockingEnabled = sms,
                        isPinEnabled = pin,
                        isLoading = false
                )
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

    fun togglePinEnabled() {
        viewModelScope.launch { settingsDataStore.setPinEnabled(!_uiState.value.isPinEnabled) }
    }

    fun clearBlockedLogs() {
        viewModelScope.launch { blockedLogRepository.clearAll() }
    }

    fun clearWhitelist() {
        viewModelScope.launch { whitelistRepository.clearAll() }
    }
}
