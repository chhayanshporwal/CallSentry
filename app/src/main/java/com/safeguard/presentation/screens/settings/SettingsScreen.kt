package com.safeguard.presentation.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.presentation.common.simpleVerticalScrollbar
import com.safeguard.presentation.theme.Error
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
        viewModel: SettingsViewModel = hiltViewModel(),
        onNavigateToOnboarding: () -> Unit,
        onNavigateToLogin: () -> Unit = {},
        onNavigateToPrivacyPolicy: () -> Unit = {}
) {
        val uiState by viewModel.uiState.collectAsState()
        var showClearLogsDialog by remember { mutableStateOf(false) }
        var showClearWhitelistDialog by remember { mutableStateOf(false) }
        var showThemeDialog by remember { mutableStateOf(false) }
        var showDeleteAccountDialog by remember { mutableStateOf(false) }

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        val scrollState = rememberScrollState()
        Box(modifier = Modifier.fillMaxSize()) {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                                        .simpleVerticalScrollbar(scrollState)
                                        .verticalScroll(scrollState)
                                        .padding(16.dp)
                ) {
                        // Header
                        Text(
                                text = "Settings",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                                text = "Configure your protection",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Appearance Section
                        SettingsSection(title = "Appearance") {
                                SettingsClickableItem(
                                        icon =
                                                Icons.Default
                                                        .Info, // Placeholder for Palette/Theme icon
                                        title = "Theme",
                                        subtitle =
                                                when (uiState.themeMode) {
                                                        1 -> "Light"
                                                        2 -> "Dark"
                                                        else -> "System Default"
                                                },
                                        onClick = { showThemeDialog = true }
                                )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Protection Settings Section
                        SettingsSection(title = "Protection") {
                                SettingsSwitchItem(
                                        icon = Icons.Default.Shield,
                                        title = "Enable Protection",
                                        subtitle = "Block all unknown calls and SMS",
                                        isChecked = uiState.isBlockingEnabled,
                                        onCheckedChange = { viewModel.toggleBlocking() }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                                SettingsSwitchItem(
                                        icon = Icons.Default.Call,
                                        title = "Block Calls",
                                        subtitle = "Block incoming calls from unknown numbers",
                                        isChecked = uiState.isCallBlockingEnabled,
                                        onCheckedChange = { viewModel.toggleCallBlocking() },
                                        enabled = uiState.isBlockingEnabled
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                                SettingsSwitchItem(
                                        icon = Icons.AutoMirrored.Filled.Message,
                                        title = "Block SMS",
                                        subtitle = "Block incoming SMS from unknown numbers",
                                        isChecked = uiState.isSmsBlockingEnabled,
                                        onCheckedChange = { viewModel.toggleSmsBlocking() },
                                        enabled = uiState.isBlockingEnabled
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                                SettingsSwitchItem(
                                        icon =
                                                Icons.Default
                                                        .Shield, // Re-using Shield or maybe another
                                        // icon
                                        // like NotificationsActive
                                        title = "Emergency Breakthrough",
                                        subtitle = "Allow call if number calls 3+ times in 5 mins",
                                        isChecked = uiState.isEmergencyBreakthroughEnabled,
                                        onCheckedChange = {
                                                viewModel.toggleEmergencyBreakthrough()
                                        },
                                        enabled = uiState.isBlockingEnabled
                                )
                        }

                        // Permissions Section
                        SettingsSection(title = "Permissions") {
                                SettingsClickableItem(
                                        icon = Icons.Default.Shield,
                                        title = "Setup Permissions",
                                        subtitle =
                                                "Grant necessary permissions for full protection",
                                        onClick = {
                                                scope.launch {
                                                        val hasAllPermissions =
                                                                checkAllPermissionsGranted(context)
                                                        if (hasAllPermissions) {
                                                                snackbarHostState.showSnackbar(
                                                                        "All permissions already configured ✓",
                                                                        duration =
                                                                                SnackbarDuration
                                                                                        .Short
                                                                )
                                                        } else {
                                                                onNavigateToOnboarding()
                                                        }
                                                }
                                        }
                                )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Data Section
                        SettingsSection(title = "Data") {
                                SettingsClickableItem(
                                        icon = Icons.Default.Block,
                                        title = "Clear Blocked Logs",
                                        subtitle = "Delete all blocked call and SMS history",
                                        onClick = { showClearLogsDialog = true },
                                        isDestructive = true
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                                SettingsClickableItem(
                                        icon = Icons.Default.People,
                                        title = "Clear Whitelist",
                                        subtitle = "Remove all whitelisted contacts",
                                        onClick = { showClearWhitelistDialog = true },
                                        isDestructive = true
                                )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Account Section - Only show if user is logged in
                        val isUserLoggedIn =
                                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser !=
                                        null
                        if (isUserLoggedIn) {
                                SettingsSection(title = "Account") {
                                        SettingsClickableItem(
                                                icon = Icons.Default.Info, // Using placeholder,
                                                // consider logout
                                                // icon
                                                title = "Logout",
                                                subtitle = "Sign out of your account",
                                                onClick = {
                                                        viewModel.logout()
                                                        onNavigateToLogin()
                                                }
                                        )

                                        HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        SettingsClickableItem(
                                                icon = Icons.Default.Block,
                                                title = "Delete Account",
                                                subtitle =
                                                        "Permanently delete your account and all data",
                                                onClick = { showDeleteAccountDialog = true },
                                                isDestructive = true
                                        )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                        }

                        // About Section
                        SettingsSection(title = "About") {
                                SettingsClickableItem(
                                        icon = Icons.Default.Info,
                                        title = "Call Sentry",
                                        subtitle = "Version 1.0.0",
                                        onClick = {}
                                )
                                SettingsClickableItem(
                                        icon = Icons.Default.Info,
                                        title = "Privacy Policy",
                                        subtitle = "View our privacy policy",
                                        onClick = onNavigateToPrivacyPolicy
                                )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                }

                // Snackbar Host
                SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
                )
        }

        // Clear Logs Dialog
        if (showClearLogsDialog) {
                AlertDialog(
                        onDismissRequest = { showClearLogsDialog = false },
                        title = { Text("Clear Blocked Logs") },
                        text = {
                                Text(
                                        "Are you sure you want to delete all blocked call and SMS history? This action cannot be undone."
                                )
                        },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                viewModel.clearBlockedLogs()
                                                showClearLogsDialog = false
                                        }
                                ) { Text("Clear", color = Error) }
                        },
                        dismissButton = {
                                TextButton(onClick = { showClearLogsDialog = false }) {
                                        Text("Cancel")
                                }
                        }
                )
        }

        // Clear Whitelist Dialog
        if (showClearWhitelistDialog) {
                AlertDialog(
                        onDismissRequest = { showClearWhitelistDialog = false },
                        title = { Text("Clear Whitelist") },
                        text = {
                                Text(
                                        "Are you sure you want to remove all whitelisted contacts? All calls and SMS will be blocked until you add contacts again."
                                )
                        },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                viewModel.clearWhitelist()
                                                showClearWhitelistDialog = false
                                        }
                                ) { Text("Clear", color = Error) }
                        },
                        dismissButton = {
                                TextButton(onClick = { showClearWhitelistDialog = false }) {
                                        Text("Cancel")
                                }
                        }
                )
        }

        // Theme Selection Dialog
        if (showThemeDialog) {
                AlertDialog(
                        onDismissRequest = { showThemeDialog = false },
                        title = { Text("Select Theme") },
                        text = {
                                Column {
                                        ThemeOption(
                                                text = "System Default",
                                                selected = uiState.themeMode == 0,
                                                onClick = {
                                                        viewModel.setThemeMode(0)
                                                        showThemeDialog = false
                                                }
                                        )
                                        ThemeOption(
                                                text = "Light",
                                                selected = uiState.themeMode == 1,
                                                onClick = {
                                                        viewModel.setThemeMode(1)
                                                        showThemeDialog = false
                                                }
                                        )
                                        ThemeOption(
                                                text = "Dark",
                                                selected = uiState.themeMode == 2,
                                                onClick = {
                                                        viewModel.setThemeMode(2)
                                                        showThemeDialog = false
                                                }
                                        )
                                }
                        },
                        confirmButton = {
                                TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
                        }
                )
        }

        // Delete Account Dialog — Two-step confirmation
        if (showDeleteAccountDialog) {
                var deleteConfirmText by remember { mutableStateOf("") }
                var showStep2 by remember { mutableStateOf(false) }

                AlertDialog(
                        onDismissRequest = {
                                showDeleteAccountDialog = false
                                deleteConfirmText = ""
                                showStep2 = false
                        },
                        title = {
                                Text(
                                        if (showStep2) "⚠️ Final Confirmation"
                                        else "Delete Account",
                                        fontWeight = FontWeight.Bold,
                                        color = Error
                                )
                        },
                        text = {
                                Column {
                                        if (!showStep2) {
                                                // Step 1 — Detailed consequences
                                                Text(
                                                        "This action is permanent and cannot be undone. Deleting your account will:",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                val consequences =
                                                        listOf(
                                                                "🗑️ Permanently delete your profile from our servers",
                                                                "📇 Remove all whitelisted contacts",
                                                                "📋 Erase all blocked call/SMS history",
                                                                "📱 Unlink your phone number",
                                                                "🔒 Sign you out immediately",
                                                                "⚙️ Reset all protection settings",
                                                                "❌ You will NOT be able to recover this account"
                                                        )

                                                consequences.forEach { item ->
                                                        Text(
                                                                text = item,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                vertical = 2.dp
                                                                        )
                                                        )
                                                }
                                        } else {
                                                // Step 2 — Type DELETE to confirm
                                                Text(
                                                        "Type DELETE below to permanently delete your account:",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Error
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                OutlinedTextField(
                                                        value = deleteConfirmText,
                                                        onValueChange = { deleteConfirmText = it },
                                                        label = { Text("Type DELETE") },
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        isError =
                                                                deleteConfirmText.isNotEmpty() &&
                                                                        deleteConfirmText
                                                                                .uppercase() !=
                                                                                "DELETE"
                                                )
                                        }
                                }
                        },
                        confirmButton = {
                                if (!showStep2) {
                                        TextButton(onClick = { showStep2 = true }) {
                                                Text("I Understand, Continue", color = Error)
                                        }
                                } else {
                                        TextButton(
                                                onClick = {
                                                        viewModel.deleteAccount()
                                                        showDeleteAccountDialog = false
                                                        deleteConfirmText = ""
                                                        showStep2 = false
                                                        onNavigateToLogin()
                                                },
                                                enabled = deleteConfirmText.uppercase() == "DELETE"
                                        ) { Text("Delete Forever", color = Error) }
                                }
                        },
                        dismissButton = {
                                TextButton(
                                        onClick = {
                                                showDeleteAccountDialog = false
                                                deleteConfirmText = ""
                                                showStep2 = false
                                        }
                                ) { Text("Cancel") }
                        }
                )
        }
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onClick: () -> Unit) {
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .clickable(onClick = onClick)
                                .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                androidx.compose.material3.RadioButton(selected = selected, onClick = onClick)
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                )
        }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
        Column {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                )
                ) { content() }
        }
}

@Composable
fun SettingsSwitchItem(
        icon: ImageVector,
        title: String,
        subtitle: String,
        isChecked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        enabled: Boolean = true
) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint =
                                if (enabled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color =
                                        if (enabled) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }

                Switch(checked = isChecked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
}

@Composable
fun SettingsClickableItem(
        icon: ImageVector,
        title: String,
        subtitle: String,
        onClick: () -> Unit,
        isDestructive: Boolean = false
) {
        Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isDestructive) Error else MaterialTheme.colorScheme.primary
                )

                Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color =
                                        if (isDestructive) Error
                                        else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }
        }
}

private fun checkAllPermissionsGranted(context: Context): Boolean {
        val requiredPermissions =
                listOf(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.ANSWER_PHONE_CALLS,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS
                )

        return requiredPermissions.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) ==
                        PackageManager.PERMISSION_GRANTED
        }
}
