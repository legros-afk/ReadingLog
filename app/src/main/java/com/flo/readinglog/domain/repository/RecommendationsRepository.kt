package com.flo.readinglog.domain.repository

import com.flo.readinglog.data.remote.model.RecommendedBook

interface RecommendationsRepository {
    suspend fun getRecommendations(favouriteBooks: List<String>): Result<List<RecommendedBook>>
}
