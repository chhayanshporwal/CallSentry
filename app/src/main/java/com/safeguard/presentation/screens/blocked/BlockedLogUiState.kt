package com.safeguard.presentation.screens.blocked

data class BlockedLogUiState(
        val logs: List<BlockedLogUiModel> = emptyList(),
        val selectedFilter: FilterType = FilterType.ALL,
        val isLoading: Boolean = true
)

enum class FilterType {
    ALL,
    CALLS,
    SMS
}
