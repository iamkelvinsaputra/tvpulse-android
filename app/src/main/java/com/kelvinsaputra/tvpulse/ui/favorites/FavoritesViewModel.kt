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
            val cachedShows = observeFavoritesUseCase(PAGE_SIZE).first()
            val canLoadMore = canLoadMoreFavoritesUseCase(PAGE_SIZE)
            _uiState.update {
                it.copy(
                    shows = cachedShows.ifEmpty { it.shows },
                    isInitialLoading = false,
                    isSyncing = cachedShows.isNotEmpty(),
                    canLoadMore = canLoadMore,
                    syncError = null,
                )
            }

            if (cachedShows.isEmpty()) return@launch

            try {
                refreshFavoritesUseCase()
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
    }

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
            _uiState.update { it.copy(isLoadingMore = true) }
            val currentLimit = visibleLimit.value
            val hasMore = canLoadMoreFavoritesUseCase(currentLimit)
            if (hasMore) {
                visibleLimit.value = currentLimit + PAGE_SIZE
            }
            val canLoadMoreAfter = canLoadMoreFavoritesUseCase(visibleLimit.value)
            _uiState.update {
                it.copy(
                    isLoadingMore = false,
                    canLoadMore = canLoadMoreAfter,
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

    private fun observeFavorites() {
        viewModelScope.launch {
            visibleLimit
                .flatMapLatest(observeFavoritesUseCase::invoke)
                .collect { shows ->
                    _uiState.update { state ->
                        state.copy(
                            shows = shows,
                            isInitialLoading = false,
                        )
                    }
                }
        }
    }

    private companion object {
        const val PAGE_SIZE = 10
    }
}
