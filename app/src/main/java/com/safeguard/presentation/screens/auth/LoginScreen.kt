package com.safeguard.presentation.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.safeguard.presentation.navigation.Screen

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Google Sign In Launcher
    val googleSignInLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        account.idToken?.let { token -> viewModel.signInWithGoogle(token) }
                    } catch (e: ApiException) {
                        // Log error or show snackbar
                    }
                }
            }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(24.dp),
            contentAlignment = Alignment.Center
    ) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
            )

            Text(
                    text =
                            if (uiState.isCodeSent) "Enter Verification Code"
                            else "Sign in to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.isCodeSent) {
                // ========================
                // Phone Input Step
                // ========================

                // Google Sign In Button
                Button(
                        onClick = {
                            if (!uiState.isLoading) {
                                val gso =
                                        GoogleSignInOptions.Builder(
                                                        GoogleSignInOptions.DEFAULT_SIGN_IN
                                                )
                                                .requestIdToken(
                                                        context.getString(
                                                                com.safeguard
                                                                        .R
                                                                        .string
                                                                        .default_web_client_id
                                                        )
                                                )
                                                .requestEmail()
                                                .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInClient.signOut()
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading,
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF3C4043),
                                        disabledContainerColor = Color.White.copy(alpha = 0.6f),
                                        disabledContentColor = Color(0xFF3C4043).copy(alpha = 0.4f)
                                ),
                        elevation =
                                ButtonDefaults.buttonElevation(
                                        defaultElevation = 2.dp,
                                        pressedElevation = 8.dp,
                                        disabledElevation = 0.dp
                                ),
                        border =
                                if (!uiState.isLoading) BorderStroke(1.dp, Color(0xFFDADCE0))
                                else null
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF4285F4)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                                "Signing in...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF3C4043).copy(alpha = 0.6f)
                        )
                    } else {
                        Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                                "Sign in with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text("OR", style = MaterialTheme.typography.labelMedium)

                // Phone number field with +91 prefix
                OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 10) {
                                viewModel.onPhoneNumberChange(input)
                            }
                        },
                        label = { Text("Phone Number") },
                        prefix = { Text("+91 ") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        placeholder = { Text("9999999999") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                )

                Button(
                        onClick = {
                            context.findActivity()?.let {
                                viewModel.sendVerificationCode(it, uiState.phoneNumber)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading && uiState.phoneNumber.length == 10
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Send Code", fontSize = 16.sp)
                    }
                }
            } else {
                // ========================
                // OTP Verification Step
                // ========================

                // Show which number OTP was sent to
                Text(
                        text = "OTP sent to +91 ${uiState.phoneNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                        value = uiState.otp,
                        onValueChange = viewModel::onOtpChange,
                        label = { Text("Verification Code") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        placeholder = { Text("123456") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !uiState.isLockedOut
                )

                // Attempts remaining indicator
                if (uiState.otpAttemptsRemaining < 5) {
                    Text(
                            text =
                                    if (uiState.isLockedOut) "Too many failed attempts"
                                    else
                                            "${uiState.otpAttemptsRemaining} attempt${if (uiState.otpAttemptsRemaining != 1) "s" else ""} remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                    if (uiState.otpAttemptsRemaining <= 2)
                                            MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                    )
                }

                Button(
                        onClick = { viewModel.verifyOtp(uiState.otp) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled =
                                !uiState.isLoading &&
                                        uiState.otp.length == 6 &&
                                        !uiState.isLockedOut
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Verify", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Resend OTP button with countdown
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.resendCooldownSeconds > 0) {
                        Text(
                                text = "Resend OTP in ${uiState.resendCooldownSeconds}s",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        TextButton(
                                onClick = {
                                    context.findActivity()?.let { viewModel.resendOtp(it) }
                                }
                        ) {
                            Text(
                                    text = "Resend OTP",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Change number button
                TextButton(onClick = { viewModel.goBackToPhoneInput() }) {
                    Text(text = "Change Phone Number", color = MaterialTheme.colorScheme.secondary)
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                    onClick = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
            ) { Text(text = "Skip for now", color = MaterialTheme.colorScheme.secondary) }
        }
    }
}

fun android.content.Context.findActivity(): Activity? =
        when (this) {
            is Activity -> this
            is android.content.ContextWrapper -> baseContext.findActivity()
            else -> null
        }
