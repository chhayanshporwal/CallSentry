package com.safeguard.service

import android.telecom.Call
import android.telecom.CallScreeningService
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
class SafeGuardCallScreeningService : CallScreeningService() {

    @Inject lateinit var whitelistRepository: WhitelistRepository
    @Inject lateinit var blockedLogRepository: BlockedLogRepository
    @Inject lateinit var settingsDataStore: SettingsDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""

        serviceScope.launch {
            // Check if blocking is enabled
            val isBlockingEnabled =
                    settingsDataStore.isBlockingEnabled.first() &&
                            settingsDataStore.isCallBlockingEnabled.first()

            if (!isBlockingEnabled) {
                // Blocking disabled, allow all calls
                respondToCall(callDetails, createAllowResponse())
                return@launch
            }

            // Check emergency numbers - always allow
            if (PhoneNumberUtils.isEmergencyNumber(phoneNumber)) {
                respondToCall(callDetails, createAllowResponse())
                return@launch
            }

            // Check whitelist
            val isWhitelisted = whitelistRepository.isCallAllowed(phoneNumber)

            val response =
                    if (isWhitelisted) {
                        // Allow whitelisted numbers
                        createAllowResponse()
                    } else {
                        // Block non-whitelisted numbers and log
                        logBlockedCall(phoneNumber)
                        createBlockResponse()
                    }

            respondToCall(callDetails, response)
        }
    }

    private fun createAllowResponse(): CallResponse {
        return CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
    }

    private fun createBlockResponse(): CallResponse {
        return CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false) // Keep in call log for transparency
                .setSkipNotification(true) // Don't notify about blocked call
                .build()
    }

    private fun logBlockedCall(phoneNumber: String) {
        serviceScope.launch {
            try {
                blockedLogRepository.addLog(
                        BlockedLog(
                                phoneNumber = phoneNumber,
                                type = BlockType.CALL,
                                timestamp = System.currentTimeMillis()
                        )
                )
            } catch (e: Exception) {
                // Silent fail - don't crash service
            }
        }
    }
}
