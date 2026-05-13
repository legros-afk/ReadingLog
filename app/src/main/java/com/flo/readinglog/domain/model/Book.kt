package com.flo.readinglog.domain.model

data class Book(
    val id: Long,
    val googleBooksId: String,
    val title: String,
    val authors: List<String>,
    val coverUrl: String?,
    val isbn: List<String>,
    val pageCount: Int?,
    val description: String?,
    val publishedDate: String?,
    val categories: List<String>,
    val isFavourite: Boolean,
    val isWantToRead: Boolean,
    val updatedAt: Long,
)
