package com.safeguard.presentation.screens.blocked

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.presentation.common.components.BlockedLogCard
import com.safeguard.presentation.common.simpleVerticalScrollbar
import com.safeguard.presentation.theme.BlockedCallColor
import com.safeguard.presentation.theme.BlockedSmsColor

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
                                        imageVector = Icons.AutoMirrored.Filled.Message,
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
            val scrollState = rememberLazyListState()
            LazyColumn(
                    state = scrollState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.simpleVerticalScrollbar(scrollState)
            ) {
                items(uiState.logs, key = { it.id }) { log ->
                    BlockedLogCard(log = log, onAddToWhitelist = { viewModel.addToWhitelist(log) })
                }
            }
        }
    }
}
