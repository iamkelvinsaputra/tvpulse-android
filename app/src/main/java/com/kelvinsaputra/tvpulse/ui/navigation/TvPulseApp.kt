package com.kelvinsaputra.tvpulse.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kelvinsaputra.tvpulse.ui.detail.DetailRoute
import com.kelvinsaputra.tvpulse.ui.detail.DetailViewModel
import com.kelvinsaputra.tvpulse.ui.favorites.FavoritesRoute
import com.kelvinsaputra.tvpulse.ui.home.HomeRoute

@Composable
fun TvPulseApp(
    deepLinkEvent: DeepLinkEvent?,
    onExit: () -> Unit,
) {
    val backStack = rememberNavBackStack(HomeDestination)

    LaunchedEffect(deepLinkEvent?.sequence) {
        deepLinkEvent?.let { event ->
            val destination = DetailDestination(event.showId)
            if (backStack.lastOrNull() != destination) {
                backStack.add(destination)
            }
        }
    }

    fun navigateToDetail(showId: Long) {
        backStack.add(DetailDestination(showId))
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        } else {
            onExit()
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = ::navigateBack,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(NAV_ANIMATION_MILLIS),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(NAV_ANIMATION_MILLIS),
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(NAV_ANIMATION_MILLIS),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(NAV_ANIMATION_MILLIS),
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(NAV_ANIMATION_MILLIS),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(NAV_ANIMATION_MILLIS),
            )
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeDestination> {
                HomeRoute(
                    onFavoritesClick = { backStack.add(FavoritesDestination) },
                    onShowClick = ::navigateToDetail,
                )
            }

            entry<FavoritesDestination> {
                FavoritesRoute(
                    onBack = ::navigateBack,
                    onHomeClick = ::navigateBack,
                    onShowClick = ::navigateToDetail,
                )
            }

            entry<DetailDestination> { destination ->
                val viewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
                    creationCallback = { factory -> factory.create(destination) },
                )
                DetailRoute(
                    viewModel = viewModel,
                    onBack = ::navigateBack,
                )
            }
        },
    )
}

private const val NAV_ANIMATION_MILLIS = 250
