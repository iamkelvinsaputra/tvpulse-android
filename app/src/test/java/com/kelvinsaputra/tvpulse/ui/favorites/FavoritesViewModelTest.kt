package com.kelvinsaputra.tvpulse.ui.favorites

import com.kelvinsaputra.tvpulse.domain.usecase.CanLoadMoreFavoritesUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveFavoritesUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.RefreshFavoritesUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.SetFavoriteUseCase
import com.kelvinsaputra.tvpulse.testutil.FakeTvShowRepository
import com.kelvinsaputra.tvpulse.testutil.MainDispatcherRule
import com.kelvinsaputra.tvpulse.testutil.sampleShow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `favorites react to room backed repository flow`() = runTest {
        val repository = FakeTvShowRepository()
        val viewModel = FavoritesViewModel(
            observeFavoritesUseCase = ObserveFavoritesUseCase(repository),
            canLoadMoreFavoritesUseCase = CanLoadMoreFavoritesUseCase(repository),
            refreshFavoritesUseCase = RefreshFavoritesUseCase(repository),
            setFavoriteUseCase = SetFavoriteUseCase(repository),
        )

        runCurrent()
        assertEquals(emptyList<Any>(), viewModel.uiState.value.shows)

        repository.favorites.value = listOf(sampleShow(id = 9))
        runCurrent()

        assertEquals(9L, viewModel.uiState.value.shows.single().id)
    }
    @Test
    fun `favorites keep cached data visible while metadata refreshes`() = runTest {
        val repository = FakeTvShowRepository().apply {
            favorites.value = listOf(sampleShow(id = 9, name = "Old Name"))
            refreshFavorites = { showIds ->
                assertEquals(listOf(9L), showIds)
                favorites.value = listOf(sampleShow(id = 9, name = "New Name"))
            }
        }
        val viewModel = FavoritesViewModel(
            observeFavoritesUseCase = ObserveFavoritesUseCase(repository),
            canLoadMoreFavoritesUseCase = CanLoadMoreFavoritesUseCase(repository),
            refreshFavoritesUseCase = RefreshFavoritesUseCase(repository),
            setFavoriteUseCase = SetFavoriteUseCase(repository),
        )

        runCurrent()

        assertEquals("New Name", viewModel.uiState.value.shows.single().name)
    }

    @Test
    fun `loading the next favorites page syncs only the newly revealed page`() = runTest {
        val syncedPages = mutableListOf<List<Long>>()
        val repository = FakeTvShowRepository().apply {
            favorites.value = (1L..15L).map { id -> sampleShow(id = id) }
            refreshFavorites = { showIds ->
                syncedPages += showIds
            }
        }
        val viewModel = FavoritesViewModel(
            observeFavoritesUseCase = ObserveFavoritesUseCase(repository),
            canLoadMoreFavoritesUseCase = CanLoadMoreFavoritesUseCase(repository),
            refreshFavoritesUseCase = RefreshFavoritesUseCase(repository),
            setFavoriteUseCase = SetFavoriteUseCase(repository),
        )

        runCurrent()
        assertEquals((1L..10L).toList(), syncedPages.single())

        viewModel.loadMore()
        runCurrent()

        assertEquals((11L..15L).toList(), syncedPages.last())
        assertEquals(15, viewModel.uiState.value.shows.size)
    }

}
