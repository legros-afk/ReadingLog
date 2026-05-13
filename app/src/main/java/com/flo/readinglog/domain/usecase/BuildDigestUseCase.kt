package com.flo.readinglog.domain.usecase

import com.flo.readinglog.domain.model.Book
import com.flo.readinglog.domain.model.ReadingEntry
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BuildDigestUseCase @Inject constructor() {

    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    operator fun invoke(
        entries: List<ReadingEntry>,
        wishlistBooks: List<Book>,
        weekStart: LocalDate,
    ): String = buildString {
        appendLine("Reading Digest - Week of ${weekStart.format(dateFormatter)}")

        if (entries.isEmpty()) {
            appendLine()
            appendLine("No reading this week.")
        } else {
            val totalPages = entries.sumOf { it.pagesRead }
            appendLine()
            appendLine("This week's reading")
            appendLine("Total pages: $totalPages")

            entries.groupBy { it.bookId }.forEach { (_, bookEntries) ->
                val book = bookEntries.first().book
                val title = book?.title ?: "Unknown book"
                val authors = book?.authors?.joinToString(", ").orEmpty()
                val pages = bookEntries.sumOf { it.pagesRead }
                val impressions = bookEntries
                    .mapNotNull { it.impressions.takeIf(String::isNotBlank) }

                appendLine()
                append(title)
                if (authors.isNotBlank()) append(" by $authors")
                appendLine()
                appendLine("  Pages: $pages")
                if (impressions.isNotEmpty()) {
                    appendLine("  \"${impressions.joinToString(" / ")}\"")
                }
            }
        }

        if (wishlistBooks.isNotEmpty()) {
            appendLine()
            appendLine("Wishlist additions")
            wishlistBooks.forEach { book ->
                val authors = book.authors.joinToString(", ")
                append("- ${book.title}")
                if (authors.isNotBlank()) append(" by $authors")
                appendLine(": ${amazonUrl(book.title, book.authors)}")
            }
        }
    }.trimEnd()

    fun amazonUrl(title: String, authors: List<String>): String {
        val query = buildString {
            append(title)
            if (authors.isNotEmpty()) {
                append(" ")
                append(authors.joinToString(" "))
            }
        }.trim()
        return "https://www.amazon.co.uk/s?k=${URLEncoder.encode(query, "UTF-8")}"
    }
}
