package com.kelvinsaputra.tvpulse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.usecase.GetTopShowsUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.HasHomeCacheUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.LoadMoreHomeShowsUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveHomeShowsUseCase
import com.kelvinsaputra.tvpulse.ui.components.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeHomeShowsUseCase: ObserveHomeShowsUseCase,
    private val hasHomeCacheUseCase: HasHomeCacheUseCase,
    private val getTopShowsUseCase: GetTopShowsUseCase,
    private val loadMoreHomeShowsUseCase: LoadMoreHomeShowsUseCase,
) : ViewModel() {

    private val visibleLimit = MutableStateFlow(PAGE_SIZE)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeCachedShows()
        refresh()
    }

    fun retry() = refresh()

    fun loadMore() {
        val state = _uiState.value
        if (
            state.shows.isEmpty() ||
            state.isLoadingMore ||
            !state.canLoadMore
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, syncError = null) }

            try {
                val hasMore = loadMoreHomeShowsUseCase(visibleLimit.value)
                if (hasMore) {
                    visibleLimit.value += PAGE_SIZE
                }
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        canLoadMore = hasMore,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        syncError = exception.toUiError(),
                    )
                }
            }
        }
    }

    private fun observeCachedShows() {
        viewModelScope.launch {
            visibleLimit
                .flatMapLatest(observeHomeShowsUseCase::invoke)
                .collect { shows ->
                    _uiState.update { state ->
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

    private fun refresh() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            val cachedShows = observeHomeShowsUseCase(PAGE_SIZE).first()
            val hasKnownCache = hasHomeCacheUseCase()
            _uiState.update {
                it.copy(
                    shows = cachedShows.ifEmpty { it.shows },
                    isInitialLoading = !hasKnownCache,
                    isSyncing = hasKnownCache,
                    blockingError = null,
                    syncError = null,
                )
            }

            try {
                val refreshedShows = getTopShowsUseCase()
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isSyncing = false,
                        canLoadMore = refreshedShows.isNotEmpty(),
                        blockingError = null,
                        syncError = null,
                    )
                }
            } catch (exception: CancellationException) {
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
        }
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}
