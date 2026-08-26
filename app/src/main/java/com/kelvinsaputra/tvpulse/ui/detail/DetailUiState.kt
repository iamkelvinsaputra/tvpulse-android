package com.kelvinsaputra.tvpulse.ui.detail

import com.kelvinsaputra.tvpulse.domain.model.TvShow

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(
        val show: TvShow,
        val isFavorite: Boolean,
        val isFavoriteUpdating: Boolean = false,
    ) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
