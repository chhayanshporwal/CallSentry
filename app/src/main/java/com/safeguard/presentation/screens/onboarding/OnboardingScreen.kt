package com.safeguard.presentation.screens.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.safeguard.presentation.navigation.Screen

@Composable
fun OnboardingScreen(
        navController: NavController,
        viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Role Launcher
    val roleLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
            ) { viewModel.checkRoleStatus() }

    // Permission Launcher
    val permissionLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { viewModel.checkPermissionStatus() }

    LaunchedEffect(uiState.isOnboardingComplete) {
        if (uiState.isOnboardingComplete) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            // Step Content
            when (uiState.currentStep) {
                OnboardingStep.WELCOME -> {
                    OnboardingPage(
                            title = "Welcome to Call Sentry",
                            description =
                                    "Your personal shield against spam calls and messages. Take back control of your phone.",
                            icon = Icons.Default.Security,
                            buttonText = "Get Started",
                            onButtonClick = { viewModel.nextStep() }
                    )
                }
                OnboardingStep.DEFAULT_ROLE -> {
                    OnboardingPage(
                            title = "Enable Protection",
                            description =
                                    "To block spam calls effectively, Call Sentry needs to be your default Caller ID & Spam app.",
                            icon = Icons.Default.Shield,
                            buttonText = "Set as Default",
                            onButtonClick = { viewModel.requestRole(roleLauncher) }
                    )
                }
                OnboardingStep.PERMISSIONS -> {
                    OnboardingPage(
                            title = "Grant Permissions",
                            description =
                                    "We need access to your Contacts and Logs to verify callers and sync your trusted numbers.",
                            icon = Icons.Default.Check,
                            buttonText = "Allow Access",
                            onButtonClick = { viewModel.requestPermissions(permissionLauncher) }
                    )
                }
                OnboardingStep.LOGIN -> {
                    // Placeholder for Phase 2 Auth
                    OnboardingPage(
                            title = "Sync Profile",
                            description =
                                    "Sign in to backup your whitelist and settings. (Coming Soon)",
                            icon = Icons.Default.Security, // Use a placeholder icon
                            buttonText = "Skip for Now", // Skip logic for MVP
                            onButtonClick = { viewModel.completeOnboarding() }
                    )
                }
            }
        }

        // Progress Indicators
        Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OnboardingStep.values().forEach { step ->
                Box(
                        modifier =
                                Modifier.size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                                if (step == uiState.currentStep)
                                                        MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                        )
                )
            }
        }
    }
}

@Composable
fun OnboardingPage(
        title: String,
        description: String,
        icon: ImageVector,
        buttonText: String,
        onButtonClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
                modifier =
                        Modifier.size(120.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .padding(32.dp),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}
