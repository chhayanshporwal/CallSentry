package com.safeguard.presentation.screens.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.domain.repository.ActivityLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RecentActivityViewModel
@Inject
constructor(private val activityLogRepository: ActivityLogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RecentActivityUiState())
    val uiState: StateFlow<RecentActivityUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            activityLogRepository.getAllLogs().collect { logs ->
                _uiState.update { it.copy(logs = logs, isLoading = false) }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { activityLogRepository.clearLogs() }
    }
}
