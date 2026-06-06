package com.gramaurja.domain.usecase

import com.gramaurja.data.model.PowerHistory
import com.gramaurja.data.repository.ZoneRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPowerHistoryUseCase @Inject constructor(
    private val repository: ZoneRepository
) {
    /** Returns a Flow of real-time history entries from RTDB */
    fun observeLive(zoneId: String): Flow<List<PowerHistory>> =
        repository.observeZoneHistory(zoneId)

    /** Returns a Flow of cached history entries from Room */
    operator fun invoke(zoneId: String): Flow<List<PowerHistory>> =
        repository.getLocalHistory(zoneId)

    /** Sync history from RTDB when connectivity is restored */
    suspend fun sync(zoneId: String) = repository.syncHistoryFromRtdb(zoneId)
}
