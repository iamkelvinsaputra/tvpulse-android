package com.kelvinsaputra.tvpulse.data.repository

import com.kelvinsaputra.tvpulse.data.local.dao.FavoriteShowDao
import com.kelvinsaputra.tvpulse.data.local.entity.FavoriteShowEntity
import com.kelvinsaputra.tvpulse.data.remote.api.TvMazeApi
import com.kelvinsaputra.tvpulse.data.remote.dto.SearchShowDto
import com.kelvinsaputra.tvpulse.data.remote.dto.TvShowDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultTvShowRepositoryTest {

    @Test
    fun `getTopShows returns only first 30 items`() = runTest {
        val api = FakeApi(
            shows = (1L..50L).map { TvShowDto(id = it, name = "Show $it") },
        )
        val repository = DefaultTvShowRepository(api, FakeDao())

        val result = repository.getTopShows()

        assertEquals(30, result.size)
        assertEquals(1L, result.first().id)
        assertEquals(30L, result.last().id)
    }

    @Test
    fun `search unwraps TVmaze search envelopes`() = runTest {
        val api = FakeApi(
            searchResults = listOf(
                SearchShowDto(show = TvShowDto(id = 7, name = "Search Hit")),
            ),
        )
        val repository = DefaultTvShowRepository(api, FakeDao())

        val result = repository.searchShows("hit")

        assertEquals(listOf(7L), result.map { it.id })
    }

    @Test
    fun `detail maps requested show`() = runTest {
        val api = FakeApi(detail = TvShowDto(id = 99, name = "Detail"))
        val repository = DefaultTvShowRepository(api, FakeDao())

        val result = repository.getShowDetail(99)

        assertEquals(99L, result.id)
        assertEquals("Detail", result.name)
    }
}

private class FakeApi(
    private val shows: List<TvShowDto> = emptyList(),
    private val searchResults: List<SearchShowDto> = emptyList(),
    private val detail: TvShowDto = TvShowDto(id = 1, name = "Detail"),
) : TvMazeApi {
    override suspend fun getShows(): List<TvShowDto> = shows
    override suspend fun searchShows(query: String): List<SearchShowDto> = searchResults
    override suspend fun getShow(showId: Long): TvShowDto = detail
}

private class FakeDao : FavoriteShowDao {
    private val favorites = MutableStateFlow<List<FavoriteShowEntity>>(emptyList())

    override fun observeAll(): Flow<List<FavoriteShowEntity>> = favorites

    override fun observeIsFavorite(showId: Long): Flow<Boolean> =
        favorites.map { list -> list.any { it.id == showId } }

    override suspend fun upsert(show: FavoriteShowEntity) {
        favorites.value = favorites.value.filterNot { it.id == show.id } + show
    }

    override suspend fun deleteById(showId: Long) {
        favorites.value = favorites.value.filterNot { it.id == showId }
    }
}
