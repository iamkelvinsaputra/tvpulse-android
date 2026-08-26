package com.kelvinsaputra.tvpulse.ui.home

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.UiError

data class HomeUiState(
    val shows: List<TvShow> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val blockingError: UiError? = null,
    val syncError: UiError? = null,
)
