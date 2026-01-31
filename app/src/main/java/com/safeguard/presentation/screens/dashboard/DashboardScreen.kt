package com.safeguard.presentation.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.presentation.theme.BlockedCallColor
import com.safeguard.presentation.theme.BlockedSmsColor
import com.safeguard.presentation.theme.CardGradientEnd
import com.safeguard.presentation.theme.CardGradientStart
import com.safeguard.presentation.theme.Secondary
import com.safeguard.presentation.theme.StatusDisabled
import com.safeguard.presentation.theme.StatusEnabled

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
    ) {
        // Header
        Text(
                text = "SafeGuard",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
        )

        Text(
                text = "Your protection shield",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Protection Card
        ProtectionCard(
                isEnabled = uiState.isBlockingEnabled,
                onToggle = { viewModel.toggleBlocking() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Row
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Calls Blocked",
                    value = uiState.blockedCallsToday.toString(),
                    subtitle = "Today",
                    icon = Icons.Default.Call,
                    iconColor = BlockedCallColor
            )

            StatCard(
                    modifier = Modifier.weight(1f),
                    title = "SMS Blocked",
                    value = uiState.blockedSmsToday.toString(),
                    subtitle = "Today",
                    icon = Icons.Default.Message,
                    iconColor = BlockedSmsColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Whitelist Count Card
        StatCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Whitelisted Contacts",
                value = uiState.whitelistCount.toString(),
                subtitle = "Trusted numbers",
                icon = Icons.Default.People,
                iconColor = Secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Settings
        Text(
                text = "Quick Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        QuickSettingItem(
                title = "Block Calls",
                subtitle = "Block incoming calls from unknown numbers",
                isEnabled = uiState.isCallBlockingEnabled,
                onToggle = { viewModel.toggleCallBlocking() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        QuickSettingItem(
                title = "Block SMS",
                subtitle = "Block incoming SMS from unknown numbers",
                isEnabled = uiState.isSmsBlockingEnabled,
                onToggle = { viewModel.toggleSmsBlocking() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProtectionCard(isEnabled: Boolean, onToggle: () -> Unit) {
    val backgroundColor by
            animateColorAsState(
                    targetValue = if (isEnabled) StatusEnabled else StatusDisabled,
                    label = "background"
            )

    val scale by animateFloatAsState(targetValue = if (isEnabled) 1f else 0.95f, label = "scale")

    Card(
            modifier = Modifier.fillMaxWidth().scale(scale),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(
                                        brush =
                                                Brush.linearGradient(
                                                        colors =
                                                                if (isEnabled) {
                                                                    listOf(
                                                                            CardGradientStart,
                                                                            CardGradientEnd
                                                                    )
                                                                } else {
                                                                    listOf(
                                                                            StatusDisabled,
                                                                            StatusDisabled.copy(
                                                                                    alpha = 0.7f
                                                                            )
                                                                    )
                                                                }
                                                )
                                )
                                .padding(24.dp)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Shield Icon
                Box(
                        modifier =
                                Modifier.size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Protection",
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = if (isEnabled) "Protection Active" else "Protection Disabled",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                    )
                    Text(
                            text =
                                    if (isEnabled) {
                                        "Only whitelisted contacts can reach you"
                                    } else {
                                        "All calls and SMS will come through"
                                    },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggle() },
                        colors =
                                SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color.White.copy(alpha = 0.3f),
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                )
            }
        }
    }
}

@Composable
fun StatCard(
        modifier: Modifier = Modifier,
        title: String,
        value: String,
        subtitle: String,
        icon: ImageVector,
        iconColor: Color
) {
    Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                        modifier =
                                Modifier.size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(iconColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(24.dp),
                            tint = iconColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickSettingItem(title: String, subtitle: String, isEnabled: Boolean, onToggle: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(checked = isEnabled, onCheckedChange = { onToggle() })
        }
    }
}
