package com.flo.readinglog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val googleBooksId: String,
    val title: String,
    val authors: String,          // comma-separated
    val coverUrl: String?,
    val isbn: String?,            // comma-separated ISBNs
    val pageCount: Int?,
    val description: String?,
    val publishedDate: String?,
    val categories: String?,      // comma-separated
    val isFavourite: Boolean = false,
    val isWantToRead: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)
