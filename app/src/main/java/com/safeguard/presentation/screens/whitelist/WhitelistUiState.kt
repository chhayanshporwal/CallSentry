package com.safeguard.presentation.screens.whitelist

import com.safeguard.domain.model.WhitelistContact

data class WhitelistUiState(
        val contacts: List<WhitelistContact> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = true,
        val showAddDialog: Boolean = false,
        val showDeleteConfirmation: WhitelistContact? = null
)
