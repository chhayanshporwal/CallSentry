package com.safeguard.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.safeguard.data.preferences.SettingsDataStore
import com.safeguard.domain.model.BlockType
import com.safeguard.domain.model.BlockedLog
import com.safeguard.domain.repository.BlockedLogRepository
import com.safeguard.domain.repository.WhitelistRepository
import com.safeguard.util.PhoneNumberUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject lateinit var whitelistRepository: WhitelistRepository
    @Inject lateinit var blockedLogRepository: BlockedLogRepository
    @Inject lateinit var settingsDataStore: SettingsDataStore

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                // Check if blocking is enabled
                val isBlockingEnabled =
                        settingsDataStore.isBlockingEnabled.first() &&
                                settingsDataStore.isSmsBlockingEnabled.first()

                if (!isBlockingEnabled) {
                    return@launch // Blocking disabled, let SMS through
                }

                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

                for (sms in messages) {
                    val sender = sms.originatingAddress ?: continue

                    // Check emergency numbers - always allow
                    if (PhoneNumberUtils.isEmergencyNumber(sender)) {
                        continue
                    }

                    // Check whitelist
                    val isWhitelisted = whitelistRepository.isSmsAllowed(sender)

                    if (!isWhitelisted) {
                        // Abort broadcast to prevent SMS from being received
                        abortBroadcast()

                        // Log the blocked SMS
                        logBlockedSms(sender, sms.messageBody)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun logBlockedSms(phoneNumber: String, preview: String?) {
        receiverScope.launch {
            try {
                blockedLogRepository.addLog(
                        BlockedLog(
                                phoneNumber = phoneNumber,
                                type = BlockType.SMS,
                                timestamp = System.currentTimeMillis(),
                                preview = preview?.take(100) // Limit preview length
                        )
                )
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
}
