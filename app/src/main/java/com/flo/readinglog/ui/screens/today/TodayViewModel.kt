package com.flo.readinglog.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.domain.model.CharacterStats
import com.flo.readinglog.domain.model.ReadingEntry
import com.flo.readinglog.domain.repository.CharacterRepository
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import com.flo.readinglog.domain.usecase.CharacterStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val entries: List<ReadingEntry> = emptyList(),
    val totalPagesToday: Int = 0,
    val characterStats: CharacterStats = CharacterStats.Empty,
    val heatmapData: Map<LocalDate, Int> = emptyMap(),
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val entryRepository: ReadingEntryRepository,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> =
        combine(
            entryRepository.observeAll(),
            characterRepository.observeStats(),
        ) { allEntries, stats ->
            val today = LocalDate.now()
            val todayEntries = allEntries.filter { it.date == today }
            TodayUiState(
                entries = todayEntries,
                totalPagesToday = todayEntries.sumOf { it.pagesRead },
                characterStats = stats,
                heatmapData = allEntries
                    .groupBy { it.date }
                    .mapValues { (_, e) -> e.sumOf { it.pagesRead } },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState(),
        )

    init {
        viewModelScope.launch {
            val stats = characterRepository.observeStats().first()
            if (!stats.shieldConsumedToday) {
                val allEntries = entryRepository.observeAll().first()
                if (CharacterStatsUseCase.streakNeedsShield(allEntries)) {
                    if (characterRepository.consumeShield()) {
                        characterRepository.recordShieldUsedToday()
                    }
                }
            }
        }
    }
}
