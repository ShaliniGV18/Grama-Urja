package com.gramaurja.domain.usecase

import com.gramaurja.data.model.StatusUpdate
import com.gramaurja.data.repository.ZoneRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetZoneStatusUseCase @Inject constructor(
    private val repository: ZoneRepository
) {
    operator fun invoke(zoneId: String): Flow<StatusUpdate> =
        repository.observeZoneStatus(zoneId)
}
