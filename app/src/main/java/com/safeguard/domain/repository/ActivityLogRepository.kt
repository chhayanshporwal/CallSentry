package com.safeguard.domain.repository

import com.safeguard.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {
    fun getAllLogs(): Flow<List<ActivityLog>>
    suspend fun addLog(log: ActivityLog)
    suspend fun clearLogs()
}
