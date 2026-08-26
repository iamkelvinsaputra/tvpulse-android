package com.kelvinsaputra.tvpulse.data.repository

import com.kelvinsaputra.tvpulse.data.local.dao.FavoriteShowDao
import com.kelvinsaputra.tvpulse.data.mapper.toDomain
import com.kelvinsaputra.tvpulse.data.mapper.toFavoriteEntity
import com.kelvinsaputra.tvpulse.data.remote.api.TvMazeApi
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultTvShowRepository @Inject constructor(
    private val api: TvMazeApi,
    private val favoriteShowDao: FavoriteShowDao,
) : TvShowRepository {

    override suspend fun getTopShows(): List<TvShow> =
        api.getShows()
            .take(HOME_SHOW_LIMIT)
            .map { it.toDomain() }

    override suspend fun searchShows(query: String): List<TvShow> =
        api.searchShows(query)
            .map { it.show.toDomain() }

    override suspend fun getShowDetail(showId: Long): TvShow =
        api.getShow(showId).toDomain()

    override fun observeFavorites(): Flow<List<TvShow>> =
        favoriteShowDao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeIsFavorite(showId: Long): Flow<Boolean> =
        favoriteShowDao.observeIsFavorite(showId)

    override suspend fun setFavorite(show: TvShow, isFavorite: Boolean) {
        if (isFavorite) {
            favoriteShowDao.upsert(show.toFavoriteEntity())
        } else {
            favoriteShowDao.deleteById(show.id)
        }
    }

    private companion object {
        const val HOME_SHOW_LIMIT = 30
    }
}
