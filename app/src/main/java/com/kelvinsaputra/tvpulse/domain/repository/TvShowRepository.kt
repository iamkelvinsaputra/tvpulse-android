package com.kelvinsaputra.tvpulse.domain.repository

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import kotlinx.coroutines.flow.Flow

interface TvShowRepository {
    suspend fun getTopShows(): List<TvShow>
    suspend fun searchShows(query: String): List<TvShow>
    suspend fun getShowDetail(showId: Long): TvShow
    fun observeFavorites(): Flow<List<TvShow>>
    fun observeIsFavorite(showId: Long): Flow<Boolean>
    suspend fun setFavorite(show: TvShow, isFavorite: Boolean)
}
