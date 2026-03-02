package com.safeguard.domain.model

enum class ActivityType {
    CALL_BLOCKED,
    SMS_BLOCKED,
    CALL_ALLOWED_EMERGENCY,
    CALL_ALLOWED_CONTACT,
    SYSTEM_EVENT // e.g. "Permissions Updated"
}

data class ActivityLog(
        val id: Long = 0,
        val type: ActivityType,
        val phoneNumber: String?, // Null for system events
        val contactName: String?,
        val details: String?, // e.g. "Blocked due to Spam Report" or "Allowed by user"
        val timestamp: Long = System.currentTimeMillis()
)
