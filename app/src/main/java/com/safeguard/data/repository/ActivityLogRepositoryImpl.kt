package com.safeguard.data.repository

import com.safeguard.data.local.dao.ActivityLogDao
import com.safeguard.data.local.entity.ActivityLogEntity
import com.safeguard.domain.model.ActivityLog
import com.safeguard.domain.model.ActivityType
import com.safeguard.domain.repository.ActivityLogRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ActivityLogRepositoryImpl @Inject constructor(private val dao: ActivityLogDao) :
        ActivityLogRepository {

    override fun getAllLogs(): Flow<List<ActivityLog>> {
        return dao.getAllLogsFlow().map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun addLog(log: ActivityLog) {
        dao.insert(log.toEntity())
    }

    override suspend fun clearLogs() {
        dao.deleteAll()
    }

    private fun ActivityLogEntity.toDomainModel(): ActivityLog {
        return ActivityLog(
                id = id,
                type = ActivityType.valueOf(type),
                phoneNumber = phoneNumber,
                contactName = contactName,
                details = details,
                timestamp = timestamp
        )
    }

    private fun ActivityLog.toEntity(): ActivityLogEntity {
        return ActivityLogEntity(
                id = id,
                type = type.name,
                phoneNumber = phoneNumber,
                contactName = contactName,
                details = details,
                timestamp = timestamp
        )
    }
}
