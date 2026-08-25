package com.kelvinsaputra.tvpulse.di

import com.kelvinsaputra.tvpulse.data.repository.FakeTvShowRepository
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTvShowRepository(
        implementation: FakeTvShowRepository
    ): TvShowRepository
}