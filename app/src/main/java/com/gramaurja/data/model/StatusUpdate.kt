package com.gramaurja.data.model

/**
 * Snapshot of a zone's current status from Firebase RTDB.
 * Maps directly to /zones/{zone_id} node.
 */
data class StatusUpdate(
    val zoneId: String = "",
    val zoneName: String = "",
    val status: PowerStatus = PowerStatus.UNKNOWN,
    val timestamp: Long = 0L,
    val updatedBy: String = "",
    val confirmCount: Int = 0
)

/** Firebase RTDB raw model (uses String for status since RTDB stores strings) */
data class ZoneRtdbModel(
    val name: String = "",
    val status: String = "UNKNOWN",
    val timestamp: Long = 0L,
    val updatedBy: String = "",
    val confirmCount: Int = 0,
    val lastThrottleUpdate: Long = 0L
) {
    fun toStatusUpdate(zoneId: String): StatusUpdate = StatusUpdate(
        zoneId = zoneId,
        zoneName = name,
        status = try { PowerStatus.valueOf(status) } catch (e: Exception) { PowerStatus.UNKNOWN },
        timestamp = timestamp,
        updatedBy = updatedBy,
        confirmCount = confirmCount
    )
}
