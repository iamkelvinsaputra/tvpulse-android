package com.kelvinsaputra.tvpulse.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination : NavKey

@Serializable
data object FavoritesDestination : NavKey

@Serializable
data class DetailDestination(
    val showId: Long,
) : NavKey
