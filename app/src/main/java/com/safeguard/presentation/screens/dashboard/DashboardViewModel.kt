package com.safeguard.presentation.screens.dashboard

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
class DashboardViewModel
@Inject
constructor(
        private val whitelistRepository: WhitelistRepository,
        private val blockedLogRepository: BlockedLogRepository,
        private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Combine all data streams
            combine(
                            settingsDataStore.isBlockingEnabled,
                            settingsDataStore.isCallBlockingEnabled,
                            settingsDataStore.isSmsBlockingEnabled,
                            blockedLogRepository.getBlockedCallsCountToday(),
                            blockedLogRepository.getBlockedSmsCountToday(),
                            whitelistRepository.getContactCount(),
                            whitelistRepository.getAllContacts(),
                            blockedLogRepository.getAllLogs()
                    ) { values ->
                try {
                    DashboardUiState(
                            isBlockingEnabled = (values[0] as? Boolean) ?: true,
                            isCallBlockingEnabled = (values[1] as? Boolean) ?: true,
                            isSmsBlockingEnabled = (values[2] as? Boolean) ?: true,
                            blockedCallsToday = (values[3] as? Int) ?: 0,
                            blockedSmsToday = (values[4] as? Int) ?: 0,
                            whitelistCount = (values[5] as? Int) ?: 0,
                            recentWhitelist =
                                    (values[6] as? List<*>)
                                            ?.take(5)
                                            ?.filterIsInstance<
                                                    com.safeguard.domain.model.WhitelistContact>()
                                            ?: emptyList(),
                            isLoading = false
                    )
                } catch (e: Exception) {
                    android.util.Log.e("DashboardViewModel", "Error loading dashboard data", e)
                    DashboardUiState(isLoading = false)
                }
            }
                    .collect { state -> _uiState.value = state }
        }
    }

    fun toggleBlocking() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isBlockingEnabled
            settingsDataStore.setBlockingEnabled(newValue)
        }
    }

    fun toggleCallBlocking() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isCallBlockingEnabled
            settingsDataStore.setCallBlockingEnabled(newValue)
        }
    }

    fun toggleSmsBlocking() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isSmsBlockingEnabled
            settingsDataStore.setSmsBlockingEnabled(newValue)
        }
    }

    fun addToWhitelist(log: com.safeguard.domain.model.BlockedLog) {
        viewModelScope.launch {
            whitelistRepository.addContact(
                    com.safeguard.domain.model.WhitelistContact(phoneNumber = log.phoneNumber)
            )
        }
    }
}
