package com.kelvinsaputra.tvpulse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.usecase.GetShowDetailUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveIsFavoriteUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveShowDetailUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.SetFavoriteUseCase
import com.kelvinsaputra.tvpulse.ui.components.UiError
import com.kelvinsaputra.tvpulse.ui.components.toUiError
import com.kelvinsaputra.tvpulse.ui.navigation.DetailDestination
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val destination: DetailDestination,
    private val observeShowDetailUseCase: ObserveShowDetailUseCase,
    private val getShowDetailUseCase: GetShowDetailUseCase,
    private val observeIsFavoriteUseCase: ObserveIsFavoriteUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _actionError = MutableStateFlow<UiError?>(null)
    val actionError: StateFlow<UiError?> = _actionError.asStateFlow()

    private var failedFavoriteTarget: Boolean? = null

    init {
        observeCachedDetail()
        observeFavoriteState()
        refresh()
    }

    fun retry() = refresh()

    fun toggleFavorite() {
        val state = _uiState.value
        val show = state.show ?: return
        if (state.isFavoriteUpdating) return
        updateFavorite(show, isFavorite = !state.isFavorite)
    }

    fun retryFavoriteUpdate() {
        val target = failedFavoriteTarget ?: return
        val show = _uiState.value.show ?: return
        updateFavorite(show, target)
    }

    fun dismissActionError() {
        failedFavoriteTarget = null
        _actionError.value = null
    }

    private fun observeCachedDetail() {
        viewModelScope.launch {
            observeShowDetailUseCase(destination.showId).collect { show ->
                if (show != null) {
                    _uiState.update {
                        it.copy(
                            show = show,
                            isInitialLoading = false,
                        )
                    }
                }
            }
        }
    }

    private fun observeFavoriteState() {
        viewModelScope.launch {
            observeIsFavoriteUseCase(destination.showId).collect { isFavorite ->
                _uiState.update {
                    it.copy(
                        isFavorite = isFavorite,
                        isFavoriteUpdating = false,
                    )
                }
            }
        }
    }

    private fun refresh() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            val cachedShow = observeShowDetailUseCase(destination.showId).first()
            _uiState.update {
                it.copy(
                    show = cachedShow ?: it.show,
                    isInitialLoading = cachedShow == null && it.show == null,
                    isSyncing = cachedShow != null || it.show != null,
                    blockingError = null,
                    syncError = null,
                )
            }

            try {
                getShowDetailUseCase(destination.showId)
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isSyncing = false,
                        blockingError = null,
                        syncError = null,
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val error = exception.toUiError()
                _uiState.update { state ->
                    val hasCache = state.show != null || cachedShow != null
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

    private fun updateFavorite(show: com.kelvinsaputra.tvpulse.domain.model.TvShow, isFavorite: Boolean) {
        _actionError.value = null
        failedFavoriteTarget = null
        _uiState.update { it.copy(isFavoriteUpdating = true) }

        viewModelScope.launch {
            try {
                setFavoriteUseCase(
                    show = show,
                    isFavorite = isFavorite,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                failedFavoriteTarget = isFavorite
                _uiState.update { it.copy(isFavoriteUpdating = false) }
                _actionError.value = exception.toUiError()
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(destination: DetailDestination): DetailViewModel
    }
}
