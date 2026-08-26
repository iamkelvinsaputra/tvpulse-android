package com.kelvinsaputra.tvpulse.ui.search

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.UiError

data class SearchUiState(
    val query: String = "",
    val shows: List<TvShow> = emptyList(),
    val hasSearched: Boolean = false,
    val isInitialLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val blockingError: UiError? = null,
    val syncError: UiError? = null,
)
