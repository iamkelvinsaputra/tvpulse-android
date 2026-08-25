package com.kelvinsaputra.tvpulse.data.remote.api

import com.kelvinsaputra.tvpulse.data.remote.dto.SearchShowDto
import com.kelvinsaputra.tvpulse.data.remote.dto.TvShowDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TvMazeApi {
    @GET("shows")
    suspend fun getShows(): List<TvShowDto>

    @GET("search/shows")
    suspend fun searchShows(
        @Query("q") query: String,
    ): List<SearchShowDto>

    @GET("shows/{id}")
    suspend fun getShow(
        @Path("id") showId: Long,
    ): TvShowDto
}
