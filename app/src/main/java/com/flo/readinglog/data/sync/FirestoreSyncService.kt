package com.flo.readinglog.data.sync

import com.flo.readinglog.data.local.dao.BookDao
import com.flo.readinglog.data.local.dao.DigestDao
import com.flo.readinglog.data.local.dao.ReadingEntryDao
import com.flo.readinglog.data.local.entity.BookEntity
import com.flo.readinglog.data.local.entity.DigestEntity
import com.flo.readinglog.data.local.entity.ReadingEntryEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val bookDao: BookDao,
    private val entryDao: ReadingEntryDao,
    private val digestDao: DigestDao,
) {
    private val uid get() = auth.currentUser?.uid

    suspend fun syncAll() {
        val uid = uid ?: return
        syncBooks(uid)
        syncEntries(uid)
        syncDigests(uid)
    }

    // ── Books ──────────────────────────────────────────────────────────────

    suspend fun syncBooks(uid: String) {
        val localBooks = bookDao.getUpdatedSince(0)
        val col = firestore.collection("users/$uid/books")

        // Push local → Firestore (last-write-wins)
        localBooks.forEach { book ->
            col.document(book.id.toString()).set(book.toMap()).await()
        }

        // Pull Firestore → local
        val remote = col.get().await()
        remote.documents.forEach { doc ->
            val remote = doc.toBookEntity() ?: return@forEach
            val local = bookDao.getById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                bookDao.upsert(remote)
            }
        }
    }

    suspend fun syncEntries(uid: String) {
        val localEntries = entryDao.getUpdatedSince(0)
        val col = firestore.collection("users/$uid/reading_entries")

        localEntries.forEach { entry ->
            col.document(entry.id.toString()).set(entry.toMap()).await()
        }

        val remote = col.get().await()
        remote.documents.forEach { doc ->
            val remote = doc.toReadingEntryEntity() ?: return@forEach
            val local = entryDao.getById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                entryDao.upsert(remote)
            }
        }
    }

    suspend fun syncDigests(uid: String) {
        val localDigests = digestDao.getUpdatedSince(0)
        val col = firestore.collection("users/$uid/digests")

        localDigests.forEach { digest ->
            col.document(digest.id.toString()).set(digest.toMap()).await()
        }

        val remote = col.get().await()
        remote.documents.forEach { doc ->
            val remote = doc.toDigestEntity() ?: return@forEach
            val local = digestDao.getById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                digestDao.upsert(remote)
            }
        }
    }

    // ── Entity → Map ──────────────────────────────────────────────────────

    private fun BookEntity.toMap() = mapOf(
        "id" to id, "googleBooksId" to googleBooksId, "title" to title,
        "authors" to authors, "coverUrl" to (coverUrl ?: ""), "isbn" to (isbn ?: ""),
        "pageCount" to (pageCount ?: 0), "description" to (description ?: ""),
        "publishedDate" to (publishedDate ?: ""), "categories" to (categories ?: ""),
        "isFavourite" to isFavourite, "isWantToRead" to isWantToRead, "updatedAt" to updatedAt
    )

    private fun ReadingEntryEntity.toMap() = mapOf(
        "id" to id, "bookId" to bookId, "pageFrom" to pageFrom, "pageTo" to pageTo,
        "impressions" to impressions, "dateEpochDay" to dateEpochDay, "updatedAt" to updatedAt
    )

    private fun DigestEntity.toMap() = mapOf(
        "id" to id, "weekStartEpochDay" to weekStartEpochDay, "message" to message,
        "sentAt" to sentAt, "updatedAt" to updatedAt
    )

    // ── Map → Entity ──────────────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toBookEntity(): BookEntity? {
        return try {
            BookEntity(
                id = getLong("id") ?: return null,
                googleBooksId = getString("googleBooksId") ?: return null,
                title = getString("title") ?: return null,
                authors = getString("authors") ?: return null,
                coverUrl = getString("coverUrl")?.ifEmpty { null },
                isbn = getString("isbn")?.ifEmpty { null },
                pageCount = getLong("pageCount")?.toInt(),
                description = getString("description")?.ifEmpty { null },
                publishedDate = getString("publishedDate")?.ifEmpty { null },
                categories = getString("categories")?.ifEmpty { null },
                isFavourite = getBoolean("isFavourite") ?: false,
                isWantToRead = getBoolean("isWantToRead") ?: false,
                updatedAt = getLong("updatedAt") ?: 0L,
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReadingEntryEntity(): ReadingEntryEntity? {
        return try {
            ReadingEntryEntity(
                id = getLong("id") ?: return null,
                bookId = getLong("bookId") ?: return null,
                pageFrom = getLong("pageFrom")?.toInt() ?: return null,
                pageTo = getLong("pageTo")?.toInt() ?: return null,
                impressions = getString("impressions") ?: "",
                dateEpochDay = getLong("dateEpochDay") ?: return null,
                updatedAt = getLong("updatedAt") ?: 0L,
            )
        } catch (e: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toDigestEntity(): DigestEntity? {
        return try {
            DigestEntity(
                id = getLong("id") ?: return null,
                weekStartEpochDay = getLong("weekStartEpochDay") ?: return null,
                message = getString("message") ?: return null,
                sentAt = getLong("sentAt") ?: 0L,
                updatedAt = getLong("updatedAt") ?: 0L,
            )
        } catch (e: Exception) { null }
    }
}
