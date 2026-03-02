package com.safeguard.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Privacy Policy") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                    text = "Call Sentry Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
            )

            Text(
                    text = "Last updated: March 2, 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PolicySection(
                    title = "1. Information We Collect",
                    content =
                            """
                        Call Sentry collects the following information to provide its call and SMS blocking services:

                        • Phone Number: Used for authentication and account identification.
                        • Email Address: Optionally provided for account recovery and profile management.
                        • Display Name: Optionally provided for personalization.
                        • Whitelisted Contacts: Phone numbers you choose to allow through the blocking filter. These are stored locally on your device.
                        • Blocked Call/SMS Logs: Records of blocked calls and messages, stored locally on your device.
                        • App Settings: Your protection preferences, stored locally on your device.
                    """.trimIndent()
            )

            PolicySection(
                    title = "2. How We Use Your Information",
                    content =
                            """
                        • Authentication: Your phone number or Google account is used to sign you in securely via Firebase Authentication.
                        • Call & SMS Filtering: Your whitelisted contacts are used locally to determine which calls and messages to allow or block.
                        • Profile Management: Your name and email are stored in Firebase Firestore for account management purposes only.
                        • We do NOT sell, share, or transfer your personal data to third parties.
                    """.trimIndent()
            )

            PolicySection(
                    title = "3. Data Storage & Security",
                    content =
                            """
                        • Local Data: Whitelisted contacts, blocked logs, and app settings are stored exclusively on your device using encrypted local storage (Room Database and DataStore).
                        • Cloud Data: Only your basic profile (name, email, phone) is stored in Firebase Firestore, protected by Firebase Security Rules that restrict access to your own data.
                        • Network Security: All network communication uses HTTPS. Cleartext (HTTP) traffic is disabled.
                        • No third-party analytics or tracking SDKs are used.
                    """.trimIndent()
            )

            PolicySection(
                    title = "4. Permissions We Request",
                    content =
                            """
                        Call Sentry requests the following Android permissions:

                        • READ_PHONE_STATE, READ_CALL_LOG, ANSWER_PHONE_CALLS: Required to screen and block incoming calls.
                        • RECEIVE_SMS, READ_SMS: Required to filter and block incoming SMS messages.
                        • READ_CONTACTS: Required to import contacts to your whitelist.
                        • POST_NOTIFICATIONS: Used to notify you about blocked calls/messages.
                        • RECEIVE_BOOT_COMPLETED: Ensures protection stays active after device restart.

                        All permissions are used solely for the app's core functionality. You can revoke permissions at any time through Android Settings.
                    """.trimIndent()
            )

            PolicySection(
                    title = "5. Account Deletion",
                    content =
                            """
                        You can delete your account at any time from Settings → Account → Delete Account. This will:

                        • Permanently remove your profile from our servers
                        • Delete all local data (whitelisted contacts, blocked logs, settings)
                        • Sign you out of the app

                        Account deletion is irreversible.
                    """.trimIndent()
            )

            PolicySection(
                    title = "6. Children's Privacy",
                    content =
                            """
                        Call Sentry is not directed at children under 13. We do not knowingly collect personal information from children. If you believe a child has provided us with personal data, please contact us so we can delete it.
                    """.trimIndent()
            )

            PolicySection(
                    title = "7. Changes to This Policy",
                    content =
                            """
                        We may update this privacy policy from time to time. Any changes will be reflected in the "Last updated" date above. Continued use of the app after changes constitutes acceptance of the updated policy.
                    """.trimIndent()
            )

            PolicySection(
                    title = "8. Contact Us",
                    content =
                            """
                        If you have questions about this privacy policy or your data, please contact us at:

                        Email: support@callsentry.app
                    """.trimIndent()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
        )
        Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}
