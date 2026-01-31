package com.safeguard.presentation.screens.settings

data class SettingsUiState(
        val isBlockingEnabled: Boolean = true,
        val isCallBlockingEnabled: Boolean = true,
        val isSmsBlockingEnabled: Boolean = true,
        val isPinEnabled: Boolean = false,
        val showPinDialog: Boolean = false,
        val isLoading: Boolean = true
)
