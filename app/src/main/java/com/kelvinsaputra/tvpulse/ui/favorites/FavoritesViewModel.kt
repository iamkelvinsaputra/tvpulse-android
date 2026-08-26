package com.kelvinsaputra.tvpulse.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveFavoritesUseCase
import com.kelvinsaputra.tvpulse.domain.usecase.SetFavoriteUseCase
import com.kelvinsaputra.tvpulse.ui.components.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = observeFavoritesUseCase()
        .map<List<TvShow>, FavoritesUiState> { shows ->
            if (shows.isEmpty()) {
                FavoritesUiState.Empty
            } else {
                FavoritesUiState.Success(shows)
            }
        }
        .catch { throwable ->
            emit(FavoritesUiState.Error(throwable.toUserMessage()))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState.Loading,
        )

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private var failedRemoval: TvShow? = null

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
                _actionError.value = exception.toUserMessage()
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
}
