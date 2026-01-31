package com.safeguard.presentation.screens.blocked

import com.safeguard.domain.model.BlockedLog

data class BlockedLogUiState(
        val logs: List<BlockedLog> = emptyList(),
        val selectedFilter: FilterType = FilterType.ALL,
        val isLoading: Boolean = true
)

enum class FilterType {
    ALL,
    CALLS,
    SMS
}
