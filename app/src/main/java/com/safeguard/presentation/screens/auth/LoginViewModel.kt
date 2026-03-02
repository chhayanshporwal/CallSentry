package com.safeguard.presentation.screens.auth

import android.app.Activity
import android.os.CountDownTimer
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
        val otp: String = "",
        val resendCooldownSeconds: Int = 0,
        val otpAttemptsRemaining: Int = 5,
        val isLockedOut: Boolean = false
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

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var countDownTimer: CountDownTimer? = null

    companion object {
        private const val MAX_OTP_ATTEMPTS = 5
        private const val COOLDOWN_SECONDS = 60L
    }

    fun onPhoneNumberChange(number: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = number, errorMessage = null)
    }

    fun onOtpChange(otp: String) {
        if (otp.length <= 6 && otp.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(otp = otp, errorMessage = null)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { settingsDataStore.setFirstLaunchComplete() }
    }

    fun goBackToPhoneInput() {
        countDownTimer?.cancel()
        _uiState.value =
                _uiState.value.copy(
                        isCodeSent = false,
                        otp = "",
                        verificationId = null,
                        errorMessage = null,
                        resendCooldownSeconds = 0,
                        otpAttemptsRemaining = MAX_OTP_ATTEMPTS,
                        isLockedOut = false
                )
    }

    fun sendVerificationCode(activity: Activity, phoneNumber: String) {
        if (phoneNumber.isBlank() || phoneNumber.length != 10 || !phoneNumber.all { it.isDigit() }
        ) {
            _uiState.value =
                    _uiState.value.copy(errorMessage = "Please enter a valid 10-digit phone number")
            return
        }

        val fullPhoneNumber = "+91$phoneNumber"

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        sendFirebaseVerificationCode(activity, fullPhoneNumber, false)
    }

    fun resendOtp(activity: Activity) {
        if (_uiState.value.resendCooldownSeconds > 0) return

        val phoneNumber = _uiState.value.phoneNumber
        val fullPhoneNumber = "+91$phoneNumber"

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        sendFirebaseVerificationCode(activity, fullPhoneNumber, true)
    }

    private fun sendFirebaseVerificationCode(
            activity: Activity,
            fullPhoneNumber: String,
            isResend: Boolean
    ) {
        val optionsBuilder =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(fullPhoneNumber)
                        .setTimeout(COOLDOWN_SECONDS, TimeUnit.SECONDS)
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
                                                        errorMessage = mapFirebaseAuthError(e)
                                                )
                                    }

                                    override fun onCodeSent(
                                            verificationId: String,
                                            token: PhoneAuthProvider.ForceResendingToken
                                    ) {
                                        resendToken = token
                                        _uiState.value =
                                                _uiState.value.copy(
                                                        isLoading = false,
                                                        isCodeSent = true,
                                                        verificationId = verificationId,
                                                        otp = "",
                                                        otpAttemptsRemaining =
                                                                if (!isResend) MAX_OTP_ATTEMPTS
                                                                else
                                                                        _uiState.value
                                                                                .otpAttemptsRemaining
                                                )
                                        startCooldownTimer()
                                    }
                                }
                        )

        // Use resend token if available and this is a resend
        if (isResend && resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken!!)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun startCooldownTimer() {
        countDownTimer?.cancel()
        _uiState.value = _uiState.value.copy(resendCooldownSeconds = COOLDOWN_SECONDS.toInt())

        countDownTimer =
                object : CountDownTimer(COOLDOWN_SECONDS * 1000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                resendCooldownSeconds =
                                                        (millisUntilFinished / 1000).toInt()
                                        )
                            }

                            override fun onFinish() {
                                _uiState.value = _uiState.value.copy(resendCooldownSeconds = 0)
                            }
                        }
                        .start()
    }

    fun verifyOtp(otp: String) {
        val verificationId = _uiState.value.verificationId ?: return

        if (_uiState.value.isLockedOut) {
            _uiState.value =
                    _uiState.value.copy(
                            errorMessage = "Too many failed attempts. Please request a new OTP."
                    )
            return
        }

        if (otp.isBlank() || otp.length != 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid 6-digit OTP")
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
        signInWithCredential(credential, isPhoneAuth = true)
    }

    private fun signInWithCredential(
            credential: com.google.firebase.auth.AuthCredential,
            isPhoneAuth: Boolean = false
    ) {
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

                countDownTimer?.cancel()
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                val remaining = _uiState.value.otpAttemptsRemaining - 1
                val isLocked = remaining <= 0

                _uiState.value =
                        _uiState.value.copy(
                                isLoading = false,
                                otpAttemptsRemaining = maxOf(remaining, 0),
                                isLockedOut = isLocked,
                                errorMessage =
                                        when {
                                            isLocked ->
                                                    "Too many failed attempts. Please request a new OTP."
                                            isPhoneAuth ->
                                                    "Invalid OTP. $remaining attempt${if (remaining != 1) "s" else ""} remaining."
                                            else -> mapFirebaseAuthError(e)
                                        }
                        )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    private fun mapFirebaseAuthError(e: Exception): String {
        val message = e.message ?: ""
        return when {
            message.contains("blocked", ignoreCase = true) ||
                    message.contains("unusual activity", ignoreCase = true) ->
                    "Too many attempts. Please try again after some time."
            message.contains("BLOCKING_FUNCTION", ignoreCase = true) ->
                    "Authentication is temporarily blocked. Please try again later."
            message.contains("INVALID_VERIFICATION_CODE", ignoreCase = true) ->
                    "The verification code is incorrect. Please try again."
            message.contains("SESSION_EXPIRED", ignoreCase = true) ->
                    "Verification session has expired. Please request a new OTP."
            message.contains("TOO_MANY_REQUESTS", ignoreCase = true) ->
                    "Too many requests. Please wait a few minutes and try again."
            message.contains("QUOTA_EXCEEDED", ignoreCase = true) ->
                    "Service temporarily unavailable. Please try again later."
            message.contains("NETWORK", ignoreCase = true) ->
                    "No internet connection. Please check your network."
            message.contains("INVALID_PHONE_NUMBER", ignoreCase = true) ->
                    "Invalid phone number. Please check and try again."
            message.contains("BILLING_NOT_ENABLED", ignoreCase = true) ->
                    "Phone authentication is not available at this time."
            message.contains("credential", ignoreCase = true) -> "Sign in failed. Please try again."
            else -> "Something went wrong. Please try again."
        }
    }
}
