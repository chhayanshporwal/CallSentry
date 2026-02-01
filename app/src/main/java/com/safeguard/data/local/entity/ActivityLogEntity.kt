package com.safeguard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val type: String, // Store enum name
        val phoneNumber: String?,
        val contactName: String?,
        val details: String?,
        val timestamp: Long
)
