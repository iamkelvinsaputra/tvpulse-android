package com.kelvinsaputra.tvpulse.ui.search

import com.kelvinsaputra.tvpulse.domain.model.TvShow

sealed interface SearchUiState {
    data class Loading(val query: String) : SearchUiState
    data class Success(val query: String, val shows: List<TvShow>) : SearchUiState
    data class Empty(
        val query: String = "",
        val message: String = "Search for a TV show.",
    ) : SearchUiState
    data class Error(val query: String, val message: String) : SearchUiState
}
