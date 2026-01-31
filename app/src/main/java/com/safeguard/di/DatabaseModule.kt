package com.safeguard.di

import android.content.Context
import androidx.room.Room
import com.safeguard.data.local.AppDatabase
import com.safeguard.data.local.dao.BlockedLogDao
import com.safeguard.data.local.dao.WhitelistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
                .build()
    }

    @Provides
    @Singleton
    fun provideWhitelistDao(database: AppDatabase): WhitelistDao {
        return database.whitelistDao()
    }

    @Provides
    @Singleton
    fun provideBlockedLogDao(database: AppDatabase): BlockedLogDao {
        return database.blockedLogDao()
    }
}
