package com.flo.readinglog.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.flo.readinglog.data.remote.WhatsAppNotifier
import com.flo.readinglog.domain.model.Digest
import com.flo.readinglog.domain.repository.DigestRepository
import com.flo.readinglog.domain.repository.ReadingEntryRepository
import com.flo.readinglog.domain.repository.BookRepository
import com.flo.readinglog.domain.repository.SettingsRepository
import com.flo.readinglog.domain.usecase.BuildDigestUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

@HiltWorker
class DigestWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val readingEntryRepository: ReadingEntryRepository,
    private val bookRepository: BookRepository,
    private val digestRepository: DigestRepository,
    private val settingsRepository: SettingsRepository,
    private val whatsAppNotifier: WhatsAppNotifier,
    private val buildDigestUseCase: BuildDigestUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val weekStartMs = weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val entries = readingEntryRepository.getInRange(weekStart, today)
        val wishlistBooks = bookRepository.getUpdatedSince(weekStartMs).filter { it.isWantToRead }

        val message = buildDigestUseCase(entries, wishlistBooks, weekStart)

        // Upsert by week — use existing ID if this week's digest was already saved (e.g. on retry)
        val existingId = digestRepository.getByWeekStart(weekStart)?.id ?: 0L
        digestRepository.upsert(
            Digest(
                id = existingId,
                weekStart = weekStart,
                message = message,
                sentAt = System.currentTimeMillis(),
            )
        )

        val settings = settingsRepository.settings.first()
        if (settings.baseUrl.isNotBlank() && settings.parentNumber.isNotBlank()) {
            val result = whatsAppNotifier.send(
                baseUrl = settings.baseUrl,
                apiToken = settings.apiToken,
                to = settings.parentNumber,
                message = message,
            )
            if (result.isFailure) {
                return if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "weekly_digest"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<DigestWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(delayToNextFriday18(), TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        private fun delayToNextFriday18(): Long {
            val now = LocalDateTime.now()
            var target = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .withHour(18).withMinute(0).withSecond(0).withNano(0)
            if (!target.isAfter(now)) target = target.plusWeeks(1)
            return ChronoUnit.MILLIS.between(now, target)
        }
    }
}
