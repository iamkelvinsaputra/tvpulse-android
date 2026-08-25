package com.kelvinsaputra.tvpulse.ui.detail

import com.kelvinsaputra.tvpulse.domain.model.TvShow

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val show: TvShow) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
