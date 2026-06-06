package com.gramaurja.di

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.gramaurja.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideGeminiModel(): GenerativeModel? {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isNotBlank()) {
            try {
                GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = key
                )
            } catch (e: Exception) {
                Timber.w(e, "Failed to create Gemini model")
                null
            }
        } else {
            Timber.w("No Gemini API key – AI prediction disabled")
            null
        }
    }
}
