package com.flo.readinglog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "digests")
data class DigestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartEpochDay: Long,
    val message: String,
    val sentAt: Long,
    val updatedAt: Long = System.currentTimeMillis(),
)
