package com.kelvinsaputra.tvpulse.testutil

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FakeTvShowRepository : TvShowRepository {
    var topShows: List<TvShow> = emptyList()
    var topShowsError: Throwable? = null
    var search: suspend (String) -> List<TvShow> = { emptyList() }
    var detail: suspend (Long) -> TvShow = { sampleShow(it) }

    val homeCache = MutableStateFlow<List<TvShow>>(emptyList())
    var homeCacheKnown: Boolean = false
    private val searchCaches = mutableMapOf<String, MutableStateFlow<List<TvShow>>>()
    private val knownSearchCaches = mutableSetOf<String>()
    private val detailCache = MutableStateFlow<Map<Long, TvShow>>(emptyMap())

    val favorites = MutableStateFlow<List<TvShow>>(emptyList())
    private val favoriteIds = MutableStateFlow<Set<Long>>(emptySet())

    override fun observeHomeShows(limit: Int): Flow<List<TvShow>> =
        homeCache.map { it.take(limit) }

    override suspend fun hasHomeCache(): Boolean = homeCacheKnown

    override suspend fun getTopShows(): List<TvShow> {
        topShowsError?.let { throw it }
        homeCache.value = topShows
        homeCacheKnown = true
        return topShows
    }

    override suspend fun loadMoreHomeShows(targetCount: Int): Boolean =
        homeCache.value.size >= targetCount

    override fun observeSearchShows(query: String, limit: Int): Flow<List<TvShow>> =
        searchCache(query).map { it.take(limit) }

    override suspend fun hasSearchCache(query: String): Boolean =
        normalizeQuery(query) in knownSearchCaches

    override suspend fun searchShows(query: String): List<TvShow> {
        val result = search(query)
        searchCache(query).value = result
        knownSearchCaches += normalizeQuery(query)
        return result
    }

    override fun observeShowDetail(showId: Long): Flow<TvShow?> =
        combine(
            detailCache,
            favorites,
        ) { cache, favoriteShows ->
            cache[showId] ?: favoriteShows.firstOrNull { it.id == showId }
        }

    override suspend fun getShowDetail(showId: Long): TvShow {
        val show = detail(showId)
        detailCache.value = detailCache.value + (showId to show)
        return show
    }

    override fun observeFavorites(limit: Int): Flow<List<TvShow>> =
        favorites.map { it.take(limit) }

    var refreshFavorites: suspend (List<Long>) -> Unit = {}

    override suspend fun canLoadMoreFavorites(visibleCount: Int): Boolean =
        favorites.value.size > visibleCount

    override suspend fun refreshFavorites(showIds: List<Long>) {
        refreshFavorites.invoke(showIds)
    }

    override fun observeIsFavorite(showId: Long): Flow<Boolean> =
        favoriteIds.map { showId in it }

    override suspend fun setFavorite(show: TvShow, isFavorite: Boolean) {
        favoriteIds.value = if (isFavorite) {
            favoriteIds.value + show.id
        } else {
            favoriteIds.value - show.id
        }
        favorites.value = if (isFavorite) {
            favorites.value.filterNot { it.id == show.id } + show
        } else {
            favorites.value.filterNot { it.id == show.id }
        }
    }

    fun setSearchCache(query: String, shows: List<TvShow>) {
        searchCache(query).value = shows
        knownSearchCaches += normalizeQuery(query)
    }

    fun setDetailCache(show: TvShow) {
        detailCache.value = detailCache.value + (show.id to show)
    }

    private fun searchCache(query: String): MutableStateFlow<List<TvShow>> =
        searchCaches.getOrPut(normalizeQuery(query)) {
            MutableStateFlow(emptyList())
        }

    private fun normalizeQuery(query: String): String = query.trim().lowercase()
}

fun sampleShow(
    id: Long = 1L,
    name: String = "Sample Show",
) = TvShow(
    id = id,
    name = name,
    imageUrl = "https://example.com/$id.jpg",
    summaryHtml = "<p>Summary</p>",
    rating = 8.0,
    genres = listOf("Drama"),
    schedule = "Friday · 20:00",
    network = "Sample Network",
    premiered = "2026-01-01",
    language = "English",
)
