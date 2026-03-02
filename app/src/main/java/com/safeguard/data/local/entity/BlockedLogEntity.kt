package com.safeguard.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "blocked_log",
        indices = [Index(value = ["timestamp"]), Index(value = ["type"])]
)
data class BlockedLogEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @ColumnInfo(name = "phone_number") val phoneNumber: String,
        @ColumnInfo(name = "type") val type: String, // "CALL" or "SMS"
        @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
        @ColumnInfo(name = "preview") val preview: String? = null, // SMS preview text
        @ColumnInfo(name = "is_read") val isRead: Boolean = false
)
