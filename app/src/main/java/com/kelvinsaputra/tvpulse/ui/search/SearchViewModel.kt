package com.kelvinsaputra.tvpulse.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.usecase.CanLoadMoreSearchShowsUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.HasSearchCacheUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveSearchShowsUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.SearchShowsUseCase
import com.kelvinsaputra.tvpulse.ui.components.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val observeSearchShowsUseCase: ObserveSearchShowsUseCase,
    private val hasSearchCacheUseCase: HasSearchCacheUseCase,
    private val searchShowsUseCase: SearchShowsUseCase,
    private val canLoadMoreSearchShowsUseCase: CanLoadMoreSearchShowsUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val retryToken = MutableStateFlow(0L)
    private val visibleLimit = MutableStateFlow(PAGE_SIZE)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val normalizedQuery = _query
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce { query -> if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MS }

    init {
        viewModelScope.launch {
            combine(
                normalizedQuery,
                retryToken,
            ) { query, _ -> query }
                .collectLatest(::observeAndSyncQuery)
        }
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun retry() {
        retryToken.value += 1
    }

    fun loadMore() {
        val state = _uiState.value
        if (
            state.query.isBlank() ||
            state.shows.isEmpty() ||
            state.isLoadingMore ||
            !state.canLoadMore
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val currentLimit = visibleLimit.value
            val hasMore = canLoadMoreSearchShowsUseCase(state.query, currentLimit)
            if (hasMore) {
                visibleLimit.value = currentLimit + PAGE_SIZE
            }
            val canLoadMoreAfter = canLoadMoreSearchShowsUseCase(
                state.query,
                visibleLimit.value,
            )
            _uiState.update {
                it.copy(
                    isLoadingMore = false,
                    canLoadMore = canLoadMoreAfter,
                )
            }
        }
    }

    private suspend fun observeAndSyncQuery(query: String) {
        if (query.isBlank()) {
            visibleLimit.value = PAGE_SIZE
            _uiState.value = SearchUiState()
            return
        }

        visibleLimit.value = PAGE_SIZE

        coroutineScope {
            val cachedShows = observeSearchShowsUseCase(query, PAGE_SIZE).first()
            val hasKnownCache = hasSearchCacheUseCase(query)
            val cachedCanLoadMore = canLoadMoreSearchShowsUseCase(query, PAGE_SIZE)
            _uiState.value = SearchUiState(
                query = query,
                shows = cachedShows,
                hasSearched = true,
                isInitialLoading = !hasKnownCache,
                isSyncing = hasKnownCache,
                canLoadMore = cachedCanLoadMore,
            )

            val observer = launch {
                visibleLimit
                    .flatMapLatest { limit ->
                        observeSearchShowsUseCase(query, limit)
                    }
                    .collect { shows ->
                        _uiState.update { state ->
                            if (state.query != query) {
                                state
                            } else {
                                state.copy(
                                    shows = shows,
                                    isInitialLoading = if (shows.isNotEmpty()) {
                                        false
                                    } else {
                                        state.isInitialLoading
                                    },
                                )
                            }
                        }
                    }
            }

            try {
                val remoteShows = searchShowsUseCase(query)
                _uiState.update { state ->
                    state.copy(
                        isInitialLoading = false,
                        isSyncing = false,
                        canLoadMore = remoteShows.size > visibleLimit.value,
                        blockingError = null,
                        syncError = null,
                    )
                }
            } catch (exception: CancellationException) {
                observer.cancel()
                throw exception
            } catch (exception: Exception) {
                val error = exception.toUiError()
                _uiState.update { state ->
                    val hasCache = hasKnownCache || state.shows.isNotEmpty() || cachedShows.isNotEmpty()
                    state.copy(
                        isInitialLoading = false,
                        isSyncing = false,
                        blockingError = if (hasCache) null else error,
                        syncError = if (hasCache) error else null,
                    )
                }
            }

            observer.join()
        }
    }

    private companion object {
        const val PAGE_SIZE = 5
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
