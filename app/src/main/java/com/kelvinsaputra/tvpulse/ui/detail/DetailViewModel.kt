package com.kelvinsaputra.tvpulse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.usecase.GetShowDetailUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveIsFavoriteUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.SetFavoriteUseCase
import com.kelvinsaputra.tvpulse.ui.components.toUserMessage
import com.kelvinsaputra.tvpulse.ui.navigation.DetailDestination
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val destination: DetailDestination,
    private val getShowDetailUseCase: GetShowDetailUseCase,
    private val observeIsFavoriteUseCase: ObserveIsFavoriteUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private var latestFavoriteState = false
    private var failedFavoriteTarget: Boolean? = null

    init {
        observeFavoriteState()
        loadDetail()
    }

    fun retry() {
        loadDetail()
    }

    fun toggleFavorite() {
        val state = _uiState.value as? DetailUiState.Success ?: return
        updateFavorite(isFavorite = !state.isFavorite)
    }

    fun retryFavoriteUpdate() {
        val target = failedFavoriteTarget ?: return
        updateFavorite(isFavorite = target)
    }

    fun dismissActionError() {
        failedFavoriteTarget = null
        _actionError.value = null
    }

    private fun updateFavorite(isFavorite: Boolean) {
        val state = _uiState.value as? DetailUiState.Success ?: return
        if (state.isFavoriteUpdating) return

        _actionError.value = null
        failedFavoriteTarget = null
        _uiState.value = state.copy(isFavoriteUpdating = true)

        viewModelScope.launch {
            try {
                setFavoriteUseCase(
                    show = state.show,
                    isFavorite = isFavorite,
                )

                val current = _uiState.value as? DetailUiState.Success
                if (current != null) {
                    _uiState.value = current.copy(isFavoriteUpdating = false)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                failedFavoriteTarget = isFavorite

                val current = _uiState.value as? DetailUiState.Success ?: state
                _uiState.value = current.copy(isFavoriteUpdating = false)
                _actionError.value = exception.toUserMessage()
            }
        }
    }

    private fun observeFavoriteState() {
        viewModelScope.launch {
            observeIsFavoriteUseCase(destination.showId).collect { isFavorite ->
                latestFavoriteState = isFavorite
                val current = _uiState.value as? DetailUiState.Success
                if (current != null) {
                    _uiState.value = current.copy(
                        isFavorite = isFavorite,
                        isFavoriteUpdating = false,
                    )
                }
            }
        }
    }

    private fun loadDetail() {
        _uiState.value = DetailUiState.Loading

        viewModelScope.launch {
            try {
                val show = getShowDetailUseCase(destination.showId)
                _uiState.value = DetailUiState.Success(
                    show = show,
                    isFavorite = latestFavoriteState,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = DetailUiState.Error(exception.toUserMessage())
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(destination: DetailDestination): DetailViewModel
    }
}
