package com.gramaurja.di

import android.content.Context
import androidx.room.Room
import com.gramaurja.data.local.AppDatabase
import com.gramaurja.data.local.PowerHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "grama_urja_db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providePowerHistoryDao(db: AppDatabase): PowerHistoryDao = db.powerHistoryDao()
}
