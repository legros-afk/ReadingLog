package com.flo.readinglog.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.flo.readinglog.data.local.ReadingLogDatabase
import com.flo.readinglog.data.local.dao.BookDao
import com.flo.readinglog.data.local.dao.DigestDao
import com.flo.readinglog.data.local.dao.ReadingEntryDao
import com.flo.readinglog.data.repository.BookRepositoryImpl
import com.flo.readinglog.data.repository.CharacterRepositoryImpl
import com.flo.readinglog.data.repository.DigestRepositoryImpl
import com.flo.readinglog.data.repository.ReadingEntryRepositoryImpl
import com.flo.readinglog.data.repository.SettingsRepositoryImpl
import com.flo.readinglog.domain.repository.BookRepository
import com.flo.readinglog.domain.repository.CharacterRepository
import com.flo.readinglog.domain.repository.DigestRepository
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import com.flo.readinglog.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ReadingLogDatabase =
        Room.databaseBuilder(context, ReadingLogDatabase::class.java, "reading_log.db").build()

    @Provides fun provideBookDao(db: ReadingLogDatabase): BookDao = db.bookDao()
    @Provides fun provideReadingEntryDao(db: ReadingLogDatabase): ReadingEntryDao = db.readingEntryDao()
    @Provides fun provideDigestDao(db: ReadingLogDatabase): DigestDao = db.digestDao()

    @Named("settings")
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") }
        )

    @Named("character")
    @Provides
    @Singleton
    fun provideCharacterDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("character") }
        )

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl

    @Provides
    @Singleton
    fun provideBookRepository(impl: BookRepositoryImpl): BookRepository = impl

    @Provides
    @Singleton
    fun provideReadingEntryRepository(impl: ReadingEntryRepositoryImpl): ReadingEntryRepository = impl

    @Provides
    @Singleton
    fun provideDigestRepository(impl: DigestRepositoryImpl): DigestRepository = impl

    @Provides
    @Singleton
    fun provideCharacterRepository(impl: CharacterRepositoryImpl): CharacterRepository = impl
}
