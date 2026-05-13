package com.flo.readinglog.data.repository

import com.flo.readinglog.BuildConfig
import com.flo.readinglog.data.remote.GoogleBooksService
import com.flo.readinglog.domain.model.Book
import com.flo.readinglog.domain.repository.GoogleBooksRepository
import javax.inject.Inject

class GoogleBooksRepositoryImpl @Inject constructor(
    private val service: GoogleBooksService,
) : GoogleBooksRepository {

    override suspend fun search(query: String): Result<List<Book>> = runCatching {
        val response = service.searchBooks(query, apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY)
        response.items?.map { item ->
            val info = item.volumeInfo
            // Force HTTPS for cover URLs (Google Books returns HTTP)
            val coverUrl = (info.imageLinks?.thumbnail ?: info.imageLinks?.smallThumbnail)
                ?.replace("http://", "https://")
            Book(
                id = 0,
                googleBooksId = item.id,
                title = info.title,
                authors = info.authors ?: emptyList(),
                coverUrl = coverUrl,
                isbn = info.industryIdentifiers
                    ?.filter { it.type in listOf("ISBN_13", "ISBN_10") }
                    ?.map { it.identifier } ?: emptyList(),
                pageCount = info.pageCount,
                description = info.description,
                publishedDate = info.publishedDate,
                categories = info.categories ?: emptyList(),
                isFavourite = false,
                isWantToRead = false,
                updatedAt = System.currentTimeMillis(),
            )
        } ?: emptyList()
    }
}
