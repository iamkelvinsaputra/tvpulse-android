package com.kelvinsaputra.tvpulse.domain.repository

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import kotlinx.coroutines.flow.Flow

interface TvShowRepository {
    fun observeHomeShows(limit: Int): Flow<List<TvShow>>
    suspend fun hasHomeCache(): Boolean
    suspend fun getTopShows(): List<TvShow>
    suspend fun loadMoreHomeShows(targetCount: Int): Boolean

    fun observeSearchShows(query: String, limit: Int): Flow<List<TvShow>>
    suspend fun hasSearchCache(query: String): Boolean
    suspend fun searchShows(query: String): List<TvShow>

    fun observeShowDetail(showId: Long): Flow<TvShow?>
    suspend fun getShowDetail(showId: Long): TvShow

    fun observeFavorites(limit: Int): Flow<List<TvShow>>
    suspend fun canLoadMoreFavorites(visibleCount: Int): Boolean
    suspend fun refreshFavorites(showIds: List<Long>)
    fun observeIsFavorite(showId: Long): Flow<Boolean>
    suspend fun setFavorite(show: TvShow, isFavorite: Boolean)
}
