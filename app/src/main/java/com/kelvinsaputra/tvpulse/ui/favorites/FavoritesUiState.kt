package com.kelvinsaputra.tvpulse.ui.favorites

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.UiError

data class FavoritesUiState(
    val shows: List<TvShow> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val syncError: UiError? = null,
)
