package com.safeguard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.safeguard.data.local.dao.BlockedLogDao
import com.safeguard.data.local.dao.WhitelistDao
import com.safeguard.data.local.entity.BlockedLogEntity
import com.safeguard.data.local.entity.WhitelistEntity

@Database(
        entities = [WhitelistEntity::class, BlockedLogEntity::class],
        version = 1,
        exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun whitelistDao(): WhitelistDao
    abstract fun blockedLogDao(): BlockedLogDao

    companion object {
        const val DATABASE_NAME = "safeguard_db"
    }
}
