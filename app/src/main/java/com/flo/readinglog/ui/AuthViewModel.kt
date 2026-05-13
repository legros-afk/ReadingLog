package com.flo.readinglog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flo.readinglog.data.sync.FirestoreSyncService
import com.flo.readinglog.domain.repository.AuthRepository
import com.flo.readinglog.worker.DigestWorker
import com.flo.readinglog.worker.SyncWorker
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncService: FirestoreSyncService,
    private val workManager: WorkManager,
) : ViewModel() {

    val isSignedIn: StateFlow<Boolean?> = authRepository.currentUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onSignedIn() {
        viewModelScope.launch {
            syncService.syncAll()
            SyncWorker.schedule(workManager)
            DigestWorker.schedule(workManager)
        }
    }
}
