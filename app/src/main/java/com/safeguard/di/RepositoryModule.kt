package com.safeguard.di

import android.content.Context
import com.safeguard.data.preferences.SettingsDataStore
import com.safeguard.data.repository.BlockedLogRepositoryImpl
import com.safeguard.data.repository.WhitelistRepositoryImpl
import com.safeguard.domain.repository.BlockedLogRepository
import com.safeguard.domain.repository.WhitelistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWhitelistRepository(impl: WhitelistRepositoryImpl): WhitelistRepository

    @Binds
    @Singleton
    abstract fun bindBlockedLogRepository(impl: BlockedLogRepositoryImpl): BlockedLogRepository

    @Binds
    @Singleton
    abstract fun bindActivityLogRepository(
            impl: com.safeguard.data.repository.ActivityLogRepositoryImpl
    ): com.safeguard.domain.repository.ActivityLogRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
