package com.flo.readinglog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.flo.readinglog.data.sync.FirestoreSyncService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncService: FirestoreSyncService,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = try {
        syncService.syncAll()
        Result.success()
    } catch (e: Exception) {
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        const val WORK_NAME = "periodic_sync"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
