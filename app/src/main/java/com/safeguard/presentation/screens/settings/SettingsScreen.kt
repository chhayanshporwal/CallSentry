package com.safeguard.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.presentation.common.simpleVerticalScrollbar
import com.safeguard.presentation.theme.Error

@Composable
fun SettingsScreen(
        viewModel: SettingsViewModel = hiltViewModel(),
        onNavigateToOnboarding: () -> Unit
) {
        val uiState by viewModel.uiState.collectAsState()
        var showClearLogsDialog by remember { mutableStateOf(false) }
        var showClearWhitelistDialog by remember { mutableStateOf(false) }

        val scrollState = rememberScrollState()
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
                                                .Shield, // Re-using Shield or maybe another icon
                                // like NotificationsActive
                                title = "Emergency Breakthrough",
                                subtitle = "Allow call if number calls 3+ times in 5 mins",
                                isChecked = uiState.isEmergencyBreakthroughEnabled,
                                onCheckedChange = { viewModel.toggleEmergencyBreakthrough() },
                                enabled = uiState.isBlockingEnabled
                        )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security Section
                SettingsSection(title = "Security") {
                        SettingsSwitchItem(
                                icon = Icons.Default.Lock,
                                title = "PIN Protection",
                                subtitle = "Require PIN to access settings",
                                isChecked = uiState.isPinEnabled,
                                onCheckedChange = { viewModel.togglePinEnabled() }
                        )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Permissions Section
                SettingsSection(title = "Permissions") {
                        SettingsClickableItem(
                                icon = Icons.Default.Shield,
                                title = "Setup Permissions",
                                subtitle = "Grant necessary permissions for full protection",
                                onClick = { onNavigateToOnboarding() }
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

                // About Section
                SettingsSection(title = "About") {
                        SettingsClickableItem(
                                icon = Icons.Default.Info,
                                title = "SafeGuard",
                                subtitle = "Version 1.0.0",
                                onClick = {}
                        )
                }

                Spacer(modifier = Modifier.height(32.dp))
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
