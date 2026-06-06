package com.gramaurja.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gramaurja.data.model.PowerHistory

@Database(
    entities = [PowerHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun powerHistoryDao(): PowerHistoryDao
}
