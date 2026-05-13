package com.flo.readinglog.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val messages: List<AnthropicMessage>,
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: String,
)

@Serializable
data class AnthropicResponse(
    val content: List<AnthropicContentBlock> = emptyList(),
)

@Serializable
data class AnthropicContentBlock(
    val type: String = "",
    val text: String = "",
)

@Serializable
data class RecommendedBook(
    val title: String,
    val author: String,
    @SerialName("why_recommended") val whyRecommended: String,
)
