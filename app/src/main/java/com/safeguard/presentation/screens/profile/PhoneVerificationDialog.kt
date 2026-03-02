package com.safeguard.presentation.screens.profile

import android.app.Activity
import android.os.CountDownTimer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.safeguard.data.repository.UserRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class PhoneVerificationStep {
    PHONE_INPUT,
    OTP_INPUT
}

@Composable
fun PhoneVerificationDialog(
        currentPhone: String?,
        onDismiss: () -> Unit,
        onVerified: (String) -> Unit,
        userRepository: UserRepository? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var step by remember { mutableStateOf(PhoneVerificationStep.PHONE_INPUT) }
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var verificationId by remember { mutableStateOf("") }
    var resendCooldownSeconds by remember { mutableStateOf(0) }
    var otpAttemptsRemaining by remember { mutableStateOf(5) }
    var isLockedOut by remember { mutableStateOf(false) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var countDownTimer by remember { mutableStateOf<CountDownTimer?>(null) }

    // Clean up timer on dispose
    DisposableEffect(Unit) { onDispose { countDownTimer?.cancel() } }

    fun startCooldownTimer() {
        countDownTimer?.cancel()
        resendCooldownSeconds = 60
        countDownTimer =
                object : CountDownTimer(60_000L, 1000L) {
                            override fun onTick(millisUntilFinished: Long) {
                                resendCooldownSeconds = (millisUntilFinished / 1000).toInt()
                            }
                            override fun onFinish() {
                                resendCooldownSeconds = 0
                            }
                        }
                        .start()
    }

    fun mapVerificationError(e: Exception): String {
        val message = e.message ?: ""
        return when {
            message.contains("blocked", ignoreCase = true) ||
                    message.contains("unusual activity", ignoreCase = true) ->
                    "Too many attempts. Please try again after some time."
            message.contains("TOO_MANY_REQUESTS", ignoreCase = true) ->
                    "Too many requests. Please wait a few minutes and try again."
            message.contains("QUOTA_EXCEEDED", ignoreCase = true) ->
                    "Service temporarily unavailable. Please try again later."
            message.contains("INVALID_PHONE_NUMBER", ignoreCase = true) ->
                    "Invalid phone number. Please check and try again."
            message.contains("NETWORK", ignoreCase = true) ->
                    "No internet connection. Please check your network."
            message.contains("SESSION_EXPIRED", ignoreCase = true) ->
                    "Session expired. Please request a new OTP."
            message.contains("BILLING_NOT_ENABLED", ignoreCase = true) ->
                    "Phone verification is not available at this time."
            message.contains("credential", ignoreCase = true) ->
                    "Verification failed. Please try again."
            else -> "Something went wrong. Please try again."
        }
    }

    val callbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    scope.launch {
                        try {
                            isLoading = true
                            auth.currentUser?.updatePhoneNumber(credential)?.await()
                            val fullPhone = "+91$phoneNumber"
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                // Update phone in user profile
                                firestore
                                        .collection("users")
                                        .document(currentUser.uid)
                                        .update("phoneNumber", fullPhone)
                                        .await()

                                // Update phone claim in lookup collection
                                if (userRepository != null) {
                                    if (!currentPhone.isNullOrBlank()) {
                                        userRepository.releasePhone(currentPhone, currentUser.uid)
                                    }
                                    userRepository.claimPhone(fullPhone, currentUser.uid)
                                }
                            }
                            countDownTimer?.cancel()
                            onVerified(fullPhone)
                        } catch (e: Exception) {
                            error = mapVerificationError(e)
                        } finally {
                            isLoading = false
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isLoading = false
                    error = mapVerificationError(e)
                }

                override fun onCodeSent(
                        verId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = verId
                    resendToken = token
                    step = PhoneVerificationStep.OTP_INPUT
                    isLoading = false
                    otp = ""
                    startCooldownTimer()
                }
            }

    fun sendVerificationCode(isResend: Boolean = false) {
        scope.launch {
            try {
                isLoading = true
                error = null

                val fullPhone = "+91$phoneNumber"

                // Check phone uniqueness before sending OTP
                if (!isResend && userRepository != null) {
                    val currentUid = auth.currentUser?.uid ?: ""
                    val result = userRepository.isPhoneUnique(fullPhone, currentUid)
                    result.fold(
                            onSuccess = { isUnique ->
                                if (!isUnique) {
                                    isLoading = false
                                    error = "A user already exists with this phone number"
                                    return@launch
                                }
                            },
                            onFailure = {
                                isLoading = false
                                error = "Could not verify phone availability. Please try again."
                                return@launch
                            }
                    )
                }

                val optionsBuilder =
                        PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(fullPhone)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(context as Activity)
                                .setCallbacks(callbacks)

                if (isResend && resendToken != null) {
                    optionsBuilder.setForceResendingToken(resendToken!!)
                }

                PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
            } catch (e: Exception) {
                isLoading = false
                error = mapVerificationError(e)
            }
        }
    }

    fun verifyOtp() {
        if (isLockedOut) {
            error = "Too many failed attempts. Please request a new OTP."
            return
        }

        scope.launch {
            try {
                isLoading = true
                error = null
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                auth.currentUser?.updatePhoneNumber(credential)?.await()

                val fullPhone = "+91$phoneNumber"
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    firestore
                            .collection("users")
                            .document(currentUser.uid)
                            .update("phoneNumber", fullPhone)
                            .await()

                    // Update phone claim in lookup collection
                    if (userRepository != null) {
                        if (!currentPhone.isNullOrBlank()) {
                            userRepository.releasePhone(currentPhone, currentUser.uid)
                        }
                        userRepository.claimPhone(fullPhone, currentUser.uid)
                    }
                }

                countDownTimer?.cancel()
                onVerified(fullPhone)
            } catch (e: Exception) {
                isLoading = false
                otpAttemptsRemaining--
                if (otpAttemptsRemaining <= 0) {
                    isLockedOut = true
                    error = "Too many failed attempts. Please request a new OTP."
                } else {
                    error =
                            "Invalid OTP. $otpAttemptsRemaining attempt${if (otpAttemptsRemaining != 1) "s" else ""} remaining."
                }
            }
        }
    }

    AlertDialog(
            onDismissRequest = {
                if (!isLoading) {
                    countDownTimer?.cancel()
                    onDismiss()
                }
            },
            title = {
                Text(
                        if (step == PhoneVerificationStep.PHONE_INPUT) "Verify Phone Number"
                        else "Enter OTP"
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (step) {
                        PhoneVerificationStep.PHONE_INPUT -> {
                            Text(
                                    "Enter your 10-digit phone number",
                                    style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = {
                                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                            phoneNumber = it
                                            error = null
                                        }
                                    },
                                    label = { Text("Phone Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                                    prefix = { Text("+91 ") },
                                    keyboardOptions =
                                            KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = error != null,
                                    enabled = !isLoading
                            )
                        }
                        PhoneVerificationStep.OTP_INPUT -> {
                            Text(
                                    "Enter the 6-digit code sent to +91 $phoneNumber",
                                    style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                    value = otp,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                            otp = it
                                            error = null
                                        }
                                    },
                                    label = { Text("OTP") },
                                    keyboardOptions =
                                            KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = error != null,
                                    enabled = !isLoading && !isLockedOut
                            )

                            // Attempts remaining
                            if (otpAttemptsRemaining < 5) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                        text =
                                                if (isLockedOut) "Too many failed attempts"
                                                else
                                                        "$otpAttemptsRemaining attempt${if (otpAttemptsRemaining != 1) "s" else ""} remaining",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color =
                                                if (otpAttemptsRemaining <= 2)
                                                        MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Resend row
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (resendCooldownSeconds > 0) {
                                    Text(
                                            text = "Resend OTP in ${resendCooldownSeconds}s",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    TextButton(
                                            onClick = { sendVerificationCode(isResend = true) },
                                            enabled = !isLoading
                                    ) { Text("Resend OTP", fontWeight = FontWeight.SemiBold) }
                                }
                            }
                        }
                    }

                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            when (step) {
                                PhoneVerificationStep.PHONE_INPUT -> {
                                    if (phoneNumber.length == 10) {
                                        sendVerificationCode()
                                    } else {
                                        error = "Please enter a valid 10-digit number"
                                    }
                                }
                                PhoneVerificationStep.OTP_INPUT -> {
                                    if (otp.length == 6) {
                                        verifyOtp()
                                    } else {
                                        error = "Please enter a valid 6-digit OTP"
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && !isLockedOut
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                                if (step == PhoneVerificationStep.PHONE_INPUT) "Send OTP"
                                else "Verify"
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                        onClick = {
                            countDownTimer?.cancel()
                            onDismiss()
                        },
                        enabled = !isLoading
                ) { Text("Cancel") }
            }
    )
}
