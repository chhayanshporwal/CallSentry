package com.safeguard.presentation.screens.recent

import com.safeguard.domain.model.ActivityLog

data class RecentActivityUiState(
        val logs: List<ActivityLog> = emptyList(),
        val isLoading: Boolean = true
)
