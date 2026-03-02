package com.safeguard.presentation.screens.recent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.domain.model.ActivityLog
import com.safeguard.domain.model.ActivityType
import com.safeguard.presentation.common.simpleVerticalScrollbar
import com.safeguard.presentation.theme.BlockedCallColor
import com.safeguard.presentation.theme.BlockedSmsColor
import com.safeguard.presentation.theme.StatusEnabled
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentActivityScreen(
        viewModel: RecentActivityViewModel = hiltViewModel(),
        onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top Bar
        CenterAlignedTopAppBar(
                title = {
                    Text(
                            "Activity Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.logs.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear History")
                        }
                    }
                },
                colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                        )
        )

        if (uiState.logs.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                            text = "No recent activity",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val scrollState = rememberLazyListState()
            LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize().simpleVerticalScrollbar(scrollState),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { items(uiState.logs, key = { it.id }) { log -> ActivityLogCard(log) } }
        }
    }
}

@Composable
fun ActivityLogCard(log: ActivityLog) {
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
            // Icon Background
            Box(
                    modifier =
                            Modifier.size(40.dp)
                                    .clip(CircleShape)
                                    .background(getIconColor(log.type).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = getIcon(log.type),
                        contentDescription = null,
                        tint = getIconColor(log.type),
                        modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = getTitle(log),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                )

                log.details?.let {
                    Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                        text = formatTimestamp(log.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun getIcon(type: ActivityType): ImageVector {
    return when (type) {
        ActivityType.CALL_BLOCKED -> Icons.Default.Call
        ActivityType.SMS_BLOCKED -> Icons.AutoMirrored.Filled.Message
        ActivityType.CALL_ALLOWED_EMERGENCY -> Icons.Default.Warning
        ActivityType.CALL_ALLOWED_CONTACT -> Icons.Default.Check
        ActivityType.SYSTEM_EVENT -> Icons.Default.Info
    }
}

@Composable
fun getIconColor(type: ActivityType): Color {
    return when (type) {
        ActivityType.CALL_BLOCKED -> BlockedCallColor
        ActivityType.SMS_BLOCKED -> BlockedSmsColor
        ActivityType.CALL_ALLOWED_EMERGENCY -> Color(0xFFFF9800) // Orange
        ActivityType.CALL_ALLOWED_CONTACT -> StatusEnabled
        ActivityType.SYSTEM_EVENT -> Color(0xFF9E9E9E) // Grey
    }
}

fun getTitle(log: ActivityLog): String {
    return if (!log.contactName.isNullOrBlank()) {
        log.contactName
    } else if (!log.phoneNumber.isNullOrBlank()) {
        log.phoneNumber
    } else {
        when (log.type) {
            ActivityType.SYSTEM_EVENT -> "System Event"
            else -> "Unknown"
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
