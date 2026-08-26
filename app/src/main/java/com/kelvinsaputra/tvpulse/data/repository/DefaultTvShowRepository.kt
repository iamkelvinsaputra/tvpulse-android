package com.kelvinsaputra.tvpulse.data.repository

import com.kelvinsaputra.tvpulse.data.local.dao.CacheMetadataDao
import com.kelvinsaputra.tvpulse.data.local.dao.CachedShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.FavoriteShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.HomeShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.SearchResultDao
import com.kelvinsaputra.tvpulse.data.local.entity.CacheMetadataEntity
import com.kelvinsaputra.tvpulse.data.local.entity.HomeShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.SearchResultEntity
import com.kelvinsaputra.tvpulse.data.mapper.toCachedEntity
import com.kelvinsaputra.tvpulse.data.mapper.toDomain
import com.kelvinsaputra.tvpulse.data.mapper.toFavoriteEntity
import com.kelvinsaputra.tvpulse.data.remote.api.TvMazeApi
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject

class DefaultTvShowRepository @Inject constructor(
    private val api: TvMazeApi,
    private val cacheMetadataDao: CacheMetadataDao,
    private val cachedShowDao: CachedShowDao,
    private val homeShowDao: HomeShowDao,
    private val searchResultDao: SearchResultDao,
    private val favoriteShowDao: FavoriteShowDao,
) : TvShowRepository {

    override fun observeHomeShows(limit: Int): Flow<List<TvShow>> =
        homeShowDao.observeShows(limit)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun hasHomeCache(): Boolean =
        cacheMetadataDao.get(HOME_CACHE_KEY) != null

    override suspend fun getTopShows(): List<TvShow> {
        val shows = api.getShows(page = 0).map { it.toDomain() }
        homeShowDao.replaceAll(
            shows = shows.map { it.toCachedEntity() },
            entries = shows.mapIndexed { index, show ->
                HomeShowEntity(
                    showId = show.id,
                    position = index,
                    remotePage = 0,
                )
            },
        )
        cacheMetadataDao.upsert(
            CacheMetadataEntity(
                key = HOME_CACHE_KEY,
                lastSyncedAtEpochMillis = System.currentTimeMillis(),
                resultCount = shows.size,
            )
        )
        return shows
    }

    override suspend fun loadMoreHomeShows(visibleCount: Int): Boolean {
        if (homeShowDao.count() > visibleCount) return true

        val nextRemotePage = (homeShowDao.maxRemotePage() ?: -1) + 1
        val shows = try {
            api.getShows(page = nextRemotePage).map { it.toDomain() }
        } catch (exception: HttpException) {
            if (exception.code() == 404) return false
            throw exception
        }
        if (shows.isEmpty()) return false

        val firstPosition = (homeShowDao.maxPosition() ?: -1) + 1
        homeShowDao.append(
            shows = shows.map { it.toCachedEntity() },
            entries = shows.mapIndexed { index, show ->
                HomeShowEntity(
                    showId = show.id,
                    position = firstPosition + index,
                    remotePage = nextRemotePage,
                )
            },
        )
        return true
    }

    override fun observeSearchShows(query: String, limit: Int): Flow<List<TvShow>> =
        searchResultDao.observeShows(normalizeQuery(query), limit)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun hasSearchCache(query: String): Boolean =
        cacheMetadataDao.get(searchCacheKey(query)) != null

    override suspend fun searchShows(query: String): List<TvShow> {
        val cacheKey = normalizeQuery(query)
        val shows = api.searchShows(query)
            .map { it.show.toDomain() }

        searchResultDao.replaceResults(
            query = cacheKey,
            shows = shows.map { it.toCachedEntity() },
            entries = shows.mapIndexed { index, show ->
                SearchResultEntity(
                    query = cacheKey,
                    showId = show.id,
                    position = index,
                )
            },
        )
        cacheMetadataDao.upsert(
            CacheMetadataEntity(
                key = searchCacheKey(query),
                lastSyncedAtEpochMillis = System.currentTimeMillis(),
                resultCount = shows.size,
            )
        )
        return shows
    }

    override suspend fun canLoadMoreSearchShows(
        query: String,
        visibleCount: Int,
    ): Boolean = searchResultDao.count(normalizeQuery(query)) > visibleCount

    override fun observeShowDetail(showId: Long): Flow<TvShow?> =
        combine(
            cachedShowDao.observeById(showId),
            favoriteShowDao.observeById(showId),
        ) { cached, favorite ->
            cached?.toDomain() ?: favorite?.toDomain()
        }

    override suspend fun getShowDetail(showId: Long): TvShow {
        val show = api.getShow(showId).toDomain()
        cachedShowDao.upsert(show.toCachedEntity())
        if (favoriteShowDao.isFavorite(showId)) {
            favoriteShowDao.upsert(show.toFavoriteEntity())
        }
        return show
    }

    override fun observeFavorites(limit: Int): Flow<List<TvShow>> =
        favoriteShowDao.observeAll(limit)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun canLoadMoreFavorites(visibleCount: Int): Boolean =
        favoriteShowDao.count() > visibleCount

    override suspend fun refreshFavorites() {
        val favorites = favoriteShowDao.getAll()
        if (favorites.isEmpty()) return

        for (favorite in favorites) {
            val show = api.getShow(favorite.id).toDomain()
            cachedShowDao.upsert(show.toCachedEntity())
            favoriteShowDao.upsert(show.toFavoriteEntity())
        }
    }

    override fun observeIsFavorite(showId: Long): Flow<Boolean> =
        favoriteShowDao.observeIsFavorite(showId)

    override suspend fun setFavorite(show: TvShow, isFavorite: Boolean) {
        if (isFavorite) {
            cachedShowDao.upsert(show.toCachedEntity())
            favoriteShowDao.upsert(show.toFavoriteEntity())
        } else {
            favoriteShowDao.deleteById(show.id)
        }
    }

    private fun normalizeQuery(query: String): String =
        query.trim().lowercase()

    private fun searchCacheKey(query: String): String =
        SEARCH_CACHE_PREFIX + normalizeQuery(query)

    private companion object {
        const val HOME_CACHE_KEY = "home"
        const val SEARCH_CACHE_PREFIX = "search:"
    }
}
