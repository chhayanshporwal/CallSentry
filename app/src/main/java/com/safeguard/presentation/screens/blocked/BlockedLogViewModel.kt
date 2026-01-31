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
        private val whitelistRepository: WhitelistRepository,
        private val contactUtils: com.safeguard.util.ContactUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockedLogUiState())
    val uiState: StateFlow<BlockedLogUiState> = _uiState.asStateFlow()

    private var collectionJob: kotlinx.coroutines.Job? = null

    init {
        loadLogs()
    }

    private fun loadLogs() {
        collectionJob?.cancel()
        collectionJob =
                viewModelScope.launch {
                    blockedLogRepository.getAllLogs().collect { logs ->
                        val filtered = filterLogs(logs, _uiState.value.selectedFilter)
                        val uiModels = mapToUiModels(filtered)
                        _uiState.update { state -> state.copy(logs = uiModels, isLoading = false) }
                    }
                }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update { it.copy(selectedFilter = filter) }
        loadFilteredLogs(filter)
    }

    private fun loadFilteredLogs(filter: FilterType) {
        collectionJob?.cancel()
        collectionJob =
                viewModelScope.launch {
                    val flow =
                            when (filter) {
                                FilterType.ALL -> blockedLogRepository.getAllLogs()
                                FilterType.CALLS ->
                                        blockedLogRepository.getLogsByType(BlockType.CALL)
                                FilterType.SMS -> blockedLogRepository.getLogsByType(BlockType.SMS)
                            }

                    flow.collect { logs ->
                        val uiModels = mapToUiModels(logs)
                        _uiState.update { it.copy(logs = uiModels) }
                    }
                }
    }

    private suspend fun mapToUiModels(logs: List<BlockedLog>): List<BlockedLogUiModel> {
        return logs.map { log ->
            BlockedLogUiModel(log = log, name = contactUtils.getContactName(log.phoneNumber))
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
            // We can also try to resolve name here when adding to whitelist
            val name = contactUtils.getContactName(log.phoneNumber)
            whitelistRepository.addContact(
                    WhitelistContact(phoneNumber = log.phoneNumber, displayName = name)
            )
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch { blockedLogRepository.clearAll() }
    }
}

data class BlockedLogUiModel(val log: BlockedLog, val name: String? = null)
