package com.flo.readinglog.data.repository

import com.flo.readinglog.data.local.dao.DigestDao
import com.flo.readinglog.data.local.mapper.toDomain
import com.flo.readinglog.data.local.mapper.toEntity
import com.flo.readinglog.domain.model.Digest
import com.flo.readinglog.domain.repository.DigestRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DigestRepositoryImpl @Inject constructor(
    private val dao: DigestDao,
) : DigestRepository {
    override fun observeAll() = dao.observeAll().map { it.map { e -> e.toDomain() } }
    override suspend fun getById(id: Long) = dao.getById(id)?.toDomain()
    override suspend fun upsert(digest: Digest): Long = dao.upsert(digest.toEntity())
    override suspend fun getUpdatedSince(since: Long) = dao.getUpdatedSince(since).map { it.toDomain() }
}
