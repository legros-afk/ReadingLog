package com.flo.readinglog.data.local.mapper

import com.flo.readinglog.data.local.entity.ReadingEntryEntity
import com.flo.readinglog.domain.model.ReadingEntry
import java.time.LocalDate

fun ReadingEntryEntity.toDomain(book: com.flo.readinglog.domain.model.Book? = null) = ReadingEntry(
    id = id,
    bookId = bookId,
    book = book,
    pageFrom = pageFrom,
    pageTo = pageTo,
    impressions = impressions,
    date = LocalDate.ofEpochDay(dateEpochDay),
    updatedAt = updatedAt,
)

fun ReadingEntry.toEntity() = ReadingEntryEntity(
    id = id,
    bookId = bookId,
    pageFrom = pageFrom,
    pageTo = pageTo,
    impressions = impressions,
    dateEpochDay = date.toEpochDay(),
    updatedAt = updatedAt,
)
