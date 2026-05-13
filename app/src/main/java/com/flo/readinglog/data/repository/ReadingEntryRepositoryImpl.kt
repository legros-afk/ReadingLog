package com.flo.readinglog.data.repository

import com.flo.readinglog.data.local.dao.BookDao
import com.flo.readinglog.data.local.dao.ReadingEntryDao
import com.flo.readinglog.data.local.mapper.toDomain
import com.flo.readinglog.data.local.mapper.toEntity
import com.flo.readinglog.domain.model.ReadingEntry
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class ReadingEntryRepositoryImpl @Inject constructor(
    private val entryDao: ReadingEntryDao,
    private val bookDao: BookDao,
) : ReadingEntryRepository {

    override fun observeAll(): Flow<List<ReadingEntry>> =
        entryDao.observeAll().map { entries ->
            entries.map { e ->
                val book = bookDao.getById(e.bookId)?.toDomain()
                e.toDomain(book)
            }
        }

    override fun observeByDate(date: LocalDate): Flow<List<ReadingEntry>> =
        entryDao.observeByDate(date.toEpochDay()).map { entries ->
            entries.map { e ->
                val book = bookDao.getById(e.bookId)?.toDomain()
                e.toDomain(book)
            }
        }

    override fun observeByBook(bookId: Long): Flow<List<ReadingEntry>> =
        entryDao.observeByBook(bookId).map { entries ->
            entries.map { e -> e.toDomain() }
        }

    override suspend fun getById(id: Long): ReadingEntry? {
        val entity = entryDao.getById(id) ?: return null
        val book = bookDao.getById(entity.bookId)?.toDomain()
        return entity.toDomain(book)
    }

    override suspend fun getInRange(from: LocalDate, to: LocalDate): List<ReadingEntry> =
        entryDao.getInRange(from.toEpochDay(), to.toEpochDay()).map { e ->
            val book = bookDao.getById(e.bookId)?.toDomain()
            e.toDomain(book)
        }

    override suspend fun upsert(entry: ReadingEntry): Long =
        entryDao.upsert(entry.toEntity())

    override suspend fun update(entry: ReadingEntry) =
        entryDao.update(entry.toEntity())

    override suspend fun delete(id: Long) {
        val entity = entryDao.getById(id) ?: return
        entryDao.delete(entity)
    }

    override suspend fun getUpdatedSince(since: Long): List<ReadingEntry> =
        entryDao.getUpdatedSince(since).map { e ->
            val book = bookDao.getById(e.bookId)?.toDomain()
            e.toDomain(book)
        }
}
