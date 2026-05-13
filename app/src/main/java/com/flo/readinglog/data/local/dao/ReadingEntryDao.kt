package com.flo.readinglog.data.local.dao

import androidx.room.*
import com.flo.readinglog.data.local.entity.ReadingEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingEntryDao {
    @Query("SELECT * FROM reading_entries ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<ReadingEntryEntity>>

    @Query("SELECT * FROM reading_entries WHERE dateEpochDay = :epochDay ORDER BY id DESC")
    fun observeByDate(epochDay: Long): Flow<List<ReadingEntryEntity>>

    @Query("SELECT * FROM reading_entries WHERE bookId = :bookId ORDER BY dateEpochDay DESC")
    fun observeByBook(bookId: Long): Flow<List<ReadingEntryEntity>>

    @Query("SELECT * FROM reading_entries WHERE id = :id")
    suspend fun getById(id: Long): ReadingEntryEntity?

    @Query("SELECT * FROM reading_entries WHERE dateEpochDay >= :fromEpochDay AND dateEpochDay <= :toEpochDay")
    suspend fun getInRange(fromEpochDay: Long, toEpochDay: Long): List<ReadingEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ReadingEntryEntity): Long

    @Update
    suspend fun update(entry: ReadingEntryEntity)

    @Delete
    suspend fun delete(entry: ReadingEntryEntity)

    @Query("SELECT * FROM reading_entries WHERE updatedAt > :since")
    suspend fun getUpdatedSince(since: Long): List<ReadingEntryEntity>
}
