package com.kelvinsaputra.tvpulse.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.kelvinsaputra.tvpulse.data.remote.api.TvMazeApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(TV_MAZE_BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideTvMazeApi(retrofit: Retrofit): TvMazeApi =
        retrofit.create(TvMazeApi::class.java)

    private const val TV_MAZE_BASE_URL = "https://api.tvmaze.com/"
}
