package com.safeguard.presentation.screens.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.safeguard.data.preferences.SettingsDataStore
import com.safeguard.data.repository.UserRepository
import com.safeguard.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
        val isLoading: Boolean = false,
        val isCodeSent: Boolean = false,
        val verificationId: String? = null,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false,
        val phoneNumber: String = "",
        val otp: String = ""
)

@HiltViewModel
class LoginViewModel
@Inject
constructor(
        private val settingsDataStore: SettingsDataStore,
        private val userRepository: UserRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onPhoneNumberChange(number: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = number)
    }

    fun onOtpChange(otp: String) {
        _uiState.value = _uiState.value.copy(otp = otp)
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsDataStore.setFirstLaunchComplete() }
    }

    private var lastRequestTime: Long = 0
    private val RATE_LIMIT_MS = 60_000L // 60 seconds

    fun sendVerificationCode(activity: Activity, phoneNumber: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRequestTime < RATE_LIMIT_MS) {
            val remainingTime = (RATE_LIMIT_MS - (currentTime - lastRequestTime)) / 1000
            _uiState.value =
                    _uiState.value.copy(
                            errorMessage = "Please wait ${remainingTime}s before retrying"
                    )
            return
        }

        if (phoneNumber.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a phone number")
            return
        }

        lastRequestTime = currentTime

        // Prepend +91 for Indian phone numbers
        val fullPhoneNumber = "+91$phoneNumber"

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        val options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(fullPhoneNumber) // Use +91 prefixed number
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(
                                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                    override fun onVerificationCompleted(
                                            credential: PhoneAuthCredential
                                    ) {
                                        _uiState.value = _uiState.value.copy(isLoading = false)
                                        signInWithPhoneAuthCredential(credential)
                                    }

                                    override fun onVerificationFailed(e: FirebaseException) {
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        errorMessage = e.message
                                                                        ?: "Verification failed"
                                                )
                                    }

                                    override fun onCodeSent(
                                            verificationId: String,
                                            token: PhoneAuthProvider.ForceResendingToken
                                    ) {
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        isCodeSent = true,
                                                        verificationId = verificationId
                                                )
                                    }
                                }
                        )
                        .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String) {
        val verificationId = _uiState.value.verificationId ?: return

        if (otp.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter OTP")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        signInWithCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: com.google.firebase.auth.AuthCredential) {
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()

                // Create or update user profile
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val profile =
                            UserProfile(
                                    uid = currentUser.uid,
                                    phoneNumber = currentUser.phoneNumber ?: "",
                                    email = currentUser.email,
                                    displayName = currentUser.displayName,
                                    photoUrl = currentUser.photoUrl?.toString()
                            )
                    userRepository.saveUserProfile(profile)
                }

                settingsDataStore.setFirstLaunchComplete()
                settingsDataStore.setAuthCompleted(true)

                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                errorMessage = e.message ?: "Sign in failed"
                        )
            }
        }
    }
}
