package com.gramaurja.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching power history locally.
 * Also used for AI prediction input.
 */
@Entity(tableName = "power_history")
data class PowerHistory(
    @PrimaryKey
    val id: String,          // zoneId + "_" + timestamp
    val zoneId: String,
    val status: String,      // "ON" or "OFF"
    val timestamp: Long,
    val reportedBy: String = ""
) {
    fun powerStatus(): PowerStatus =
        try { PowerStatus.valueOf(status) } catch (e: Exception) { PowerStatus.UNKNOWN }
}
