package com.safeguard.presentation.screens.settings

data class SettingsUiState(
        val isBlockingEnabled: Boolean = true,
        val isCallBlockingEnabled: Boolean = true,
        val isSmsBlockingEnabled: Boolean = true,
        val isEmergencyBreakthroughEnabled: Boolean = false,
        val isPinEnabled: Boolean = false,
        val showPinDialog: Boolean = false,
        val themeMode: Int = 0, // 0: System, 1: Light, 2: Dark
        val isLoading: Boolean = true
)
