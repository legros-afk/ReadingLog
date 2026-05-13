package com.flo.readinglog.domain.model

import java.time.LocalDate

data class ReadingEntry(
    val id: Long,
    val bookId: Long,
    val book: Book?,
    val pageFrom: Int,
    val pageTo: Int,
    val impressions: String,
    val date: LocalDate,
    val updatedAt: Long,
) {
    val pagesRead: Int get() = pageTo - pageFrom + 1
}
