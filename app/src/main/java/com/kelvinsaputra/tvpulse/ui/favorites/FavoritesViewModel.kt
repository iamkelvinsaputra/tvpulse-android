package com.kelvinsaputra.tvpulse.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.usecase.CanLoadMoreFavoritesUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveFavoritesUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.RefreshFavoritesUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.SetFavoriteUseCase
import com.kelvinsaputra.tvpulse.ui.components.UiError
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
class FavoritesViewModel @Inject constructor(
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val canLoadMoreFavoritesUseCase: CanLoadMoreFavoritesUseCase,
    private val refreshFavoritesUseCase: RefreshFavoritesUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase,
) : ViewModel() {

    private val visibleLimit = MutableStateFlow(PAGE_SIZE)
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _actionError = MutableStateFlow<UiError?>(null)
    val actionError: StateFlow<UiError?> = _actionError.asStateFlow()

    private var failedRemoval: TvShow? = null

    init {
        observeFavorites()
        refresh()
    }

    fun refresh() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            val currentLimit = visibleLimit.value
            val cachedShows = observeFavoritesUseCase(currentLimit).first()
            val canLoadMore = canLoadMoreFavoritesUseCase(currentLimit)

            _uiState.update { state ->
                state.copy(
                    shows = cachedShows.ifEmpty { state.shows },
                    isInitialLoading = false,
                    isSyncing = cachedShows.isNotEmpty(),
                    canLoadMore = canLoadMore,
                    syncError = null,
                )
            }

            // Favorite membership is local. If Room has no favorites, there is
            // nothing to refresh from TVmaze.
            if (cachedShows.isEmpty()) return@launch

            syncFavoritePage(
                showIds = cachedShows.map(TvShow::id),
            )
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (
            state.shows.isEmpty() ||
            state.isLoadingMore ||
            state.isSyncing ||
            !state.canLoadMore
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val currentLimit = visibleLimit.value
            if (!canLoadMoreFavoritesUseCase(currentLimit)) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        canLoadMore = false,
                    )
                }
                return@launch
            }

            val nextLimit = currentLimit + PAGE_SIZE
            visibleLimit.value = nextLimit

            // Reveal the next cached Room page immediately, then sync only the
            // newly revealed favorites in the background.
            val visibleShows = observeFavoritesUseCase(nextLimit).first()
            val newPage = visibleShows
                .drop(currentLimit)
                .take(PAGE_SIZE)

            val canLoadMoreAfter = canLoadMoreFavoritesUseCase(nextLimit)
            _uiState.update {
                it.copy(
                    shows = visibleShows,
                    isLoadingMore = false,
                    isSyncing = newPage.isNotEmpty(),
                    canLoadMore = canLoadMoreAfter,
                    syncError = null,
                )
            }

            if (newPage.isNotEmpty()) {
                syncFavoritePage(
                    showIds = newPage.map(TvShow::id),
                )
            }
        }
    }

    fun removeFavorite(show: TvShow) {
        _actionError.value = null

        viewModelScope.launch {
            try {
                setFavoriteUseCase(
                    show = show,
                    isFavorite = false,
                )
                failedRemoval = null
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                failedRemoval = show
                _actionError.value = exception.toUiError()
            }
        }
    }

    fun retryRemoval() {
        failedRemoval?.let(::removeFavorite)
    }

    fun dismissActionError() {
        failedRemoval = null
        _actionError.value = null
    }

    private suspend fun syncFavoritePage(showIds: List<Long>) {
        try {
            refreshFavoritesUseCase(showIds)
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncError = null,
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    syncError = exception.toUiError(),
                )
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            visibleLimit
                .flatMapLatest(observeFavoritesUseCase::invoke)
                .collect { shows ->
                    val canLoadMore = canLoadMoreFavoritesUseCase(visibleLimit.value)
                    _uiState.update { state ->
                        state.copy(
                            shows = shows,
                            isInitialLoading = false,
                            canLoadMore = canLoadMore,
                        )
                    }
                }
        }
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}
