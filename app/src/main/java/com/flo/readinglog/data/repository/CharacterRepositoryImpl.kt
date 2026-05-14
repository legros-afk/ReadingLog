package com.flo.readinglog.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flo.readinglog.domain.model.CharacterStats
import com.flo.readinglog.domain.repository.CharacterRepository
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import com.flo.readinglog.domain.usecase.CharacterStatsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

class CharacterRepositoryImpl @Inject constructor(
    @Named("character") private val dataStore: DataStore<Preferences>,
    private val readingEntryRepository: ReadingEntryRepository,
) : CharacterRepository {

    companion object {
        private val GOLD = intPreferencesKey("gold_pieces")
        private val SHIELDS = intPreferencesKey("paladin_shields")
        private val SHIELD_USED_DATE = stringPreferencesKey("shield_used_date")
    }

    override fun observeStats(): Flow<CharacterStats> =
        combine(readingEntryRepository.observeAll(), dataStore.data) { entries, prefs ->
            val totalPages = entries.sumOf { it.pagesRead }
            val gold = prefs[GOLD] ?: 0
            val shields = prefs[SHIELDS] ?: 3
            val shieldUsedToday = prefs[SHIELD_USED_DATE] == LocalDate.now().toString()

            CharacterStats(
                totalPages = totalPages,
                level = CharacterStatsUseCase.getLevel(totalPages),
                xpIntoCurrentLevel = CharacterStatsUseCase.getXpIntoLevel(totalPages),
                xpToNextLevel = CharacterStatsUseCase.getXpToNextLevel(totalPages),
                currentStreak = CharacterStatsUseCase.computeStreak(entries),
                longestStreak = CharacterStatsUseCase.computeLongestStreak(entries),
                goldPieces = gold,
                paladinShields = shields,
                shieldConsumedToday = shieldUsedToday,
            )
        }

    override suspend fun addGold(amount: Int) {
        dataStore.edit { it[GOLD] = (it[GOLD] ?: 0) + amount }
    }

    override suspend fun spendGold(amount: Int): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val current = prefs[GOLD] ?: 0
            if (current >= amount) {
                prefs[GOLD] = current - amount
                success = true
            }
        }
        return success
    }

    override suspend fun addShield() {
        dataStore.edit { it[SHIELDS] = (it[SHIELDS] ?: 3) + 1 }
    }

    override suspend fun consumeShield(): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val current = prefs[SHIELDS] ?: 0
            if (current > 0) {
                prefs[SHIELDS] = current - 1
                success = true
            }
        }
        return success
    }

    override suspend fun recordShieldUsedToday() {
        dataStore.edit { it[SHIELD_USED_DATE] = LocalDate.now().toString() }
    }
}
