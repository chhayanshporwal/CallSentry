package com.safeguard.data.repository

import com.safeguard.data.local.dao.BlockedLogDao
import com.safeguard.data.local.entity.BlockedLogEntity
import com.safeguard.domain.model.BlockType
import com.safeguard.domain.model.BlockedLog
import com.safeguard.domain.repository.BlockedLogRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BlockedLogRepositoryImpl @Inject constructor(private val blockedLogDao: BlockedLogDao) :
        BlockedLogRepository {

    override fun getAllLogs(): Flow<List<BlockedLog>> {
        return blockedLogDao.getAllLogsFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getLogsByType(type: BlockType): Flow<List<BlockedLog>> {
        return blockedLogDao.getLogsByTypeFlow(type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTotalCount(): Flow<Int> {
        return blockedLogDao.getTotalCountFlow()
    }

    override fun getBlockedCallsCount(): Flow<Int> {
        return blockedLogDao.getBlockedCallsCountFlow()
    }

    override fun getBlockedSmsCount(): Flow<Int> {
        return blockedLogDao.getBlockedSmsCountFlow()
    }

    override fun getBlockedCallsCountToday(): Flow<Int> {
        val startOfDay = getStartOfDay()
        return blockedLogDao.getBlockedCallsCountSince(startOfDay)
    }

    override fun getBlockedSmsCountToday(): Flow<Int> {
        val startOfDay = getStartOfDay()
        return blockedLogDao.getBlockedSmsCountSince(startOfDay)
    }

    override suspend fun getLogById(id: Long): BlockedLog? {
        return blockedLogDao.getById(id)?.toDomainModel()
    }

    override suspend fun addLog(log: BlockedLog): Long {
        return blockedLogDao.insert(log.toEntity())
    }

    override suspend fun removeLog(log: BlockedLog) {
        blockedLogDao.delete(log.toEntity())
    }

    override suspend fun removeOlderThan(days: Int): Int {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return blockedLogDao.deleteOlderThan(cutoff)
    }

    override suspend fun markAsRead(id: Long) {
        blockedLogDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        blockedLogDao.markAllAsRead()
    }

    override suspend fun clearAll() {
        blockedLogDao.deleteAll()
    }

    private fun getStartOfDay(): Long {
        val calendar =
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
        return calendar.timeInMillis
    }

    // Mappers
    private fun BlockedLogEntity.toDomainModel(): BlockedLog {
        return BlockedLog(
                id = id,
                phoneNumber = phoneNumber,
                type = BlockType.valueOf(type),
                timestamp = timestamp,
                preview = preview,
                isRead = isRead
        )
    }

    private fun BlockedLog.toEntity(): BlockedLogEntity {
        return BlockedLogEntity(
                id = id,
                phoneNumber = phoneNumber,
                type = type.name,
                timestamp = timestamp,
                preview = preview,
                isRead = isRead
        )
    }
}
