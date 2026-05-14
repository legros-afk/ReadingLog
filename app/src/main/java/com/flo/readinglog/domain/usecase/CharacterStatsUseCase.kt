package com.flo.readinglog.domain.usecase

import com.flo.readinglog.domain.model.ReadingEntry
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CharacterStatsUseCase {

    const val PAGES_PER_LEVEL = 500

    fun getLevel(totalPages: Int): Int = (totalPages / PAGES_PER_LEVEL) + 1

    fun getXpIntoLevel(totalPages: Int): Int = totalPages % PAGES_PER_LEVEL

    fun getXpToNextLevel(totalPages: Int): Int = PAGES_PER_LEVEL - (totalPages % PAGES_PER_LEVEL)

    fun computeStreak(entries: List<ReadingEntry>): Int {
        val distinctDates = entries.map { it.date }.distinct().sorted()
        if (distinctDates.isEmpty()) return 0

        val today = LocalDate.now()
        val mostRecent = distinctDates.last()

        if (mostRecent < today.minusDays(1)) return 0

        var streak = 1
        for (i in distinctDates.indices.reversed().drop(1)) {
            if (distinctDates[i] == distinctDates[i + 1].minusDays(1)) streak++
            else break
        }
        return streak
    }

    fun computeLongestStreak(entries: List<ReadingEntry>): Int {
        val distinctDates = entries.map { it.date }.distinct().sorted()
        if (distinctDates.isEmpty()) return 0

        var longest = 1
        var current = 1
        for (i in 1 until distinctDates.size) {
            if (distinctDates[i] == distinctDates[i - 1].plusDays(1)) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }

    // True if the last read was exactly 2 days ago — a shield would bridge the gap.
    fun streakNeedsShield(entries: List<ReadingEntry>): Boolean {
        val mostRecent = entries.map { it.date }.maxOrNull() ?: return false
        return ChronoUnit.DAYS.between(mostRecent, LocalDate.now()) == 2L
    }

    fun goldForPages(pagesRead: Int): Int = pagesRead
}
