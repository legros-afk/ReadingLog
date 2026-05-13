package com.flo.readinglog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flo.readinglog.data.local.dao.BookDao
import com.flo.readinglog.data.local.dao.DigestDao
import com.flo.readinglog.data.local.dao.ReadingEntryDao
import com.flo.readinglog.data.local.entity.BookEntity
import com.flo.readinglog.data.local.entity.DigestEntity
import com.flo.readinglog.data.local.entity.ReadingEntryEntity

@Database(
    entities = [BookEntity::class, ReadingEntryEntity::class, DigestEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ReadingLogDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun readingEntryDao(): ReadingEntryDao
    abstract fun digestDao(): DigestDao
}
