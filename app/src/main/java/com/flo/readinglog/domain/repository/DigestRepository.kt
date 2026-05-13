package com.flo.readinglog.domain.repository

import com.flo.readinglog.domain.model.Digest
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DigestRepository {
    fun observeAll(): Flow<List<Digest>>
    suspend fun getById(id: Long): Digest?
    suspend fun getByWeekStart(weekStart: LocalDate): Digest?
    suspend fun upsert(digest: Digest): Long
    suspend fun getUpdatedSince(since: Long): List<Digest>
}
