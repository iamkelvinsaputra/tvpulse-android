package com.kelvinsaputra.tvpulse.testutil

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTvShowRepository : TvShowRepository {
    var topShows: List<TvShow> = emptyList()
    var topShowsError: Throwable? = null
    var search: suspend (String) -> List<TvShow> = { emptyList() }
    var detail: suspend (Long) -> TvShow = { sampleShow(it) }

    val favorites = MutableStateFlow<List<TvShow>>(emptyList())
    private val favoriteIds = MutableStateFlow<Set<Long>>(emptySet())

    override suspend fun getTopShows(): List<TvShow> {
        topShowsError?.let { throw it }
        return topShows
    }

    override suspend fun searchShows(query: String): List<TvShow> = search(query)

    override suspend fun getShowDetail(showId: Long): TvShow = detail(showId)

    override fun observeFavorites(): Flow<List<TvShow>> = favorites

    override fun observeIsFavorite(showId: Long): Flow<Boolean> =
        favoriteIds.map { showId in it }

    override suspend fun setFavorite(show: TvShow, isFavorite: Boolean) {
        favoriteIds.value = if (isFavorite) {
            favoriteIds.value + show.id
        } else {
            favoriteIds.value - show.id
        }
        favorites.value = if (isFavorite) {
            (favorites.value.filterNot { it.id == show.id } + show)
        } else {
            favorites.value.filterNot { it.id == show.id }
        }
    }
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
