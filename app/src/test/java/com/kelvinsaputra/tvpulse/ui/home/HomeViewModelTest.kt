package com.kelvinsaputra.tvpulse.ui.home

import com.kelvinsaputra.tvpulse.domain.usecase.GetTopShowsUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.HasHomeCacheUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.LoadMoreHomeShowsUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveHomeShowsUseCase
import com.kelvinsaputra.tvpulse.testutil.FakeTvShowRepository
import com.kelvinsaputra.tvpulse.testutil.MainDispatcherRule
import com.kelvinsaputra.tvpulse.testutil.sampleShow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `home syncs server result into cached ui`() = runTest {
        val repository = FakeTvShowRepository().apply {
            topShows = listOf(sampleShow())
        }
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.shows.size)
        assertFalse(viewModel.uiState.value.isInitialLoading)
    }

    @Test
    fun `home shows blocking error only when no cache exists`() = runTest {
        val repository = FakeTvShowRepository().apply {
            topShowsError = IllegalStateException("boom")
        }
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.blockingError)
        assertEquals(emptyList<Any>(), viewModel.uiState.value.shows)
    }

    private fun createViewModel(repository: FakeTvShowRepository) = HomeViewModel(
        observeHomeShowsUseCase = ObserveHomeShowsUseCase(repository),
        hasHomeCacheUseCase = HasHomeCacheUseCase(repository),
        getTopShowsUseCase = GetTopShowsUseCase(repository),
        loadMoreHomeShowsUseCase = LoadMoreHomeShowsUseCase(repository),
    )
}
