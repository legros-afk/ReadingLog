package com.flo.readinglog.data.repository

import com.flo.readinglog.BuildConfig
import com.flo.readinglog.data.remote.AnthropicService
import com.flo.readinglog.data.remote.model.AnthropicMessage
import com.flo.readinglog.data.remote.model.AnthropicRequest
import com.flo.readinglog.data.remote.model.RecommendedBook
import com.flo.readinglog.domain.repository.RecommendationsRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class RecommendationsRepositoryImpl @Inject constructor(
    private val service: AnthropicService,
    private val json: Json,
) : RecommendationsRepository {

    override suspend fun getRecommendations(favouriteBooks: List<String>): Result<List<RecommendedBook>> = runCatching {
        val bookList = favouriteBooks.joinToString("\n") { "- $it" }
        val prompt = """
            I'm a young reader and these are some of my favourite books:
            $bookList

            Based on these favourites, please recommend 5 books I might enjoy.
            Respond ONLY with a valid JSON array (no markdown, no explanation) in this exact format:
            [{"title":"...","author":"...","why_recommended":"...","year":"2019","genres":["Fantasy","Adventure"],"synopsis":"A short 2-3 sentence synopsis of the book."}]
        """.trimIndent()

        val request = AnthropicRequest(
            model = "claude-haiku-4-5-20251001",
            maxTokens = 1024,
            messages = listOf(AnthropicMessage(role = "user", content = prompt)),
        )

        val response = service.createMessage(
            apiKey = BuildConfig.ANTHROPIC_API_KEY,
            request = request,
        )

        val text = response.content.firstOrNull { it.type == "text" }?.text
            ?: error("No text in response")

        // Strip any accidental markdown code fences
        val cleaned = text.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        json.decodeFromString<List<RecommendedBook>>(cleaned)
    }
}
