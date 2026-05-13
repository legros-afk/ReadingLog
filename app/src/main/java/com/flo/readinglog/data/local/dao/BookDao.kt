package com.flo.readinglog.data.local.dao

import androidx.room.*
import com.flo.readinglog.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun observeAll(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isFavourite = 1 ORDER BY title ASC")
    fun observeFavourites(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isWantToRead = 1 ORDER BY title ASC")
    fun observeWantToRead(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE googleBooksId = :googleBooksId LIMIT 1")
    suspend fun getByGoogleBooksId(googleBooksId: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    @Query("SELECT * FROM books WHERE updatedAt > :since")
    suspend fun getUpdatedSince(since: Long): List<BookEntity>
}
