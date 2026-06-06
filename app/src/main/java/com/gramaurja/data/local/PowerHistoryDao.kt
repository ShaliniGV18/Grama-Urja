package com.gramaurja.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gramaurja.data.model.PowerHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PowerHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: PowerHistory)

    @Query("SELECT * FROM power_history WHERE zoneId = :zoneId ORDER BY timestamp DESC LIMIT 100")
    fun getHistoryForZone(zoneId: String): Flow<List<PowerHistory>>

    @Query("SELECT * FROM power_history WHERE zoneId = :zoneId AND timestamp > :since ORDER BY timestamp ASC")
    suspend fun getHistorySince(zoneId: String, since: Long): List<PowerHistory>

    @Query("DELETE FROM power_history WHERE timestamp < :before")
    suspend fun pruneOldHistory(before: Long)

    @Query("SELECT COUNT(*) FROM power_history WHERE zoneId = :zoneId")
    suspend fun countForZone(zoneId: String): Int
}
