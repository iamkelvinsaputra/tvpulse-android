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
}
