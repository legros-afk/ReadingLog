package com.flo.readinglog.domain.repository

import com.flo.readinglog.domain.model.Digest
import kotlinx.coroutines.flow.Flow

interface DigestRepository {
    fun observeAll(): Flow<List<Digest>>
    suspend fun getById(id: Long): Digest?
    suspend fun upsert(digest: Digest): Long
    suspend fun getUpdatedSince(since: Long): List<Digest>
}
