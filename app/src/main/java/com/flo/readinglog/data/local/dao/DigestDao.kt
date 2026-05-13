package com.flo.readinglog.data.local.dao

import androidx.room.*
import com.flo.readinglog.data.local.entity.DigestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DigestDao {
    @Query("SELECT * FROM digests ORDER BY weekStartEpochDay DESC")
    fun observeAll(): Flow<List<DigestEntity>>

    @Query("SELECT * FROM digests WHERE id = :id")
    suspend fun getById(id: Long): DigestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(digest: DigestEntity): Long

    @Query("SELECT * FROM digests WHERE weekStartEpochDay = :weekStartEpochDay")
    suspend fun getByWeekStart(weekStartEpochDay: Long): DigestEntity?

    @Query("SELECT * FROM digests WHERE updatedAt > :since")
    suspend fun getUpdatedSince(since: Long): List<DigestEntity>
}
