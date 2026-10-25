package com.enak.app.di

import android.content.Context
import com.enak.data.local.AppUsageDao
import com.enak.data.local.EnakDatabase
import com.enak.data.local.SessionDao
import com.enak.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EnakDatabase {
        return EnakDatabase.getDatabase(context)
    }

    @Provides
    fun provideSessionDao(database: EnakDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    fun provideAppUsageDao(database: EnakDatabase): AppUsageDao {
        return database.appUsageDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }
}
