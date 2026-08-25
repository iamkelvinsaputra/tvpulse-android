package com.kelvinsaputra.tvpulse.di

import com.kelvinsaputra.tvpulse.data.repository.DefaultTvShowRepository
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
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
    abstract fun bindTvShowRepository(
        implementation: DefaultTvShowRepository,
    ): TvShowRepository
}
