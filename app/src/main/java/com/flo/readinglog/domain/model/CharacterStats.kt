package com.flo.readinglog.domain.model

data class CharacterStats(
    val totalPages: Int,
    val level: Int,
    val xpIntoCurrentLevel: Int,
    val xpToNextLevel: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val goldPieces: Int,
    val paladinShields: Int,
    val shieldConsumedToday: Boolean,
) {
    companion object {
        val Empty = CharacterStats(
            totalPages = 0,
            level = 1,
            xpIntoCurrentLevel = 0,
            xpToNextLevel = 500,
            currentStreak = 0,
            longestStreak = 0,
            goldPieces = 0,
            paladinShields = 3,
            shieldConsumedToday = false,
        )
    }
}
