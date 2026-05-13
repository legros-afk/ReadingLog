package com.flo.readinglog.domain.repository

import com.flo.readinglog.domain.model.Book

interface GoogleBooksRepository {
    suspend fun search(query: String): Result<List<Book>>
}
