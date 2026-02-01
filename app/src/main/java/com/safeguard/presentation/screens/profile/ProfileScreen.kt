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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.safeguard.presentation.theme.Primary
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfileData(
    val name: String?,
    val email: String?,
    val phone: String?,
    val role: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var userProfileData by remember { mutableStateOf<UserProfileData?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    suspend fun loadUserProfile() {
        try {
            error = null
            isLoading = true
            if (user != null) {
                // Fetch additional profile data from Firestore if available
                val docRef = firestore.collection("users").document(user.uid)
                val snapshot = docRef.get().await()
                
                userProfileData = UserProfileData(
                    name = snapshot.getString("name") ?: user.displayName,
                    email = snapshot.getString("email") ?: user.email,
                    phone = snapshot.getString("phoneNumber") ?: user.phoneNumber,
                    role = snapshot.getString("role")
                )
            }
        } catch (e: Exception) {
            error = "Failed to load profile: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadUserProfile()
    }
    
    // Update edited fields when profile data loads
    LaunchedEffect(userProfileData) {
        userProfileData?.let {
            editedName = it.name ?: ""
            editedEmail = it.email ?: ""
        }
    }
    
    suspend fun saveProfile() {
        if (user == null) return
        isSaving = true
        try {
            // Check email uniqueness
            if (editedEmail != userProfileData?.email) {
                val emailSnapshot = firestore.collection("users")
                    .whereEqualTo("email", editedEmail)
                    .get()
                    .await()
                
                val emailExists = emailSnapshot.documents.any { it.id != user.uid }
                if (emailExists) {
                    error = "This email is already registered to another account"
                    isSaving = false
                    return
                }
            }
            
            val updates = hashMapOf<String, Any>(
                "name" to editedName,
                "email" to editedEmail
            )
            firestore.collection("users").document(user.uid).update(updates).await()
            loadUserProfile()
            isEditMode = false
            error = null  // Clear any previous errors
        } catch (e: Exception) {
            error = "Failed to save: ${e.message}"
        } finally {
            isSaving = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (user != null && !isLoading && error == null) {
                        IconButton(
                            onClick = {
                                if (isEditMode) {
                                    scope.launch { saveProfile() }
                                } else {
                                    isEditMode = true
                                }
                            },
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                    contentDescription = if (isEditMode) "Save" else "Edit"
                                )
                            }
                        }
                        if (isEditMode) {
                            IconButton(onClick = { 
                                isEditMode = false
                                // Reset edited values
                                editedName = userProfileData?.name ?: ""
                                editedEmail = userProfileData?.email ?: ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
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
                error != null -> {
                    // Error State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
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
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            scope.launch {
                                isLoading = true
                                loadUserProfile()
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }
                user != null && userProfileData != null -> {
                    // Success State
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getInitials(userProfileData?.name ?: "User"),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Name
                        Text(
                            text = if (isEditMode) "Editing Profile" else (userProfileData?.name ?: "Not set"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Profile Info - Editable
                        if (isEditMode) {
                            // Name Field
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("Name") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Email Field
                            OutlinedTextField(
                                value = editedEmail,
                                onValueChange = { editedEmail = it },
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
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
                                            userProfileData?.phone ?: "Not set",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    Icon(Icons.Default.Edit, null, tint = Primary)
                                }
                            }
                        } else {
                            // View Mode - Static Cards
                            ProfileInfoCard(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = userProfileData?.email ?: "Not set"
                            )

                            ProfileInfoCard(
                                icon = Icons.Default.Phone,
                                label = "Phone",
                                value = userProfileData?.phone ?: "Not set"
                            )
                        }

                        userProfileData?.role?.let { role ->
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
                                auth.signOut()
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
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
                        Text(
                            text = "Not signed in",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please sign in to view your profile",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(42.dp))  // Golden ratio: 26 * φ
                        Button(
                            onClick = { navController.navigate("login") },
                            modifier = Modifier.fillMaxWidth(0.618f)  // Golden ratio width
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
                    currentPhone = userProfileData?.phone,
                    onDismiss = { showPhoneDialog = false },
                    onVerified = { newPhone ->
                        showPhoneDialog = false
                        scope.launch { loadUserProfile() }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
