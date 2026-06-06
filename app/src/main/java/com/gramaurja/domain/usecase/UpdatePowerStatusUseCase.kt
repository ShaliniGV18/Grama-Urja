package com.gramaurja.domain.usecase

import com.gramaurja.data.local.PreferencesManager
import com.gramaurja.data.model.PowerStatus
import com.gramaurja.data.repository.ZoneRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

/** Throttle constant: 30 seconds between updates */
private const val UPDATE_THROTTLE_MS = 30_000L

class UpdatePowerStatusUseCase @Inject constructor(
    private val repository: ZoneRepository,
    private val prefs: PreferencesManager
) {
    /**
     * Updates power status for a zone.
     * Returns Result.failure if the user updated too recently (spam prevention).
     */
    suspend operator fun invoke(
        zoneId: String,
        zoneName: String,
        status: PowerStatus,
        userId: String
    ): Result<Unit> {
        // Throttle check
        val lastUpdate = prefs.lastUpdateTime.first()
        val now = System.currentTimeMillis()
        if ((now - lastUpdate) < UPDATE_THROTTLE_MS) {
            val waitSeconds = ((UPDATE_THROTTLE_MS - (now - lastUpdate)) / 1000).toInt()
            Timber.w("Update throttled. Wait ${waitSeconds}s")
            return Result.failure(
                IllegalStateException("Please wait ${waitSeconds} seconds before updating again")
            )
        }

        val result = repository.updatePowerStatus(zoneId, zoneName, status, userId)
        if (result.isSuccess) {
            prefs.recordUpdateTime(now)
            prefs.cacheStatus(status, now)
        }
        return result
    }
}
