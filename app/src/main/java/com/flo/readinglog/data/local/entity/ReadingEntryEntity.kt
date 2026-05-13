package com.flo.readinglog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_entries",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("bookId")]
)
data class ReadingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val pageFrom: Int,
    val pageTo: Int,
    val impressions: String,
    val dateEpochDay: Long,       // LocalDate.toEpochDay()
    val updatedAt: Long = System.currentTimeMillis(),
)
