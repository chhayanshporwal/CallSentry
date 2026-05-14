package com.safeguard.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.safeguard.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val isEditMode: Boolean = false,
        val name: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val role: String? = null,
        val editedName: String = "",
        val editedEmail: String = "",
        val error: String? = null,
        val isLoggedIn: Boolean = false
)

@HiltViewModel
class ProfileViewModel
@Inject
constructor(private val firebaseAuth: FirebaseAuth, val userRepository: UserRepository) :
        ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val user = firebaseAuth.currentUser
            if (user == null) {
                _uiState.value = ProfileUiState(isLoading = false, isLoggedIn = false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isLoggedIn = true)

            val result = userRepository.getUserProfile(user.uid)
            result.fold(
                    onSuccess = { profile ->
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        name = profile?.displayName ?: user.displayName,
                                        email = profile?.email ?: user.email,
                                        phone = profile?.phoneNumber ?: user.phoneNumber,
                                        editedName = profile?.displayName ?: user.displayName ?: "",
                                        editedEmail = profile?.email ?: user.email ?: ""
                                )
                    },
                    onFailure = { _ ->
                        // Show user-friendly error, still populate with Firebase Auth data
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        name = user.displayName,
                                        email = user.email,
                                        phone = user.phoneNumber,
                                        editedName = user.displayName ?: "",
                                        editedEmail = user.email ?: "",
                                        error =
                                                "Could not load profile from server. Showing cached data."
                                )
                    }
            )
        }
    }

    fun toggleEditMode() {
        val current = _uiState.value
        if (current.isEditMode) {
            _uiState.value =
                    current.copy(
                            isEditMode = false,
                            editedName = current.name ?: "",
                            editedEmail = current.email ?: "",
                            error = null
                    )
        } else {
            _uiState.value = current.copy(isEditMode = true, error = null)
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(editedName = name)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(editedEmail = email)
    }

    fun saveProfile() {
        val user = firebaseAuth.currentUser ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val state = _uiState.value

            // Validate email format if changed
            if (state.editedEmail.isNotBlank() && !isValidEmail(state.editedEmail)) {
                _uiState.value =
                        _uiState.value.copy(
                                isSaving = false,
                                error = "Please enter a valid email address"
                        )
                return@launch
            }

            // Check email uniqueness if changed
            val emailChanged = state.editedEmail != state.email && state.editedEmail.isNotBlank()
            if (emailChanged) {
                val emailResult = userRepository.isEmailUnique(state.editedEmail, user.uid)
                emailResult.fold(
                        onSuccess = { isUnique ->
                            if (!isUnique) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isSaving = false,
                                                error =
                                                        "A user already exists with this email address"
                                        )
                                return@launch
                            }
                        },
                        onFailure = { _ ->
                            _uiState.value =
                                    _uiState.value.copy(
                                            isSaving = false,
                                            error =
                                                    "Could not verify email availability. Please try again."
                                    )
                            return@launch
                        }
                )
            }

            val updates =
                    hashMapOf<String, Any>(
                            "displayName" to state.editedName,
                            "email" to state.editedEmail
                    )

            val result = userRepository.updateUserProfile(user.uid, updates)
            result.fold(
                    onSuccess = {
                        // Update email claim in lookup collection
                        if (emailChanged) {
                            val oldEmail = state.email
                            if (!oldEmail.isNullOrBlank()) {
                                userRepository.releaseEmail(oldEmail, user.uid)
                            }
                            userRepository.claimEmail(state.editedEmail, user.uid)
                        }

                        _uiState.value =
                                _uiState.value.copy(
                                        isSaving = false,
                                        isEditMode = false,
                                        name = state.editedName,
                                        email = state.editedEmail,
                                        error = null
                                )
                    },
                    onFailure = { e ->
                        _uiState.value =
                                _uiState.value.copy(isSaving = false, error = mapFirebaseError(e))
                    }
            )
        }
    }

    fun onPhoneVerified(newPhone: String) {
        _uiState.value = _uiState.value.copy(phone = newPhone)
        loadProfile()
    }

    fun signOut() {
        firebaseAuth.signOut()
        _uiState.value = ProfileUiState(isLoading = false, isLoggedIn = false)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun mapFirebaseError(e: Throwable): String {
        val message = e.message ?: ""
        return when {
            message.contains("PERMISSION_DENIED", ignoreCase = true) ->
                    "Unable to save changes. Please check your connection and try again."
            message.contains("UNAVAILABLE", ignoreCase = true) ->
                    "Server is temporarily unavailable. Please try again later."
            message.contains("NOT_FOUND", ignoreCase = true) ->
                    "Your profile was not found. Please sign out and sign in again."
            message.contains("NETWORK", ignoreCase = true) ->
                    "No internet connection. Please check your network and try again."
            else -> "Failed to save profile. Please try again."
        }
    }
}
