package com.kelvinsaputra.tvpulse.ui.favorites

import com.kelvinsaputra.tvpulse.domain.usecase.ObserveFavoritesUseCase
import com.kelvinsaputra.tvpulse.testutil.FakeTvShowRepository
import com.kelvinsaputra.tvpulse.testutil.MainDispatcherRule
import com.kelvinsaputra.tvpulse.testutil.sampleShow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `favorites react to repository flow updates`() = runTest {
        val repository = FakeTvShowRepository()
        val viewModel = FavoritesViewModel(ObserveFavoritesUseCase(repository))
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        runCurrent()
        assertEquals(FavoritesUiState.Empty, viewModel.uiState.value)

        repository.favorites.value = listOf(sampleShow(id = 9))
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state is FavoritesUiState.Success)
        assertEquals(9L, (state as FavoritesUiState.Success).shows.single().id)

        collectJob.cancel()
    }
}
