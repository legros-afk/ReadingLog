package com.flo.readinglog.domain.model

import kotlinx.serialization.Serializable

data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val emoji: String,
)

@Serializable
data class ShopPurchase(
    val id: String,
    val itemId: String,
    val itemName: String,
    val emoji: String,
    val cost: Int,
    val purchasedAt: Long,
)

val SHOP_ITEMS = listOf(
    ShopItem(
        id = "long_rest",
        name = "The Long Rest",
        description = "A proper screen time session tonight — you've earned it!",
        cost = 15,
        emoji = "🛌",
    ),
    ShopItem(
        id = "strategist",
        name = "The Strategist",
        description = "You pick the game or activity of the day!",
        cost = 25,
        emoji = "🧠",
    ),
    ShopItem(
        id = "epic_quest",
        name = "The Epic Quest",
        description = "A big day out or special adventure of your choice!",
        cost = 100,
        emoji = "🗺️",
    ),
)
