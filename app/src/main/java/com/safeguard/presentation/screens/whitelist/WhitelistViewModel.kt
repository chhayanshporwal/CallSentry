package com.safeguard.presentation.screens.whitelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeguard.domain.model.WhitelistContact
import com.safeguard.domain.repository.WhitelistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WhitelistViewModel @Inject constructor(private val whitelistRepository: WhitelistRepository) :
        ViewModel() {

    private val _uiState = MutableStateFlow(WhitelistUiState())
    val uiState: StateFlow<WhitelistUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            whitelistRepository.getAllContacts().collect { contacts ->
                _uiState.update { it.copy(contacts = contacts, isLoading = false) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        viewModelScope.launch {
            if (query.isBlank()) {
                whitelistRepository.getAllContacts().collect { contacts ->
                    _uiState.update { it.copy(contacts = contacts) }
                }
            } else {
                whitelistRepository.searchContacts(query).collect { contacts ->
                    _uiState.update { it.copy(contacts = contacts) }
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun addContact(phoneNumber: String, name: String?) {
        viewModelScope.launch {
            whitelistRepository.addContact(
                    WhitelistContact(
                            phoneNumber = phoneNumber,
                            displayName = name?.takeIf { it.isNotBlank() }
                    )
            )
            hideAddDialog()
        }
    }

    fun showDeleteConfirmation(contact: WhitelistContact) {
        _uiState.update { it.copy(showDeleteConfirmation = contact) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = null) }
    }

    fun deleteContact(contact: WhitelistContact) {
        viewModelScope.launch {
            whitelistRepository.removeContact(contact)
            hideDeleteConfirmation()
        }
    }
}
