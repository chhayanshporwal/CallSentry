package com.safeguard.presentation.screens.dashboard

import com.safeguard.domain.model.BlockedLog
import com.safeguard.domain.model.WhitelistContact

data class DashboardUiState(
        val isBlockingEnabled: Boolean = true,
        val isCallBlockingEnabled: Boolean = true,
        val isSmsBlockingEnabled: Boolean = true,
        val blockedCallsToday: Int = 0,
        val blockedSmsToday: Int = 0,
        val whitelistCount: Int = 0,
        val recentWhitelist: List<WhitelistContact> = emptyList(),
        val recentBlockedLogs: List<BlockedLog> = emptyList(),
        val isLoading: Boolean = true
)
