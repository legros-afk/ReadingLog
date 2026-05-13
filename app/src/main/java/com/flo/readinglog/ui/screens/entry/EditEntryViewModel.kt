package com.flo.readinglog.ui.screens.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.domain.model.ReadingEntry
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import com.flo.readinglog.domain.validation.EntryValidator
import com.flo.readinglog.domain.validation.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class EditEntryUiState(
    val entry: ReadingEntry? = null,
    val pageFrom: String = "",
    val pageTo: String = "",
    val impressions: String = "",
    val date: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteConfirm: Boolean = false,
    val showDatePicker: Boolean = false,
)

@HiltViewModel
class EditEntryViewModel @Inject constructor(
    private val entryRepository: ReadingEntryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditEntryUiState())
    val uiState: StateFlow<EditEntryUiState> = _uiState.asStateFlow()

    fun load(entryId: Long) {
        viewModelScope.launch {
            val entry = entryRepository.getById(entryId)
            if (entry != null) {
                _uiState.update {
                    it.copy(
                        entry = entry,
                        pageFrom = entry.pageFrom.toString(),
                        pageTo = entry.pageTo.toString(),
                        impressions = entry.impressions,
                        date = entry.date,
                        isLoading = false,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Entry not found") }
            }
        }
    }

    fun onPageFromChange(value: String) = _uiState.update { it.copy(pageFrom = value) }
    fun onPageToChange(value: String) = _uiState.update { it.copy(pageTo = value) }
    fun onImpressionsChange(value: String) = _uiState.update { it.copy(impressions = value) }
    fun onDateChange(date: LocalDate) = _uiState.update { it.copy(date = date, showDatePicker = false) }
    fun onShowDatePicker(show: Boolean) = _uiState.update { it.copy(showDatePicker = show) }
    fun onShowDeleteConfirm(show: Boolean) = _uiState.update { it.copy(showDeleteConfirm = show) }
    fun onErrorShown() = _uiState.update { it.copy(errorMessage = null) }

    fun save() {
        val state = _uiState.value
        val entry = state.entry ?: return
        when (val v = EntryValidator.validatePageRange(state.pageFrom, state.pageTo)) {
            is ValidationResult.Invalid -> { _uiState.update { it.copy(errorMessage = v.message) }; return }
            else -> Unit
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                entryRepository.update(
                    entry.copy(
                        pageFrom = state.pageFrom.toInt(),
                        pageTo = state.pageTo.toInt(),
                        impressions = state.impressions,
                        date = state.date,
                        updatedAt = System.currentTimeMillis(),
                    )
                )
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save: ${e.message}") }
            }
        }
    }

    fun delete() {
        val entryId = _uiState.value.entry?.id ?: return
        viewModelScope.launch {
            try {
                entryRepository.delete(entryId)
                _uiState.update { it.copy(deletedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete: ${e.message}") }
            }
        }
    }
}
