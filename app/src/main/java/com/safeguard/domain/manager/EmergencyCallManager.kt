package com.safeguard.domain.manager

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyCallManager @Inject constructor() {

    private val callAttempts = ConcurrentHashMap<String, MutableList<Long>>()

    // Configuration constants
    companion object {
        const val EMERGENCY_WINDOW_MS = 5 * 60 * 1000L // 5 minutes
        const val REQUIRED_ATTEMPTS = 3 // Allow on the 4th call (i.e. if 3 previous attempts exist)
    }

    /**
     * Records a rejected call attempt for a phone number. Cleans up old attempts before adding the
     * new one.
     */
    fun recordAttempt(phoneNumber: String) {
        val now = System.currentTimeMillis()
        val attempts = callAttempts.getOrPut(phoneNumber) { mutableListOf() }

        synchronized(attempts) {
            // Remove attempts older than the window
            attempts.removeAll { it < now - EMERGENCY_WINDOW_MS }
            // Add current attempt
            attempts.add(now)
        }
    }

    /**
     * Checks if a phone number should be allowed through based on frequency. Returns true if the
     * number has called significantly often recently.
     */
    fun shouldAllow(phoneNumber: String): Boolean {
        val now = System.currentTimeMillis()
        val attempts = callAttempts[phoneNumber] ?: return false

        synchronized(attempts) {
            // Remove attempts older than the window so we count accurately
            attempts.removeAll { it < now - EMERGENCY_WINDOW_MS }

            // Check if we have met the required threshold
            return attempts.size >= REQUIRED_ATTEMPTS
        }
    }

    /** Clears attempts for a specific number (e.g. after a successful call) */
    fun clearAttempts(phoneNumber: String) {
        callAttempts.remove(phoneNumber)
    }
}
