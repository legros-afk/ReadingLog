package com.flo.readinglog.domain.repository

import com.flo.readinglog.domain.model.CharacterStats
import com.flo.readinglog.domain.model.ShopItem
import com.flo.readinglog.domain.model.ShopPurchase
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun observeStats(): Flow<CharacterStats>
    fun observePurchases(): Flow<List<ShopPurchase>>
    suspend fun addGold(amount: Int)
    suspend fun spendGold(amount: Int): Boolean
    suspend fun addShield()
    suspend fun consumeShield(): Boolean
    suspend fun recordShieldUsedToday()
    suspend fun requestReward(item: ShopItem): Boolean
}
