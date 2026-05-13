package com.flo.readinglog.domain.repository

import com.flo.readinglog.domain.model.ReadingEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ReadingEntryRepository {
    fun observeAll(): Flow<List<ReadingEntry>>
    fun observeByDate(date: LocalDate): Flow<List<ReadingEntry>>
    fun observeByBook(bookId: Long): Flow<List<ReadingEntry>>
    suspend fun getById(id: Long): ReadingEntry?
    suspend fun getInRange(from: LocalDate, to: LocalDate): List<ReadingEntry>
    suspend fun upsert(entry: ReadingEntry): Long
    suspend fun update(entry: ReadingEntry)
    suspend fun delete(id: Long)
    suspend fun getUpdatedSince(since: Long): List<ReadingEntry>
}
