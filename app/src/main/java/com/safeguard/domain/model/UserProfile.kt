package com.safeguard.domain.model

data class UserProfile(
        val uid: String = "",
        val phoneNumber: String = "",
        val email: String? = null,
        val displayName: String? = null,
        val photoUrl: String? = null,
        val createdAt: Long = System.currentTimeMillis()
)
