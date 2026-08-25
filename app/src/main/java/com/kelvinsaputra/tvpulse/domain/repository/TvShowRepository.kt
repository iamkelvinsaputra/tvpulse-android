package com.kelvinsaputra.tvpulse.domain.repository

import com.kelvinsaputra.tvpulse.domain.model.TvShow

interface TvShowRepository {
    suspend fun getTopShows(): List<TvShow>
    suspend fun searchShows(query: String): List<TvShow>
    suspend fun getShowDetail(showId: Long): TvShow
}
