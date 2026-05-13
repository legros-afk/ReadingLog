package com.flo.readinglog.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    fun isSignedIn(): Boolean
    suspend fun signOut()
}
