package com.flo.readinglog.data.remote

import com.flo.readinglog.data.remote.model.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksService {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10,
        @Query("key") apiKey: String? = null,
    ): GoogleBooksResponse
}
