package com.flo.readinglog.domain.repository

import com.flo.readinglog.domain.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun observeAll(): Flow<List<Book>>
    fun observeFavourites(): Flow<List<Book>>
    fun observeWantToRead(): Flow<List<Book>>
    suspend fun getById(id: Long): Book?
    suspend fun getByGoogleBooksId(googleBooksId: String): Book?
    suspend fun upsert(book: Book): Long
    suspend fun setFavourite(id: Long, favourite: Boolean)
    suspend fun setWantToRead(id: Long, wantToRead: Boolean)
    suspend fun delete(id: Long)
    suspend fun getUpdatedSince(since: Long): List<Book>
}
