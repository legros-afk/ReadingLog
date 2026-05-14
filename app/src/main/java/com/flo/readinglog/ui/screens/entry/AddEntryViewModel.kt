package com.flo.readinglog.ui.screens.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.domain.model.Book
import com.flo.readinglog.domain.model.ReadingEntry
import com.flo.readinglog.domain.repository.BookRepository
import com.flo.readinglog.domain.repository.CharacterRepository
import com.flo.readinglog.domain.repository.GoogleBooksRepository
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import com.flo.readinglog.domain.usecase.CharacterStatsUseCase
import com.flo.readinglog.domain.validation.EntryValidator
import com.flo.readinglog.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEntryUiState(
    val selectedBook: Book? = null,
    val bookSearchQuery: String = "",
    val bookSearchResults: List<Book> = emptyList(),
    val isSearching: Boolean = false,
    val pageFrom: String = "",
    val pageTo: String = "",
    val impressions: String = "",
    val date: LocalDate = LocalDate.now(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showRollResult: Boolean = false,
    val lastRoll: Int = 0,
    val goldEarned: Int = 0,
    val errorMessage: String? = null,
    val showDatePicker: Boolean = false,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val googleBooksRepository: GoogleBooksRepository,
    private val entryRepository: ReadingEntryRepository,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEntryUiState())
    val uiState: StateFlow<AddEntryUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        searchQuery
            .debounce(400)
            .filter { it.length >= 2 }
            .onEach { query ->
                _uiState.update { it.copy(isSearching = true, bookSearchResults = emptyList()) }
                googleBooksRepository.search(query)
                    .onSuccess { results ->
                        _uiState.update { it.copy(bookSearchResults = results, isSearching = false) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isSearching = false, errorMessage = "Search failed: ${e.message}") }
                    }
            }
            .launchIn(viewModelScope)
    }

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            val book = bookRepository.getById(bookId)
            _uiState.update { it.copy(selectedBook = book) }
        }
    }

    fun onBookSearchQueryChange(query: String) {
        _uiState.update { it.copy(bookSearchQuery = query) }
        searchQuery.value = query
    }

    fun onBookSelected(book: Book) {
        _uiState.update { it.copy(selectedBook = book, bookSearchQuery = "", bookSearchResults = emptyList()) }
    }

    fun onPageFromChange(value: String) = _uiState.update { it.copy(pageFrom = value) }
    fun onPageToChange(value: String) = _uiState.update { it.copy(pageTo = value) }
    fun onImpressionsChange(value: String) = _uiState.update { it.copy(impressions = value) }
    fun onDateChange(date: LocalDate) = _uiState.update { it.copy(date = date, showDatePicker = false) }
    fun onShowDatePicker(show: Boolean) = _uiState.update { it.copy(showDatePicker = show) }
    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }

    fun save() {
        val state = _uiState.value
        val book = state.selectedBook ?: run {
            _uiState.update { it.copy(errorMessage = "Please select a book") }
            return
        }
        when (val v = EntryValidator.validatePageRange(state.pageFrom, state.pageTo)) {
            is ValidationResult.Invalid -> {
                _uiState.update { it.copy(errorMessage = v.message) }
                return
            }
            else -> Unit
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val savedBook = bookRepository.getByGoogleBooksId(book.googleBooksId)
                val bookId = savedBook?.id ?: bookRepository.upsert(book)
                val entry = ReadingEntry(
                    id = 0,
                    bookId = bookId,
                    book = null,
                    pageFrom = state.pageFrom.toInt(),
                    pageTo = state.pageTo.toInt(),
                    impressions = state.impressions,
                    date = state.date,
                    updatedAt = System.currentTimeMillis(),
                )
                entryRepository.upsert(entry)

                val roll = (1..20).random()
                val critBonus = if (roll == 20) 10 else 0
                val goldEarned = CharacterStatsUseCase.goldForPages(entry.pagesRead) + critBonus
                characterRepository.addGold(goldEarned)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true,
                        lastRoll = roll,
                        goldEarned = goldEarned,
                        showRollResult = true,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save: ${e.message}") }
            }
        }
    }
}
