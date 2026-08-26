package com.kelvinsaputra.tvpulse.ui.favorites

import com.kelvinsaputra.tvpulse.domain.model.TvShow

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Success(val shows: List<TvShow>) : FavoritesUiState
    data object Empty : FavoritesUiState
    data class Error(val message: String) : FavoritesUiState
}
