package com.flo.readinglog.domain.repository

import com.flo.readinglog.domain.model.CharacterStats
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun observeStats(): Flow<CharacterStats>
    suspend fun addGold(amount: Int)
    suspend fun spendGold(amount: Int): Boolean
    suspend fun addShield()
    suspend fun consumeShield(): Boolean
    suspend fun recordShieldUsedToday()
}
