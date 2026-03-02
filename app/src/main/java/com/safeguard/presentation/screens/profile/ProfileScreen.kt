package com.safeguard.presentation.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.safeguard.presentation.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showPhoneDialog by remember { mutableStateOf(false) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Profile") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            if (uiState.isLoggedIn && !uiState.isLoading && uiState.error == null) {
                                IconButton(
                                        onClick = {
                                            if (uiState.isEditMode) {
                                                viewModel.saveProfile()
                                            } else {
                                                viewModel.toggleEditMode()
                                            }
                                        },
                                        enabled = !uiState.isSaving
                                ) {
                                    if (uiState.isSaving) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                                imageVector =
                                                        if (uiState.isEditMode) Icons.Default.Check
                                                        else Icons.Default.Edit,
                                                contentDescription =
                                                        if (uiState.isEditMode) "Save" else "Edit"
                                        )
                                    }
                                }
                                if (uiState.isEditMode) {
                                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                                    }
                                }
                            }
                        }
                )
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    // Loading State
                    Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading profile...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                uiState.error != null && !uiState.isEditMode -> {
                    // Error State
                    Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                text = uiState.error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.loadProfile() }) { Text("Retry") }
                    }
                }
                uiState.isLoggedIn -> {
                    // Success State
                    Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar
                        Box(
                                modifier =
                                        Modifier.size(100.dp).clip(CircleShape).background(Primary),
                                contentAlignment = Alignment.Center
                        ) {
                            Text(
                                    text = getInitials(uiState.name ?: "User"),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Name
                        Text(
                                text =
                                        if (uiState.isEditMode) "Editing Profile"
                                        else (uiState.name ?: "Not set"),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Profile Info - Editable
                        if (uiState.isEditMode) {
                            // Name Field
                            OutlinedTextField(
                                    value = uiState.editedName,
                                    onValueChange = { viewModel.onNameChange(it) },
                                    label = { Text("Name") },
                                    leadingIcon = { Icon(Icons.Default.Person, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Email Field
                            OutlinedTextField(
                                    value = uiState.editedEmail,
                                    onValueChange = { viewModel.onEmailChange(it) },
                                    label = { Text("Email") },
                                    leadingIcon = { Icon(Icons.Default.Email, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Phone Edit Button
                            OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { showPhoneDialog = true }
                            ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                            Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = Primary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                "Phone",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                                uiState.phone ?: "Not set",
                                                style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Icon(Icons.Default.Edit, null, tint = Primary)
                                }
                            }

                            // Show error in edit mode
                            AnimatedVisibility(visible = uiState.error != null) {
                                Text(
                                        text = uiState.error ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        } else {
                            // View Mode - Static Cards
                            ProfileInfoCard(
                                    icon = Icons.Default.Email,
                                    label = "Email",
                                    value = uiState.email ?: "Not set"
                            )

                            ProfileInfoCard(
                                    icon = Icons.Default.Phone,
                                    label = "Phone",
                                    value = uiState.phone ?: "Not set"
                            )
                        }

                        uiState.role?.let { role ->
                            Spacer(modifier = Modifier.height(8.dp))
                            ProfileInfoCard(
                                    icon = Icons.Default.Person,
                                    label = "Role",
                                    value = role.replaceFirstChar { it.uppercase() }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Sign Out Button
                        Button(
                                onClick = {
                                    viewModel.signOut()
                                    navController.popBackStack()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                        )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out")
                        }
                    }
                }
                else -> {
                    // Not signed in
                    Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Not signed in", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text = "Please sign in to view your profile",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(42.dp))
                        Button(
                                onClick = { navController.navigate("login") },
                                modifier = Modifier.fillMaxWidth(0.618f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In")
                        }
                    }
                }
            }

            // Phone Verification Dialog
            if (showPhoneDialog) {
                PhoneVerificationDialog(
                        currentPhone = uiState.phone,
                        onDismiss = { showPhoneDialog = false },
                        onVerified = { newPhone ->
                            showPhoneDialog = false
                            viewModel.onPhoneVerified(newPhone)
                        }
                )
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        label: String,
        value: String
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> "${parts[0].take(1)}${parts.last().take(1)}".uppercase()
    }
}
