package com.kelvinsaputra.tvpulse.ui.home

import com.kelvinsaputra.tvpulse.domain.model.TvShow

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val shows: List<TvShow>) : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}
