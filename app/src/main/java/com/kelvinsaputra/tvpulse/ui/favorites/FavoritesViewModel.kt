package com.kelvinsaputra.tvpulse.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.usecase.ObserveFavoritesUseCase
import com.kelvinsaputra.tvpulse.ui.components.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavoritesUseCase: ObserveFavoritesUseCase,
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
}
