package com.gramaurja.data.repository

import com.gramaurja.data.model.PowerHistory
import com.gramaurja.data.model.PowerStatus
import com.gramaurja.data.model.StatusUpdate
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for zone power status operations.
 */
interface ZoneRepository {

    /** Real-time stream of status updates for a zone */
    fun observeZoneStatus(zoneId: String): Flow<StatusUpdate>

    /** Update power status in Firebase RTDB */
    suspend fun updatePowerStatus(
        zoneId: String,
        zoneName: String,
        status: PowerStatus,
        userId: String
    ): Result<Unit>

    /** Add a confirmation vote from the current user */
    suspend fun confirmStatus(zoneId: String, userId: String): Result<Unit>

    /** Real-time stream of history entries for a zone from RTDB */
    fun observeZoneHistory(zoneId: String): Flow<List<PowerHistory>>

    /** Get history from local Room cache */
    fun getLocalHistory(zoneId: String): Flow<List<PowerHistory>>

    /** Sync history from RTDB to local Room (called on reconnect) */
    suspend fun syncHistoryFromRtdb(zoneId: String)

    /** Prune history older than 7 days */
    suspend fun pruneOldHistory()

    /** Get all zones (static list for now) */
    fun getAllZones(): List<com.gramaurja.data.model.Zone>
}
