package com.safeguard.domain.model

data class BlockedLog(
        val id: Long = 0,
        val phoneNumber: String,
        val type: BlockType,
        val timestamp: Long = System.currentTimeMillis(),
        val preview: String? = null,
        val isRead: Boolean = false
)

enum class BlockType {
    CALL,
    SMS
}
