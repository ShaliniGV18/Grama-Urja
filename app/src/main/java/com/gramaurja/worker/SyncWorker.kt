package com.gramaurja.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gramaurja.data.local.PreferencesManager
import com.gramaurja.data.repository.ZoneRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * WorkManager worker that syncs power history from Firebase RTDB
 * when internet connectivity is restored after an offline period.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ZoneRepository,
    private val prefs: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val zoneId = prefs.selectedZoneId.first()
            if (zoneId.isNullOrBlank()) {
                Timber.d("SyncWorker: No zone selected, skipping")
                return Result.success()
            }

            Timber.d("SyncWorker: syncing history for $zoneId")
            repository.syncHistoryFromRtdb(zoneId)
            repository.pruneOldHistory()

            Timber.i("SyncWorker: sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker failed")
            Result.retry()
        }
    }
}
