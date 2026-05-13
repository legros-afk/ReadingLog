package com.flo.readinglog.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.domain.model.Book
import com.flo.readinglog.domain.model.ReadingEntry
import com.flo.readinglog.domain.repository.BookRepository
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailUiState(
    val book: Book? = null,
    val entries: List<ReadingEntry> = emptyList(),
    val totalPagesRead: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val entryRepository: ReadingEntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun load(bookId: Long) {
        viewModelScope.launch {
            combine(
                bookRepository.observeAll().map { it.find { b -> b.id == bookId } },
                entryRepository.observeByBook(bookId)
            ) { book, entries ->
                BookDetailUiState(
                    book = book,
                    entries = entries,
                    totalPagesRead = entries.sumOf { it.pagesRead },
                    isLoading = false,
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun toggleFavourite() {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            bookRepository.setFavourite(book.id, !book.isFavourite)
        }
    }

    fun toggleWantToRead() {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            bookRepository.setWantToRead(book.id, !book.isWantToRead)
        }
    }

    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }
}
