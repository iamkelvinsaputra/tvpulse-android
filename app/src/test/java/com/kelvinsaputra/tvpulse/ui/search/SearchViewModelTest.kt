package com.kelvinsaputra.tvpulse.ui.search

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
        val viewModel = SearchViewModel(SearchShowsUseCase(repository))

        viewModel.onQueryChange("   ")
        runCurrent()

        assertEquals(0, callCount)
        assertTrue(viewModel.uiState.value is SearchUiState.Empty)
    }

    @Test
    fun `search exposes error state when request fails`() = runTest {
        val repository = FakeTvShowRepository().apply {
            search = { throw IllegalStateException("boom") }
        }
        val viewModel = SearchViewModel(SearchShowsUseCase(repository))

        viewModel.onQueryChange("broken")
        advanceTimeBy(350)
        runCurrent()

        assertTrue(viewModel.uiState.value is SearchUiState.Error)
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
        val viewModel = SearchViewModel(SearchShowsUseCase(repository))

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
        val state = viewModel.uiState.value as SearchUiState.Success
        assertEquals("new", state.query)
    }
}
