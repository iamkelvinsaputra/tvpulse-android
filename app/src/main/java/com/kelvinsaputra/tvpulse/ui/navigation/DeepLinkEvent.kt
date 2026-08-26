package com.kelvinsaputra.tvpulse.ui.navigation

sealed interface DeepLinkTarget {
    data object Home : DeepLinkTarget

    data object Favorites : DeepLinkTarget

    data class Search(
        val query: String?,
    ) : DeepLinkTarget

    data class Detail(
        val showId: Long,
    ) : DeepLinkTarget
}

data class DeepLinkEvent(
    val target: DeepLinkTarget,
    val sequence: Long,
)

data class HomeDeepLinkRequest(
    val query: String,
    val focusSearch: Boolean,
    val sequence: Long,
)
