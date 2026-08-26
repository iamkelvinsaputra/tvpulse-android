package com.kelvinsaputra.tvpulse.ui.home

import com.kelvinsaputra.tvpulse.domain.usecase.GetTopShowsUseCase
import com.kelvinsaputra.tvpulse.testutil.FakeTvShowRepository
import com.kelvinsaputra.tvpulse.testutil.MainDispatcherRule
import com.kelvinsaputra.tvpulse.testutil.sampleShow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `home transitions from loading to success`() = runTest {
        val repository = FakeTvShowRepository().apply {
            topShows = listOf(sampleShow())
        }
        val viewModel = HomeViewModel(GetTopShowsUseCase(repository))

        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is HomeUiState.Success)
        assertEquals(1, (state as HomeUiState.Success).shows.size)
    }

    @Test
    fun `home exposes empty state for empty repository result`() = runTest {
        val viewModel = HomeViewModel(GetTopShowsUseCase(FakeTvShowRepository()))

        advanceUntilIdle()

        assertEquals(HomeUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `home exposes error state when repository fails`() = runTest {
        val repository = FakeTvShowRepository().apply {
            topShowsError = IllegalStateException("boom")
        }
        val viewModel = HomeViewModel(GetTopShowsUseCase(repository))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }
}
