package com.flo.readinglog.ui.screens.history

import androidx.lifecycle.ViewModel
import com.flo.readinglog.domain.model.ReadingEntry
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class HistoryUiState(
    val grouped: Map<LocalDate, List<ReadingEntry>> = emptyMap(),
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    entryRepository: ReadingEntryRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> =
        entryRepository.observeAll()
            .map { entries ->
                val grouped = entries
                    .groupBy { it.date }
                    .toSortedMap(compareByDescending { it })
                HistoryUiState(grouped = grouped)
            }
            .stateIn(
                scope = kotlinx.coroutines.MainScope(),
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoryUiState(),
            )
}
