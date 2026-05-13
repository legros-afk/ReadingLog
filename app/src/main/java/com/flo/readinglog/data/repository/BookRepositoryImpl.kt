package com.flo.readinglog.data.repository

import com.flo.readinglog.data.local.dao.BookDao
import com.flo.readinglog.data.local.entity.BookEntity
import com.flo.readinglog.data.local.mapper.toDomain
import com.flo.readinglog.data.local.mapper.toEntity
import com.flo.readinglog.domain.model.Book
import com.flo.readinglog.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val dao: BookDao,
) : BookRepository {

    override fun observeAll() = dao.observeAll().map { it.map { e -> e.toDomain() } }
    override fun observeFavourites() = dao.observeFavourites().map { it.map { e -> e.toDomain() } }
    override fun observeWantToRead() = dao.observeWantToRead().map { it.map { e -> e.toDomain() } }

    override suspend fun getById(id: Long) = dao.getById(id)?.toDomain()
    override suspend fun getByGoogleBooksId(googleBooksId: String) = dao.getByGoogleBooksId(googleBooksId)?.toDomain()

    override suspend fun upsert(book: Book): Long = dao.upsert(book.toEntity())

    override suspend fun setFavourite(id: Long, favourite: Boolean) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(isFavourite = favourite, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun setWantToRead(id: Long, wantToRead: Boolean) {
        val entity = dao.getById(id) ?: return
        dao.update(entity.copy(isWantToRead = wantToRead, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(id: Long) {
        val entity = dao.getById(id) ?: return
        dao.delete(entity)
    }

    override suspend fun getUpdatedSince(since: Long) =
        dao.getUpdatedSince(since).map { it.toDomain() }
}
