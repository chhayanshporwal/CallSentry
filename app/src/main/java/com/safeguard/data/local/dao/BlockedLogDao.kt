package com.safeguard.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safeguard.data.local.entity.BlockedLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedLogDao {

    @Query("SELECT * FROM blocked_log ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<BlockedLogEntity>>

    @Query("SELECT * FROM blocked_log ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getLogs(limit: Int, offset: Int): List<BlockedLogEntity>

    @Query("SELECT * FROM blocked_log WHERE type = :type ORDER BY timestamp DESC")
    fun getLogsByTypeFlow(type: String): Flow<List<BlockedLogEntity>>

    @Query("SELECT * FROM blocked_log WHERE id = :id")
    suspend fun getById(id: Long): BlockedLogEntity?

    @Query("SELECT COUNT(*) FROM blocked_log") fun getTotalCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_log WHERE type = 'CALL'")
    fun getBlockedCallsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_log WHERE type = 'SMS'")
    fun getBlockedSmsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_log WHERE type = 'CALL' AND timestamp > :since")
    fun getBlockedCallsCountSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM blocked_log WHERE type = 'SMS' AND timestamp > :since")
    fun getBlockedSmsCountSince(since: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlockedLogEntity): Long

    @Delete suspend fun delete(entity: BlockedLogEntity)

    @Query("DELETE FROM blocked_log WHERE id = :id") suspend fun deleteById(id: Long)

    @Query("DELETE FROM blocked_log WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long): Int

    @Query("DELETE FROM blocked_log") suspend fun deleteAll()

    @Query("UPDATE blocked_log SET is_read = 1 WHERE id = :id") suspend fun markAsRead(id: Long)

    @Query("UPDATE blocked_log SET is_read = 1") suspend fun markAllAsRead()
}
