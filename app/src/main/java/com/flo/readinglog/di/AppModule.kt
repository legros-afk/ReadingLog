package com.flo.readinglog.di

import android.content.Context
import androidx.room.Room
import com.flo.readinglog.data.local.ReadingLogDatabase
import com.flo.readinglog.data.local.dao.BookDao
import com.flo.readinglog.data.local.dao.DigestDao
import com.flo.readinglog.data.local.dao.ReadingEntryDao
import com.flo.readinglog.data.repository.BookRepositoryImpl
import com.flo.readinglog.data.repository.DigestRepositoryImpl
import com.flo.readinglog.data.repository.ReadingEntryRepositoryImpl
import com.flo.readinglog.domain.repository.BookRepository
import com.flo.readinglog.domain.repository.DigestRepository
import com.flo.readinglog.domain.repository.ReadingEntryRepository
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
    fun provideDatabase(@ApplicationContext context: Context): ReadingLogDatabase =
        Room.databaseBuilder(context, ReadingLogDatabase::class.java, "reading_log.db").build()

    @Provides fun provideBookDao(db: ReadingLogDatabase): BookDao = db.bookDao()
    @Provides fun provideReadingEntryDao(db: ReadingLogDatabase): ReadingEntryDao = db.readingEntryDao()
    @Provides fun provideDigestDao(db: ReadingLogDatabase): DigestDao = db.digestDao()

    @Provides
    @Singleton
    fun provideBookRepository(impl: BookRepositoryImpl): BookRepository = impl

    @Provides
    @Singleton
    fun provideReadingEntryRepository(impl: ReadingEntryRepositoryImpl): ReadingEntryRepository = impl

    @Provides
    @Singleton
    fun provideDigestRepository(impl: DigestRepositoryImpl): DigestRepository = impl
}
