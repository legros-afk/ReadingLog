package com.flo.readinglog.data.local.mapper

import com.flo.readinglog.data.local.entity.BookEntity
import com.flo.readinglog.domain.model.Book

fun BookEntity.toDomain() = Book(
    id = id,
    googleBooksId = googleBooksId,
    title = title,
    authors = authors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    coverUrl = coverUrl,
    isbn = isbn?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
    pageCount = pageCount,
    description = description,
    publishedDate = publishedDate,
    categories = categories?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
    isFavourite = isFavourite,
    isWantToRead = isWantToRead,
    updatedAt = updatedAt,
)

fun Book.toEntity() = BookEntity(
    id = id,
    googleBooksId = googleBooksId,
    title = title,
    authors = authors.joinToString(","),
    coverUrl = coverUrl,
    isbn = isbn.joinToString(",").ifEmpty { null },
    pageCount = pageCount,
    description = description,
    publishedDate = publishedDate,
    categories = categories.joinToString(",").ifEmpty { null },
    isFavourite = isFavourite,
    isWantToRead = isWantToRead,
    updatedAt = updatedAt,
)
