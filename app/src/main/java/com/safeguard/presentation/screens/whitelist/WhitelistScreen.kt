package com.safeguard.presentation.screens.whitelist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.domain.model.WhitelistContact
import com.safeguard.presentation.theme.Primary
import com.safeguard.util.PhoneNumberUtils

@Composable
fun WhitelistScreen(viewModel: WhitelistViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                        onClick = { viewModel.showAddDialog() },
                        containerColor = Primary
                ) {
                    Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Contact",
                            tint = Color.White
                    )
                }
            }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(paddingValues)
        ) {
            // Header
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                        text = "Whitelist",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                        text = "Contacts that can reach you",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) }
                )
            }

            // Contact List
            if (uiState.contacts.isEmpty() && !uiState.isLoading) {
                EmptyState()
            } else {
                LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.contacts, key = { it.id }) { contact ->
                        ContactCard(
                                contact = contact,
                                onDelete = { viewModel.showDeleteConfirmation(contact) }
                        )
                    }
                }
            }
        }

        // Add Dialog
        if (uiState.showAddDialog) {
            AddContactDialog(
                    onDismiss = { viewModel.hideAddDialog() },
                    onConfirm = { phone, name -> viewModel.addContact(phone, name) }
            )
        }

        // Delete Confirmation
        uiState.showDeleteConfirmation?.let { contact ->
            DeleteConfirmationDialog(
                    contact = contact,
                    onDismiss = { viewModel.hideDeleteConfirmation() },
                    onConfirm = { viewModel.deleteContact(contact) }
            )
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search contacts...") },
            leadingIcon = {
                Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                ) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors =
                    TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                    )
    )
}

@Composable
fun ContactCard(contact: WhitelistContact, onDelete: () -> Unit) {
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
            // Avatar
            Box(
                    modifier =
                            Modifier.size(48.dp)
                                    .clip(CircleShape)
                                    .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text =
                                (contact.displayName?.firstOrNull() ?: contact.phoneNumber.first())
                                        .uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = contact.displayName ?: "Unknown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = PhoneNumberUtils.formatForDisplay(contact.phoneNumber),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    text = "No contacts yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                    text = "Tap + to add trusted contacts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AddContactDialog(onDismiss: () -> Unit, onConfirm: (String, String?) -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Contact") },
            text = {
                Column {
                    OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name (optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                        onClick = { onConfirm(phoneNumber, name) },
                        enabled = phoneNumber.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DeleteConfirmationDialog(
        contact: WhitelistContact,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Remove Contact") },
            text = {
                Text(
                        "Are you sure you want to remove ${contact.displayName ?: contact.phoneNumber} from your whitelist?"
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
