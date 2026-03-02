package com.safeguard.domain.model

data class WhitelistContact(
        val id: Long = 0,
        val phoneNumber: String,
        val displayName: String? = null,
        val contactUri: String? = null,
        val allowCalls: Boolean = true,
        val allowSms: Boolean = true,
        val createdAt: Long = System.currentTimeMillis()
)
