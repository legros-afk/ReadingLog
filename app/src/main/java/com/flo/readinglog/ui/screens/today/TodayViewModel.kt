package com.flo.readinglog.ui.screens.today

import androidx.lifecycle.ViewModel
import com.flo.readinglog.domain.model.ReadingEntry
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val entries: List<ReadingEntry> = emptyList(),
    val totalPagesToday: Int = 0,
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    entryRepository: ReadingEntryRepository,
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> =
        entryRepository.observeByDate(LocalDate.now())
            .map { entries ->
                TodayUiState(
                    entries = entries,
                    totalPagesToday = entries.sumOf { it.pagesRead },
                )
            }
            .stateIn(
                scope = kotlinx.coroutines.MainScope(),
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TodayUiState(),
            )
}
