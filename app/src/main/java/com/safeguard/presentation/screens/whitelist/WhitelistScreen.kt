package com.safeguard.presentation.screens.whitelist

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safeguard.domain.model.WhitelistContact
import com.safeguard.presentation.common.simpleVerticalScrollbar
import com.safeguard.presentation.theme.Primary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Data class for contacts loaded from the device
data class DeviceContact(val name: String, val phoneNumber: String, val contactId: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WhitelistScreen(viewModel: WhitelistViewModel = hiltViewModel()) {
        val uiState by viewModel.uiState.collectAsState()
        val scrollState = rememberLazyListState()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val haptic = LocalHapticFeedback.current

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
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                contentWindowInsets = WindowInsets(0.dp)
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
                                        state = scrollState,
                                        contentPadding =
                                                PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.simpleVerticalScrollbar(scrollState)
                                ) {
                                        items(uiState.contacts, key = { it.id }) { contact ->
                                                ContactCard(
                                                        contact = contact,
                                                        onToggleCalls = {
                                                                viewModel.toggleCallPermission(
                                                                        contact
                                                                )
                                                        },
                                                        onToggleSms = {
                                                                viewModel.toggleSmsPermission(
                                                                        contact
                                                                )
                                                        },
                                                        onDelete = {
                                                                viewModel.showDeleteConfirmation(
                                                                        contact
                                                                )
                                                        },
                                                        onLongPress = {
                                                                haptic.performHapticFeedback(
                                                                        HapticFeedbackType.LongPress
                                                                )
                                                                viewModel.cycleFilterMode(contact)
                                                                scope.launch {
                                                                        val newMode =
                                                                                when {
                                                                                        contact.allowCalls &&
                                                                                                contact.allowSms ->
                                                                                                "Calls Only"
                                                                                        contact.allowCalls &&
                                                                                                !contact.allowSms ->
                                                                                                "Messages Only"
                                                                                        else ->
                                                                                                "Active"
                                                                                }
                                                                        snackbarHostState
                                                                                .currentSnackbarData
                                                                                ?.dismiss()
                                                                        snackbarHostState
                                                                                .showSnackbar(
                                                                                        "${contact.displayName ?: contact.phoneNumber}: $newMode",
                                                                                        duration =
                                                                                                SnackbarDuration
                                                                                                        .Short
                                                                                )
                                                                }
                                                        }
                                                )
                                        }
                                }
                        }
                }

                // Add Dialog
                if (uiState.showAddDialog) {
                        AddContactDialog(
                                onDismiss = { viewModel.hideAddDialog() },
                                onConfirm = { phone, name, allowCalls, allowSms ->
                                        viewModel.addContact(phone, name, allowCalls, allowSms)
                                        scope.launch {
                                                snackbarHostState.showSnackbar("Contact added")
                                        }
                                },
                                onImportContact = {
                                        viewModel.hideAddDialog()
                                        // Check permission first
                                        val hasPermission =
                                                androidx.core.content.ContextCompat
                                                        .checkSelfPermission(
                                                                context,
                                                                android.Manifest.permission
                                                                        .READ_CONTACTS
                                                        ) ==
                                                        android.content.pm.PackageManager
                                                                .PERMISSION_GRANTED

                                        if (hasPermission) {
                                                viewModel.showContactPicker()
                                        } else {
                                                // For simplicity, show a message. In a real app,
                                                // you'd
                                                // launch a permission request first.
                                                scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                                "Please grant contacts permission in app settings"
                                                        )
                                                }
                                        }
                                }
                        )
                }

                // Multi-select Contact Picker
                if (uiState.showContactPicker) {
                        ContactPickerDialog(
                                contentResolver = context.contentResolver,
                                onDismiss = { viewModel.hideContactPicker() },
                                onImport = { selectedContacts ->
                                        val whitelistContacts =
                                                selectedContacts.map { dc ->
                                                        WhitelistContact(
                                                                phoneNumber = dc.phoneNumber,
                                                                displayName = dc.name
                                                        )
                                                }
                                        viewModel.addContactsBatch(whitelistContacts)
                                        scope.launch {
                                                snackbarHostState.showSnackbar(
                                                        "Added ${selectedContacts.size} contact(s)",
                                                        duration = SnackbarDuration.Short
                                                )
                                        }
                                }
                        )
                }

                // Delete Confirmation
                uiState.showDeleteConfirmation?.let { contact ->
                        DeleteConfirmationDialog(
                                contact = contact,
                                onDismiss = { viewModel.hideDeleteConfirmation() },
                                onConfirm = {
                                        viewModel.deleteContact(contact)
                                        scope.launch {
                                                snackbarHostState.showSnackbar("Contact removed")
                                        }
                                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactCard(
        contact: WhitelistContact,
        onToggleCalls: () -> Unit,
        onToggleSms: () -> Unit,
        onDelete: () -> Unit,
        onLongPress: () -> Unit
) {
        // Determine status text and color
        val statusText: String
        val statusColor: Color

        when {
                contact.allowCalls && contact.allowSms -> {
                        statusText = "Active"
                        statusColor = Color(0xFF4CAF50) // Green
                }
                contact.allowCalls && !contact.allowSms -> {
                        statusText = "Calls Only"
                        statusColor = Color(0xFF2196F3) // Blue
                }
                !contact.allowCalls && contact.allowSms -> {
                        statusText = "Messages Only"
                        statusColor = Color(0xFFFF9800) // Orange
                }
                else -> {
                        statusText = "Inactive"
                        statusColor = MaterialTheme.colorScheme.error
                }
        }

        Card(
                modifier =
                        Modifier.fillMaxWidth()
                                .combinedClickable(onClick = {}, onLongClick = onLongPress),
                shape = RoundedCornerShape(16.dp),
                colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                                (contact.displayName?.firstOrNull()
                                                                ?: contact.phoneNumber.first())
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
                                        text = contact.phoneNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Status label
                                Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor,
                                        modifier = Modifier.padding(top = 2.dp)
                                )
                        }

                        // Call toggle
                        IconButton(onClick = onToggleCalls) {
                                Icon(
                                        imageVector =
                                                if (contact.allowCalls) Icons.Default.Call
                                                else Icons.Default.PhoneDisabled,
                                        contentDescription =
                                                if (contact.allowCalls) "Calls allowed"
                                                else "Calls blocked",
                                        tint =
                                                if (contact.allowCalls) Primary
                                                else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.4f)
                                )
                        }

                        // SMS toggle
                        IconButton(onClick = onToggleSms) {
                                Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription =
                                                if (contact.allowSms) "SMS allowed"
                                                else "SMS blocked",
                                        tint =
                                                if (contact.allowSms) Primary
                                                else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.4f)
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
                                color =
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.7f
                                        )
                        )
                }
        }
}

@Composable
fun AddContactDialog(
        onDismiss: () -> Unit,
        onConfirm: (String, String?, Boolean, Boolean) -> Unit,
        onImportContact: () -> Unit
) {
        var phoneNumber by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var allowCalls by remember { mutableStateOf(true) }
        var allowSms by remember { mutableStateOf(true) }
        var isError by remember { mutableStateOf(false) }

        fun validateAndConfirm() {
                if (phoneNumber.isBlank() || !phoneNumber.any { it.isDigit() }) {
                        isError = true
                } else {
                        isError = false
                        onConfirm(phoneNumber, name, allowCalls, allowSms)
                }
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Add Contact") },
                text = {
                        Column {
                                OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = {
                                                phoneNumber = it
                                                isError = false
                                        },
                                        label = { Text("Phone Number") },
                                        singleLine = true,
                                        isError = isError,
                                        supportingText = {
                                                if (isError) Text("Invalid phone number")
                                        },
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                                Spacer(modifier = Modifier.height(16.dp))

                                // Permission toggles
                                Text(
                                        text = "Allow this contact to:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Checkbox(
                                                checked = allowCalls,
                                                onCheckedChange = { allowCalls = it }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint =
                                                        if (allowCalls) Primary
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                                text = "Calls",
                                                style = MaterialTheme.typography.bodyMedium
                                        )
                                }

                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Checkbox(
                                                checked = allowSms,
                                                onCheckedChange = { allowSms = it }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                                imageVector = Icons.Default.Sms,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint =
                                                        if (allowSms) Primary
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                                text = "Messages",
                                                style = MaterialTheme.typography.bodyMedium
                                        )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                        onClick = onImportContact,
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Icon(
                                                Icons.Default.Contacts,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Import from Contacts")
                                }
                        }
                },
                confirmButton = {
                        TextButton(
                                onClick = { validateAndConfirm() },
                                enabled = phoneNumber.isNotBlank()
                        ) { Text("Add") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
        )
}

// ============================================================
// Multi-Select Contact Picker Dialog
// ============================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactPickerDialog(
        contentResolver: ContentResolver,
        onDismiss: () -> Unit,
        onImport: (List<DeviceContact>) -> Unit
) {
        var isLoading by remember { mutableStateOf(true) }
        var searchQuery by remember { mutableStateOf("") }
        val allContacts = remember { mutableStateListOf<DeviceContact>() }
        val selectedContacts = remember { mutableStateListOf<DeviceContact>() }

        // Load contacts from device
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
                scope.launch {
                        val loaded =
                                withContext(Dispatchers.IO) { loadDeviceContacts(contentResolver) }
                        allContacts.addAll(loaded)
                        isLoading = false
                }
        }

        val filteredContacts =
                if (searchQuery.isBlank()) {
                        allContacts
                } else {
                        allContacts.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                        it.phoneNumber.contains(searchQuery)
                        }
                }

        val allSelected =
                filteredContacts.isNotEmpty() &&
                        filteredContacts.all { fc ->
                                selectedContacts.any { it.phoneNumber == fc.phoneNumber }
                        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Column {
                                Text("Select Contacts")
                                Text(
                                        text = "${selectedContacts.size} selected",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                },
                text = {
                        Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                                // Search
                                OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Search...") },
                                        singleLine = true,
                                        leadingIcon = {
                                                Icon(
                                                        Icons.Default.Search,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        },
                                        trailingIcon = {
                                                if (searchQuery.isNotEmpty()) {
                                                        IconButton(onClick = { searchQuery = "" }) {
                                                                Icon(
                                                                        Icons.Default.Close,
                                                                        contentDescription =
                                                                                "Clear",
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                )
                                                        }
                                                }
                                        }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Select All row
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant.copy(
                                                                        alpha = 0.5f
                                                                )
                                                        )
                                                        .combinedClickable(
                                                                onClick = {
                                                                        if (allSelected) {
                                                                                selectedContacts
                                                                                        .clear()
                                                                        } else {
                                                                                selectedContacts
                                                                                        .clear()
                                                                                selectedContacts
                                                                                        .addAll(
                                                                                                filteredContacts
                                                                                        )
                                                                        }
                                                                },
                                                                onLongClick = {}
                                                        )
                                                        .padding(
                                                                horizontal = 12.dp,
                                                                vertical = 10.dp
                                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                imageVector =
                                                        if (allSelected) Icons.Default.CheckBox
                                                        else Icons.Default.CheckBoxOutlineBlank,
                                                contentDescription = "Select All",
                                                tint =
                                                        if (allSelected) Primary
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                                text = "Select All (${filteredContacts.size})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                        )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (isLoading) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) { CircularProgressIndicator(color = Primary) }
                                } else if (filteredContacts.isEmpty()) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text(
                                                        text = "No contacts found",
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant
                                                )
                                        }
                                } else {
                                        LazyColumn(
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                                items(
                                                        filteredContacts,
                                                        key = {
                                                                "${it.contactId}_${it.phoneNumber}"
                                                        }
                                                ) { dc ->
                                                        val isSelected =
                                                                selectedContacts.any {
                                                                        it.phoneNumber ==
                                                                                dc.phoneNumber
                                                                }
                                                        DeviceContactRow(
                                                                contact = dc,
                                                                isSelected = isSelected,
                                                                onToggle = {
                                                                        if (isSelected) {
                                                                                selectedContacts
                                                                                        .removeAll {
                                                                                                it.phoneNumber ==
                                                                                                        dc.phoneNumber
                                                                                        }
                                                                        } else {
                                                                                selectedContacts
                                                                                        .add(dc)
                                                                        }
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }
                },
                confirmButton = {
                        TextButton(
                                onClick = { onImport(selectedContacts.toList()) },
                                enabled = selectedContacts.isNotEmpty()
                        ) { Text("Import (${selectedContacts.size})") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
        )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceContactRow(contact: DeviceContact, isSelected: Boolean, onToggle: () -> Unit) {
        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                        if (isSelected) Primary.copy(alpha = 0.08f)
                                        else Color.Transparent
                                )
                                .combinedClickable(onClick = onToggle, onLongClick = {})
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                // Avatar
                Box(
                        modifier =
                                Modifier.size(36.dp)
                                        .clip(CircleShape)
                                        .background(Primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                        )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = contact.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                                text = contact.phoneNumber,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                }

                Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        }
}

/**
 * Loads all contacts with phone numbers from the device. Must be called from a background thread.
 */
fun loadDeviceContacts(contentResolver: ContentResolver): List<DeviceContact> {
        val contactsMap = mutableMapOf<String, DeviceContact>()

        val projection =
                arrayOf(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                )

        contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        projection,
                        null,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                )
                ?.use { cursor ->
                        val idIndex =
                                cursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                )
                        val nameIndex =
                                cursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                                )
                        val numberIndex =
                                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                        while (cursor.moveToNext()) {
                                val id = if (idIndex >= 0) cursor.getString(idIndex) else continue
                                val name =
                                        if (nameIndex >= 0) cursor.getString(nameIndex) ?: "Unknown"
                                        else "Unknown"
                                val number =
                                        if (numberIndex >= 0) cursor.getString(numberIndex) ?: ""
                                        else ""

                                if (number.isNotBlank()) {
                                        // Use cleaned number as key to deduplicate
                                        val cleanNumber = number.replace(Regex("[\\s\\-()]"), "")
                                        if (!contactsMap.containsKey(cleanNumber)) {
                                                contactsMap[cleanNumber] =
                                                        DeviceContact(
                                                                name = name,
                                                                phoneNumber = number.trim(),
                                                                contactId = id
                                                        )
                                        }
                                }
                        }
                }

        return contactsMap.values.toList()
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
