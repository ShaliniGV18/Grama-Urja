package com.gramaurja.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gramaurja.data.model.PowerStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "grama_urja_prefs")

/**
 * DataStore-based preferences manager.
 * Stores: selected zone, cached power status, cached timestamp, FCM token.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_SELECTED_ZONE_ID = stringPreferencesKey("selected_zone_id")
        val KEY_SELECTED_ZONE_NAME = stringPreferencesKey("selected_zone_name")
        val KEY_CACHED_STATUS = stringPreferencesKey("cached_status")
        val KEY_CACHED_TIMESTAMP = longPreferencesKey("cached_timestamp")
        val KEY_FCM_TOKEN = stringPreferencesKey("fcm_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        val KEY_LANGUAGE = stringPreferencesKey("language")
    }

    // Selected zone
    val selectedZoneId: Flow<String?> = context.dataStore.data
        .map { it[KEY_SELECTED_ZONE_ID] }
        .distinctUntilChanged()

    val selectedZoneName: Flow<String?> = context.dataStore.data
        .map { it[KEY_SELECTED_ZONE_NAME] }
        .distinctUntilChanged()

    // Cached status (offline support)
    val cachedStatus: Flow<PowerStatus> = context.dataStore.data
        .map { prefs ->
            val raw = prefs[KEY_CACHED_STATUS] ?: "UNKNOWN"
            try { PowerStatus.valueOf(raw) } catch (e: Exception) { PowerStatus.UNKNOWN }
        }
        .distinctUntilChanged()

    val cachedTimestamp: Flow<Long> = context.dataStore.data
        .map { it[KEY_CACHED_TIMESTAMP] ?: 0L }
        .distinctUntilChanged()

    val lastUpdateTime: Flow<Long> = context.dataStore.data
        .map { it[KEY_LAST_UPDATE_TIME] ?: 0L }
        .distinctUntilChanged()

    val userId: Flow<String?> = context.dataStore.data
        .map { it[KEY_USER_ID] }
        .distinctUntilChanged()

    suspend fun saveSelectedZone(zoneId: String, zoneName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_ZONE_ID] = zoneId
            prefs[KEY_SELECTED_ZONE_NAME] = zoneName
        }
    }

    suspend fun cacheStatus(status: PowerStatus, timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CACHED_STATUS] = status.name
            prefs[KEY_CACHED_TIMESTAMP] = timestamp
        }
    }

    suspend fun saveUserId(uid: String) {
        context.dataStore.edit { it[KEY_USER_ID] = uid }
    }

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { it[KEY_FCM_TOKEN] = token }
    }

    suspend fun recordUpdateTime(time: Long) {
        context.dataStore.edit { it[KEY_LAST_UPDATE_TIME] = time }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
