package com.safeguard.presentation.screens.blocked

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.domain.model.BlockType
import com.safeguard.domain.model.BlockedLog
import com.safeguard.presentation.theme.BlockedCallColor
import com.safeguard.presentation.theme.BlockedSmsColor
import com.safeguard.presentation.theme.Secondary
import com.safeguard.util.PhoneNumberUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BlockedLogScreen(viewModel: BlockedLogViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                    text = "Blocked",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                    text = "Calls and messages that were blocked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                        selected = uiState.selectedFilter == FilterType.ALL,
                        onClick = { viewModel.setFilter(FilterType.ALL) },
                        label = { Text("All") },
                        colors =
                                FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                )

                FilterChip(
                        selected = uiState.selectedFilter == FilterType.CALLS,
                        onClick = { viewModel.setFilter(FilterType.CALLS) },
                        label = { Text("Calls") },
                        leadingIcon = {
                            if (uiState.selectedFilter == FilterType.CALLS) {
                                Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors =
                                FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BlockedCallColor,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                )

                FilterChip(
                        selected = uiState.selectedFilter == FilterType.SMS,
                        onClick = { viewModel.setFilter(FilterType.SMS) },
                        label = { Text("SMS") },
                        leadingIcon = {
                            if (uiState.selectedFilter == FilterType.SMS) {
                                Icon(
                                        imageVector = Icons.Default.Message,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        colors =
                                FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BlockedSmsColor,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                )
            }
        }

        // Log List
        if (uiState.logs.isEmpty() && !uiState.isLoading) {
            EmptyBlockedState()
        } else {
            LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.logs, key = { it.id }) { log ->
                    BlockedLogCard(log = log, onAddToWhitelist = { viewModel.addToWhitelist(log) })
                }
            }
        }
    }
}

@Composable
fun BlockedLogCard(log: BlockedLog, onAddToWhitelist: () -> Unit) {
    val iconColor = if (log.type == BlockType.CALL) BlockedCallColor else BlockedSmsColor
    val icon = if (log.type == BlockType.CALL) Icons.Default.Call else Icons.Default.Message

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                    modifier =
                            Modifier.size(48.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = icon,
                        contentDescription = log.type.name,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = PhoneNumberUtils.formatForDisplay(log.phoneNumber),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                )

                if (log.preview != null) {
                    Text(
                            text = log.preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                        text = formatTimestamp(log.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onAddToWhitelist) {
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to Whitelist",
                        tint = Secondary
                )
            }
        }
    }
}

@Composable
fun EmptyBlockedState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    text = "No blocked items",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                    text = "Blocked calls and SMS will appear here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} min ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}
