package com.gramaurja.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.gramaurja.data.local.PowerHistoryDao
import com.gramaurja.data.model.PowerHistory
import com.gramaurja.data.model.PowerStatus
import com.gramaurja.data.model.SampleZones
import com.gramaurja.data.model.StatusUpdate
import com.gramaurja.data.model.Zone
import com.gramaurja.data.model.ZoneRtdbModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Realtime Database implementation of ZoneRepository.
 *
 * RTDB structure:
 *   /zones/{zone_id}/status        → "ON" | "OFF" | "UNKNOWN"
 *   /zones/{zone_id}/timestamp     → server timestamp (long)
 *   /zones/{zone_id}/confirmCount  → int
 *   /zones/{zone_id}/history/{id}  → { status, timestamp }
 */
@Singleton
class ZoneRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val historyDao: PowerHistoryDao
) : ZoneRepository {

    private val zonesRef by lazy { 
        database.getReference("zones").apply { keepSynced(true) }
    }

    /** Real-time Flow using Firebase ValueEventListener */
    override fun observeZoneStatus(zoneId: String): Flow<StatusUpdate> = callbackFlow {
        val ref = zonesRef.child(zoneId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) return

                    val name = snapshot.child("name").getValue(String::class.java) ?: zoneId
                    val statusStr = snapshot.child("status").getValue(String::class.java) ?: "UNKNOWN"
                    
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val updatedBy = snapshot.child("updatedBy").getValue(String::class.java) ?: ""
                    val confirmCount = snapshot.child("confirmCount").getValue(Int::class.java) ?: 0

                    val status = try { PowerStatus.valueOf(statusStr) } catch (e: Exception) { PowerStatus.UNKNOWN }

                    if (status != PowerStatus.UNKNOWN) {
                        trySend(StatusUpdate(
                            zoneId = zoneId,
                            zoneName = name,
                            status = status,
                            timestamp = timestamp,
                            updatedBy = updatedBy,
                            confirmCount = confirmCount
                        ))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse RTDB snapshot for zone: $zoneId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("RTDB listener cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun updatePowerStatus(
        zoneId: String,
        zoneName: String,
        status: PowerStatus,
        userId: String
    ): Result<Unit> {
        return try {
            val ref = zonesRef.child(zoneId)
            val historyId = UUID.randomUUID().toString()

            // Atomic update: status + reset confirmCount + add to history
            val updates = mapOf(
                "name" to zoneName,
                "status" to status.name,
                "timestamp" to ServerValue.TIMESTAMP,
                "updatedBy" to userId,
                "confirmCount" to 1,  // reporter = first confirm
                "history/$historyId/status" to status.name,
                "history/$historyId/timestamp" to ServerValue.TIMESTAMP,
                "history/$historyId/reportedBy" to userId
            )

            ref.updateChildren(updates).await()

            // Also cache locally
            val now = System.currentTimeMillis()
            historyDao.insertHistory(
                PowerHistory(
                    id = "${zoneId}_$now",
                    zoneId = zoneId,
                    status = status.name,
                    timestamp = now,
                    reportedBy = userId
                )
            )

            Timber.i("Status updated to ${status.name} for zone $zoneId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update status for zone $zoneId")
            Result.failure(e)
        }
    }

    override suspend fun confirmStatus(zoneId: String, userId: String): Result<Unit> {
        return try {
            val ref = zonesRef.child(zoneId).child("confirmCount")
            // Atomic increment using transaction
            ref.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                    val current = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = current + 1
                    return com.google.firebase.database.Transaction.success(currentData)
                }
                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    if (error != null) Timber.e("Confirm transaction failed: ${error.message}")
                }
            })
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Confirm status failed")
            Result.failure(e)
        }
    }

    override fun observeZoneHistory(zoneId: String): Flow<List<PowerHistory>> = callbackFlow {
        val ref = zonesRef.child(zoneId).child("history").limitToLast(50)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<PowerHistory>()
                snapshot.children.forEach { child ->
                    val status = child.child("status").getValue(String::class.java) ?: "UNKNOWN"
                    val timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L
                    val reportedBy = child.child("reportedBy").getValue(String::class.java) ?: ""
                    
                    val historyItem = PowerHistory(
                        id = child.key ?: "${zoneId}_$timestamp",
                        zoneId = zoneId,
                        status = status,
                        timestamp = timestamp,
                        reportedBy = reportedBy
                    )
                    list.add(historyItem)
                    
                    // Pipe to local Room for permanent presence
                    this@callbackFlow.launch { historyDao.insertHistory(historyItem) }
                }
                trySend(list.sortedByDescending { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("History listener cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getLocalHistory(zoneId: String): Flow<List<PowerHistory>> {
        return historyDao.getHistoryForZone(zoneId)
    }

    override suspend fun syncHistoryFromRtdb(zoneId: String) {
        try {
            val snapshot = zonesRef.child(zoneId).child("history")
                .orderByChild("timestamp")
                .limitToLast(50)
                .get()
                .await()

            snapshot.children.forEach { child ->
                val statusStr = child.child("status").getValue(String::class.java) ?: return@forEach
                val timestamp = child.child("timestamp").getValue(Long::class.java) ?: return@forEach
                val reportedBy = child.child("reportedBy").getValue(String::class.java) ?: ""

                historyDao.insertHistory(
                    PowerHistory(
                        id = "${zoneId}_$timestamp",
                        zoneId = zoneId,
                        status = statusStr,
                        timestamp = timestamp,
                        reportedBy = reportedBy
                    )
                )
            }
            Timber.d("Synced history for zone $zoneId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync history for zone $zoneId")
        }
    }

    override suspend fun pruneOldHistory() {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        historyDao.pruneOldHistory(sevenDaysAgo)
    }

    override fun getAllZones(): List<Zone> = SampleZones.zones
}
