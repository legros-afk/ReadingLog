package com.flo.readinglog.domain.usecase

import com.flo.readinglog.domain.model.Book
import com.flo.readinglog.domain.model.ReadingEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class BuildDigestUseCaseTest {

    private lateinit var useCase: BuildDigestUseCase
    private val weekStart = LocalDate.of(2025, 1, 6)

    @Before
    fun setUp() {
        useCase = BuildDigestUseCase()
    }

    // --- Digest message generation ---

    @Test
    fun `empty entries produces no-reading message`() {
        val message = useCase(emptyList(), emptyList(), weekStart)
        assertTrue(message.contains("No reading this week"))
    }

    @Test
    fun `empty entries omits reading section header`() {
        val message = useCase(emptyList(), emptyList(), weekStart)
        assertFalse(message.contains("This week's reading"))
    }

    @Test
    fun `header contains week start date`() {
        val message = useCase(emptyList(), emptyList(), weekStart)
        assertTrue(message.contains("6 Jan 2025"))
    }

    @Test
    fun `entries produce total page count`() {
        val entries = listOf(
            entry(pageFrom = 1, pageTo = 50),   // 50 pages
            entry(pageFrom = 51, pageTo = 80),  // 30 pages
        )
        val message = useCase(entries, emptyList(), weekStart)
        assertTrue(message.contains("Total pages: 80"))
    }

    @Test
    fun `entries include book title and author`() {
        val book = book(title = "The Hobbit", authors = listOf("J.R.R. Tolkien"))
        val entries = listOf(entry(book = book))
        val message = useCase(entries, emptyList(), weekStart)
        assertTrue(message.contains("The Hobbit"))
        assertTrue(message.contains("J.R.R. Tolkien"))
    }

    @Test
    fun `entries include impressions`() {
        val book = book(title = "Matilda")
        val entries = listOf(entry(book = book, impressions = "Absolutely loved it!"))
        val message = useCase(entries, emptyList(), weekStart)
        assertTrue(message.contains("Absolutely loved it!"))
    }

    @Test
    fun `empty impressions are not printed`() {
        val book = book(title = "Matilda")
        val entries = listOf(entry(book = book, impressions = ""))
        val message = useCase(entries, emptyList(), weekStart)
        assertFalse(message.contains("\"\""))
    }

    @Test
    fun `multiple entries for same book are grouped`() {
        val book = book(id = 1L, title = "The Hobbit")
        val entries = listOf(
            entry(bookId = 1L, book = book, pageFrom = 1, pageTo = 40),
            entry(bookId = 1L, book = book, pageFrom = 41, pageTo = 80),
        )
        val message = useCase(entries, emptyList(), weekStart)
        // "The Hobbit" should appear once, showing 80 combined pages
        val titleCount = message.split("The Hobbit").size - 1
        assertTrue("Book title should appear once per book section", titleCount == 1)
        assertTrue(message.contains("Pages: 80"))
    }

    @Test
    fun `no wishlist section when wishlist is empty`() {
        val entries = listOf(entry())
        val message = useCase(entries, emptyList(), weekStart)
        assertFalse(message.contains("Wishlist"))
    }

    @Test
    fun `wishlist section present when wishlist books exist`() {
        val book = book(title = "Percy Jackson", authors = listOf("Rick Riordan"))
        val message = useCase(emptyList(), listOf(book), weekStart)
        assertTrue(message.contains("Wishlist additions"))
        assertTrue(message.contains("Percy Jackson"))
    }

    @Test
    fun `wishlist entry contains Amazon URL`() {
        val book = book(title = "Matilda", authors = listOf("Roald Dahl"))
        val message = useCase(emptyList(), listOf(book), weekStart)
        assertTrue(message.contains("amazon.co.uk"))
    }

    // --- Amazon URL generation ---

    @Test
    fun `amazonUrl encodes title and author`() {
        val url = useCase.amazonUrl("The Hobbit", listOf("J.R.R. Tolkien"))
        assertTrue(url.startsWith("https://www.amazon.co.uk/s?k="))
        assertTrue(url.contains("Hobbit"))
        assertTrue(url.contains("Tolkien"))
    }

    @Test
    fun `amazonUrl with no authors encodes only title`() {
        val url = useCase.amazonUrl("Matilda", emptyList())
        assertTrue(url.contains("Matilda"))
        assertFalse(url.endsWith("+"))
    }

    @Test
    fun `amazonUrl encodes spaces`() {
        val url = useCase.amazonUrl("Harry Potter", listOf("J.K. Rowling"))
        // URLEncoder replaces spaces with +
        assertTrue(url.contains("Harry+Potter") || url.contains("Harry%20Potter"))
    }

    @Test
    fun `amazonUrl with multiple authors includes all`() {
        val url = useCase.amazonUrl("Book", listOf("Author One", "Author Two"))
        assertTrue(url.contains("Author"))
        assertTrue(url.contains("One"))
        assertTrue(url.contains("Two"))
    }

    // --- Helpers ---

    private fun book(
        id: Long = 1L,
        title: String = "Test Book",
        authors: List<String> = listOf("Test Author"),
    ) = Book(
        id = id,
        googleBooksId = "gbid_$id",
        title = title,
        authors = authors,
        coverUrl = null,
        isbn = emptyList(),
        pageCount = null,
        description = null,
        publishedDate = null,
        categories = emptyList(),
        isFavourite = false,
        isWantToRead = false,
        updatedAt = 0L,
    )

    private fun entry(
        id: Long = 1L,
        bookId: Long = 1L,
        book: Book? = book(),
        pageFrom: Int = 1,
        pageTo: Int = 10,
        impressions: String = "",
        date: LocalDate = weekStart,
    ) = ReadingEntry(
        id = id,
        bookId = bookId,
        book = book,
        pageFrom = pageFrom,
        pageTo = pageTo,
        impressions = impressions,
        date = date,
        updatedAt = 0L,
    )
}
