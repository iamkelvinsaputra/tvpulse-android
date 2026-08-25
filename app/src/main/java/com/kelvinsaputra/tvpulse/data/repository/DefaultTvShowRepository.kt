package com.kelvinsaputra.tvpulse.data.repository

import com.kelvinsaputra.tvpulse.data.mapper.toDomain
import com.kelvinsaputra.tvpulse.data.remote.api.TvMazeApi
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class DefaultTvShowRepository @Inject constructor(
    private val api: TvMazeApi,
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

    private companion object {
        const val HOME_SHOW_LIMIT = 30
    }
}
