package com.kelvinsaputra.tvpulse.ui.detail

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.UiError

data class DetailUiState(
    val show: TvShow? = null,
    val isInitialLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val isFavorite: Boolean = false,
    val isFavoriteUpdating: Boolean = false,
    val blockingError: UiError? = null,
    val syncError: UiError? = null,
)
