package com.flo.readinglog.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleBooksResponse(
    val items: List<GoogleBooksItem>? = null,
    val totalItems: Int = 0,
)

@Serializable
data class GoogleBooksItem(
    val id: String,
    val volumeInfo: VolumeInfo = VolumeInfo(),
)

@Serializable
data class VolumeInfo(
    val title: String = "",
    val authors: List<String>? = null,
    val description: String? = null,
    val publishedDate: String? = null,
    val pageCount: Int? = null,
    val categories: List<String>? = null,
    val imageLinks: ImageLinks? = null,
    val industryIdentifiers: List<IndustryIdentifier>? = null,
)

@Serializable
data class ImageLinks(
    val thumbnail: String? = null,
    val smallThumbnail: String? = null,
)

@Serializable
data class IndustryIdentifier(
    val type: String = "",
    val identifier: String = "",
)
