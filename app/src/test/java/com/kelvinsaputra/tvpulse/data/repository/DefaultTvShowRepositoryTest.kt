package com.kelvinsaputra.tvpulse.data.repository

import com.kelvinsaputra.tvpulse.data.local.dao.CacheMetadataDao
import com.kelvinsaputra.tvpulse.data.local.dao.CachedShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.FavoriteShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.HomeShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.SearchResultDao
import com.kelvinsaputra.tvpulse.data.local.entity.CacheMetadataEntity
import com.kelvinsaputra.tvpulse.data.local.entity.CachedShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.FavoriteShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.HomeShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.SearchResultEntity
import com.kelvinsaputra.tvpulse.data.remote.api.TvMazeApi
import com.kelvinsaputra.tvpulse.data.remote.dto.SearchShowDto
import com.kelvinsaputra.tvpulse.data.remote.dto.TvShowDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultTvShowRepositoryTest {

    @Test
    fun `home maps first remote page`() = runTest {
        val api = FakeApi(
            shows = (1L..50L).map { TvShowDto(id = it, name = "Show $it") },
        )
        val repository = createRepository(api)

        val result = repository.getTopShows()

        assertEquals(50, result.size)
        assertEquals(1L, result.first().id)
        assertEquals(50L, result.last().id)
    }

    @Test
    fun `search unwraps TVmaze search envelopes`() = runTest {
        val api = FakeApi(
            searchResults = listOf(
                SearchShowDto(show = TvShowDto(id = 7, name = "Search Hit")),
            ),
        )
        val repository = createRepository(api)

        val result = repository.searchShows("hit")

        assertEquals(listOf(7L), result.map { it.id })
    }

    @Test
    fun `search is capped at ten results`() = runTest {
        val api = FakeApi(
            searchResults = (1L..15L).map { id ->
                SearchShowDto(show = TvShowDto(id = id, name = "Show $id"))
            },
        )
        val repository = createRepository(api)

        val result = repository.searchShows("show")

        assertEquals(10, result.size)
        assertEquals(10L, result.last().id)
    }

    @Test
    fun `detail maps requested show`() = runTest {
        val api = FakeApi(detail = TvShowDto(id = 99, name = "Detail"))
        val repository = createRepository(api)

        val result = repository.getShowDetail(99)

        assertEquals(99L, result.id)
        assertEquals("Detail", result.name)
    }

    private fun createRepository(api: TvMazeApi) = DefaultTvShowRepository(
        api = api,
        cacheMetadataDao = FakeCacheMetadataDao(),
        cachedShowDao = FakeCachedShowDao(),
        homeShowDao = FakeHomeShowDao(),
        searchResultDao = FakeSearchResultDao(),
        favoriteShowDao = FakeFavoriteShowDao(),
    )
}

private class FakeApi(
    private val shows: List<TvShowDto> = emptyList(),
    private val searchResults: List<SearchShowDto> = emptyList(),
    private val detail: TvShowDto = TvShowDto(id = 1, name = "Detail"),
) : TvMazeApi {
    override suspend fun getShows(page: Int): List<TvShowDto> = shows
    override suspend fun searchShows(query: String): List<SearchShowDto> = searchResults
    override suspend fun getShow(showId: Long): TvShowDto = detail
}

private class FakeCacheMetadataDao : CacheMetadataDao {
    override suspend fun get(key: String): CacheMetadataEntity? = null
    override suspend fun upsert(metadata: CacheMetadataEntity) = Unit
}

private class FakeCachedShowDao : CachedShowDao {
    override fun observeById(showId: Long): Flow<CachedShowEntity?> = flowOf(null)
    override suspend fun getById(showId: Long): CachedShowEntity? = null
    override suspend fun upsert(show: CachedShowEntity) = Unit
    override suspend fun upsertAll(shows: List<CachedShowEntity>) = Unit
}

private class FakeHomeShowDao : HomeShowDao {
    override fun observeShows(limit: Int): Flow<List<CachedShowEntity>> = flowOf(emptyList())
    override suspend fun count(): Int = 0
    override suspend fun maxRemotePage(): Int? = null
    override suspend fun maxPosition(): Int? = null
    override suspend fun upsertCachedShows(shows: List<CachedShowEntity>) = Unit
    override suspend fun insertAll(entries: List<HomeShowEntity>) = Unit
    override suspend fun clear() = Unit
}

private class FakeSearchResultDao : SearchResultDao {
    override fun observeShows(query: String, limit: Int): Flow<List<CachedShowEntity>> =
        flowOf(emptyList())

    override suspend fun upsertCachedShows(shows: List<CachedShowEntity>) = Unit
    override suspend fun insertAll(entries: List<SearchResultEntity>) = Unit
    override suspend fun clearQuery(query: String) = Unit
}

private class FakeFavoriteShowDao : FavoriteShowDao {
    override fun observeAll(limit: Int): Flow<List<FavoriteShowEntity>> = flowOf(emptyList())
    override suspend fun getAll(): List<FavoriteShowEntity> = emptyList()
    override suspend fun count(): Int = 0
    override fun observeById(showId: Long): Flow<FavoriteShowEntity?> = flowOf(null)
    override fun observeIsFavorite(showId: Long): Flow<Boolean> = flowOf(false)
    override suspend fun isFavorite(showId: Long): Boolean = false
    override suspend fun upsert(show: FavoriteShowEntity) = Unit
    override suspend fun upsertAll(shows: List<FavoriteShowEntity>) = Unit
    override suspend fun deleteById(showId: Long) = Unit
}
