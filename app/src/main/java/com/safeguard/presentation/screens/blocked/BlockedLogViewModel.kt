package com.safeguard.presentation.screens.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.domain.model.BlockType
import com.safeguard.domain.model.BlockedLog
import com.safeguard.domain.model.WhitelistContact
import com.safeguard.domain.repository.BlockedLogRepository
import com.safeguard.domain.repository.WhitelistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BlockedLogViewModel
@Inject
constructor(
        private val blockedLogRepository: BlockedLogRepository,
        private val whitelistRepository: WhitelistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockedLogUiState())
    val uiState: StateFlow<BlockedLogUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            blockedLogRepository.getAllLogs().collect { logs ->
                _uiState.update { state ->
                    state.copy(logs = filterLogs(logs, state.selectedFilter), isLoading = false)
                }
            }
        }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadFilteredLogs(filter)
    }

    private fun loadFilteredLogs(filter: FilterType) {
        viewModelScope.launch {
            val flow =
                    when (filter) {
                        FilterType.ALL -> blockedLogRepository.getAllLogs()
                        FilterType.CALLS -> blockedLogRepository.getLogsByType(BlockType.CALL)
                        FilterType.SMS -> blockedLogRepository.getLogsByType(BlockType.SMS)
                    }

            flow.collect { logs -> _uiState.update { it.copy(logs = logs) } }
        }
    }

    private fun filterLogs(logs: List<BlockedLog>, filter: FilterType): List<BlockedLog> {
        return when (filter) {
            FilterType.ALL -> logs
            FilterType.CALLS -> logs.filter { it.type == BlockType.CALL }
            FilterType.SMS -> logs.filter { it.type == BlockType.SMS }
        }
    }

    fun addToWhitelist(log: BlockedLog) {
        viewModelScope.launch {
            whitelistRepository.addContact(WhitelistContact(phoneNumber = log.phoneNumber))
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch { blockedLogRepository.clearAll() }
    }
}
