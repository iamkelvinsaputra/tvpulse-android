package com.kelvinsaputra.tvpulse.ui.search

import com.kelvinsaputra.tvpulse.domain.usecase.HasSearchCacheUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveSearchShowsUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.SearchShowsUseCase
import com.kelvinsaputra.tvpulse.testutil.FakeTvShowRepository
import com.kelvinsaputra.tvpulse.testutil.MainDispatcherRule
import com.kelvinsaputra.tvpulse.testutil.sampleShow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `blank query does not call repository`() = runTest {
        var callCount = 0
        val repository = FakeTvShowRepository().apply {
            search = {
                callCount += 1
                emptyList()
            }
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChange("   ")
        runCurrent()

        assertEquals(0, callCount)
        assertFalse(viewModel.uiState.value.hasSearched)
    }

    @Test
    fun `search failure without cache becomes blocking error`() = runTest {
        val repository = FakeTvShowRepository().apply {
            search = { throw IllegalStateException("boom") }
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChange("broken")
        advanceTimeBy(350)
        runCurrent()

        assertNotNull(viewModel.uiState.value.blockingError)
        assertTrue(viewModel.uiState.value.shows.isEmpty())
    }

    @Test
    fun `search exposes at most ten results`() = runTest {
        val repository = FakeTvShowRepository().apply {
            search = {
                (1L..15L).map { id -> sampleShow(id = id, name = "Show $id") }
            }
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChange("show")
        advanceTimeBy(350)
        runCurrent()

        assertEquals(10, viewModel.uiState.value.shows.size)
    }

    @Test
    fun `new query cancels stale search`() = runTest {
        val completed = mutableListOf<String>()
        val repository = FakeTvShowRepository().apply {
            search = { query ->
                delay(if (query == "old") 1_000 else 10)
                completed += query
                listOf(sampleShow(name = query))
            }
        }
        val viewModel = createViewModel(repository)

        viewModel.onQueryChange("old")
        advanceTimeBy(350)
        runCurrent()

        viewModel.onQueryChange("new")
        advanceTimeBy(350)
        runCurrent()
        advanceTimeBy(10)
        runCurrent()

        assertFalse("old" in completed)
        assertTrue("new" in completed)
        assertEquals("new", viewModel.uiState.value.query)
        assertEquals("new", viewModel.uiState.value.shows.single().name)
    }

    private fun createViewModel(repository: FakeTvShowRepository) = SearchViewModel(
        observeSearchShowsUseCase = ObserveSearchShowsUseCase(repository),
        hasSearchCacheUseCase = HasSearchCacheUseCase(repository),
        searchShowsUseCase = SearchShowsUseCase(repository),
    )
}
