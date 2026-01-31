package com.safeguard.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "whitelist",
        indices = [Index(value = ["phone_number"], unique = true), Index(value = ["created_at"])]
)
data class WhitelistEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @ColumnInfo(name = "phone_number") val phoneNumber: String,
        @ColumnInfo(name = "display_name") val displayName: String? = null,
        @ColumnInfo(name = "contact_uri") val contactUri: String? = null,
        @ColumnInfo(name = "allow_calls") val allowCalls: Boolean = true,
        @ColumnInfo(name = "allow_sms") val allowSms: Boolean = true,
        @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
        @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
