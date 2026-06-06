package com.gramaurja.di

import com.gramaurja.data.repository.ZoneRepository
import com.gramaurja.data.repository.ZoneRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindZoneRepository(impl: ZoneRepositoryImpl): ZoneRepository
}
