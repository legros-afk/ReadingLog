package com.flo.readinglog.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flo.readinglog.domain.model.CharacterStats
import com.flo.readinglog.domain.model.ShopItem
import com.flo.readinglog.domain.model.ShopPurchase
import com.flo.readinglog.domain.repository.CharacterRepository
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import com.flo.readinglog.domain.usecase.CharacterStatsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class CharacterRepositoryImpl @Inject constructor(
    @Named("character") private val dataStore: DataStore<Preferences>,
    private val readingEntryRepository: ReadingEntryRepository,
    private val json: Json,
) : CharacterRepository {

    companion object {
        private val GOLD = intPreferencesKey("gold_pieces")
        private val SHIELDS = intPreferencesKey("paladin_shields")
        private val SHIELD_USED_DATE = stringPreferencesKey("shield_used_date")
        private val PURCHASES = stringPreferencesKey("shop_purchases")
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

    override fun observePurchases(): Flow<List<ShopPurchase>> =
        dataStore.data.map { prefs ->
            val raw = prefs[PURCHASES] ?: "[]"
            try { json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
        }

    override suspend fun addGold(amount: Int) {
        dataStore.edit { it[GOLD] = (it[GOLD] ?: 0) + amount }
    }

    override suspend fun spendGold(amount: Int): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val current = prefs[GOLD] ?: 0
            if (current >= amount) { prefs[GOLD] = current - amount; success = true }
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
            if (current > 0) { prefs[SHIELDS] = current - 1; success = true }
        }
        return success
    }

    override suspend fun recordShieldUsedToday() {
        dataStore.edit { it[SHIELD_USED_DATE] = LocalDate.now().toString() }
    }

    override suspend fun requestReward(item: ShopItem): Boolean {
        var success = false
        dataStore.edit { prefs ->
            val gold = prefs[GOLD] ?: 0
            if (gold >= item.cost) {
                prefs[GOLD] = gold - item.cost
                val existing: List<ShopPurchase> = try {
                    json.decodeFromString(prefs[PURCHASES] ?: "[]")
                } catch (_: Exception) { emptyList() }
                val updated = existing + ShopPurchase(
                    id = UUID.randomUUID().toString(),
                    itemId = item.id,
                    itemName = item.name,
                    emoji = item.emoji,
                    cost = item.cost,
                    purchasedAt = System.currentTimeMillis(),
                )
                prefs[PURCHASES] = json.encodeToString(updated)
                success = true
            }
        }
        return success
    }
}
