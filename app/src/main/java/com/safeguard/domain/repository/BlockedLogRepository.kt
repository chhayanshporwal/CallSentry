package com.safeguard.domain.repository

import com.safeguard.domain.model.BlockType
import com.safeguard.domain.model.BlockedLog
import kotlinx.coroutines.flow.Flow

interface BlockedLogRepository {

    fun getAllLogs(): Flow<List<BlockedLog>>

    fun getLogsByType(type: BlockType): Flow<List<BlockedLog>>

    fun getTotalCount(): Flow<Int>

    fun getBlockedCallsCount(): Flow<Int>

    fun getBlockedSmsCount(): Flow<Int>

    fun getBlockedCallsCountToday(): Flow<Int>

    fun getBlockedSmsCountToday(): Flow<Int>

    suspend fun getLogById(id: Long): BlockedLog?

    suspend fun addLog(log: BlockedLog): Long

    suspend fun removeLog(log: BlockedLog)

    suspend fun removeOlderThan(days: Int): Int

    suspend fun markAsRead(id: Long)

    suspend fun markAllAsRead()

    suspend fun clearAll()
}
