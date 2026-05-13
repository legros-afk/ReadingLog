package com.flo.readinglog.ui.screens.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.domain.model.Book
import com.flo.readinglog.domain.repository.BookRepository
import com.flo.readinglog.domain.repository.GoogleBooksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BookFilter { ALL, FAVOURITES, WANT_TO_READ }

data class BooksUiState(
    val books: List<Book> = emptyList(),
    val filter: BookFilter = BookFilter.ALL,
    val isAddingBook: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Book> = emptyList(),
    val isSearching: Boolean = false,
    val errorMessage: String? = null,
    val showAddBookDialog: Boolean = false,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class BooksViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val googleBooksRepository: GoogleBooksRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    private val _filter = MutableStateFlow(BookFilter.ALL)

    init {
        _filter
            .flatMapLatest { filter ->
                when (filter) {
                    BookFilter.ALL -> bookRepository.observeAll()
                    BookFilter.FAVOURITES -> bookRepository.observeFavourites()
                    BookFilter.WANT_TO_READ -> bookRepository.observeWantToRead()
                }
            }
            .onEach { books -> _uiState.update { it.copy(books = books) } }
            .launchIn(viewModelScope)

        searchQuery
            .debounce(400)
            .filter { it.length >= 2 }
            .onEach { query ->
                _uiState.update { it.copy(isSearching = true) }
                googleBooksRepository.search(query)
                    .onSuccess { results -> _uiState.update { it.copy(searchResults = results, isSearching = false) } }
                    .onFailure { e -> _uiState.update { it.copy(isSearching = false, errorMessage = "Search failed: ${e.message}") } }
            }
            .launchIn(viewModelScope)
    }

    fun setFilter(filter: BookFilter) { _uiState.update { it.copy(filter = filter) }; _filter.value = filter }
    fun onSearchQueryChange(query: String) { _uiState.update { it.copy(searchQuery = query) }; searchQuery.value = query }
    fun onShowAddBookDialog(show: Boolean) = _uiState.update { it.copy(showAddBookDialog = show, searchQuery = "", searchResults = emptyList()) }
    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }

    fun addBookToCatalogue(book: Book) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingBook = true) }
            try {
                val existing = bookRepository.getByGoogleBooksId(book.googleBooksId)
                if (existing == null) bookRepository.upsert(book)
                _uiState.update { it.copy(isAddingBook = false, showAddBookDialog = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAddingBook = false, errorMessage = "Failed to add book: ${e.message}") }
            }
        }
    }
}
