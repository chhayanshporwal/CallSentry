package com.safeguard.presentation.screens.profile

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

enum class PhoneVerificationStep {
    PHONE_INPUT,
    OTP_INPUT
}

@Composable
fun PhoneVerificationDialog(
    currentPhone: String?,
    onDismiss: () -> Unit,
    onVerified: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    
    var step by remember { mutableStateOf(PhoneVerificationStep.PHONE_INPUT)  }
    var phoneNumber by remember { mutableStateOf("")  }
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var verificationId by remember { mutableStateOf("") }
    
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-verify if possible
            scope.launch {
                try {
                    isLoading = true
                    auth.currentUser?.updatePhoneNumber(credential)?.await()
                    val fullPhone = "+91$phoneNumber"
                    firestore.collection("users")
                        .document(auth.currentUser!!.uid)
                        .update("phoneNumber", fullPhone)
                        .await()
                    onVerified(fullPhone)
                } catch (e: Exception) {
                    error = "Verification failed: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            isLoading = false
            error = "Verification failed: ${e.message}"
        }

        override fun onCodeSent(verId: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = verId
            step = PhoneVerificationStep.OTP_INPUT
            isLoading = false
        }
    }
    
    suspend fun checkPhoneUnique(phone: String): Boolean {
        val snapshot = firestore.collection("users")
            .whereEqualTo("phoneNumber", phone)
            .get()
            .await()
        
        // Check if phone exists for a different user
        return snapshot.documents.all { doc ->  
            doc.id == auth.currentUser?.uid
        }
    }
    
    fun sendVerificationCode() {
        scope.launch {
            try {
                isLoading = true
                error = null
                
                val fullPhone = "+91$phoneNumber"
                
                // Check uniqueness
                if (!checkPhoneUnique(fullPhone)) {
                    isLoading = false
                    error = "This number is already registered to another account"
                    return@launch
                }
                
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(fullPhone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(context as Activity)
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                isLoading = false
                error = "Error: ${e.message}"
            }
        }
    }
    
    fun verifyOtp() {
        scope.launch {
            try {
                isLoading = true
                error = null
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                auth.currentUser?.updatePhoneNumber(credential)?.await()
                
                val fullPhone = "+91$phoneNumber"
                firestore.collection("users")
                    .document(auth.currentUser!!.uid)
                    .update("phoneNumber", fullPhone)
                    .await()
                    
                onVerified(fullPhone)
            } catch (e: Exception) {
                isLoading = false
                error = "Invalid OTP: ${e.message}"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { 
            Text(if (step == PhoneVerificationStep.PHONE_INPUT) "Verify Phone Number" else "Enter OTP") 
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = error != null,
                            enabled = !isLoading
                        )
                    }
                    
                    PhoneVerificationStep.OTP_INPUT -> {
                        Text(
                            "Enter the 6-digit code sent to +91$phoneNumber",
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = error != null,
                            enabled = !isLoading
                        )
                    }
                }
                
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        error!!,
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
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (step == PhoneVerificationStep.PHONE_INPUT) "Send OTP" else "Verify")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
