package com.flo.readinglog.ui.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.data.remote.model.RecommendedBook
import com.flo.readinglog.domain.repository.BookRepository
import com.flo.readinglog.domain.repository.GoogleBooksRepository
import com.flo.readinglog.domain.repository.RecommendationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val recommendations: List<RecommendedBook> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val addingBookTitle: String? = null,
    val noFavouritesWarning: Boolean = false,
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val recommendationsRepository: RecommendationsRepository,
    private val googleBooksRepository: GoogleBooksRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    fun getRecommendations() {
        viewModelScope.launch {
            val favourites = bookRepository.observeFavourites().first()
            if (favourites.isEmpty()) {
                _uiState.update { it.copy(noFavouritesWarning = true) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, noFavouritesWarning = false, recommendations = emptyList()) }
            val bookDescriptions = favourites.map { book ->
                "${book.title} by ${book.authors.joinToString(", ")}" +
                        if (book.categories.isNotEmpty()) " (${book.categories.take(2).joinToString(", ")})" else ""
            }
            recommendationsRepository.getRecommendations(bookDescriptions)
                .onSuccess { recs ->
                    _uiState.update { it.copy(recommendations = recs, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to get recommendations: ${e.message}") }
                }
        }
    }

    fun addToMyList(rec: RecommendedBook) {
        viewModelScope.launch {
            _uiState.update { it.copy(addingBookTitle = rec.title) }
            googleBooksRepository.search("${rec.title} ${rec.author}")
                .onSuccess { results ->
                    val match = results.firstOrNull()
                    if (match != null) {
                        val existing = bookRepository.getByGoogleBooksId(match.googleBooksId)
                        if (existing == null) {
                            bookRepository.upsert(match.copy(isWantToRead = true))
                        } else {
                            bookRepository.setWantToRead(existing.id, true)
                        }
                    } else {
                        _uiState.update { it.copy(errorMessage = "Couldn't find '${rec.title}' in Google Books") }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = "Search failed: ${e.message}") }
                }
            _uiState.update { it.copy(addingBookTitle = null) }
        }
    }

    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }
    fun dismissNoFavouritesWarning() = _uiState.update { it.copy(noFavouritesWarning = false) }
}
