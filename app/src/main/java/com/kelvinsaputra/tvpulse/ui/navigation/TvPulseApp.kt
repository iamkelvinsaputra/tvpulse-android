package com.kelvinsaputra.tvpulse.ui.navigation

import androidx.compose.runtime.Composable
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
import com.kelvinsaputra.tvpulse.ui.search.SearchRoute

@Composable
fun TvPulseApp(onExit: () -> Unit) {
    val backStack = rememberNavBackStack(HomeDestination)
    fun navigateBack() { if (backStack.size > 1) backStack.removeLastOrNull() else onExit() }
    fun navigateToDetail(showId: Long) { backStack.add(DetailDestination(showId)) }

    NavDisplay(
        backStack = backStack,
        onBack = ::navigateBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeDestination> {
                HomeRoute(
                    onSearchClick = { backStack.add(SearchDestination) },
                    onFavoritesClick = { backStack.add(FavoritesDestination) },
                    onShowClick = ::navigateToDetail,
                )
            }
            entry<SearchDestination> { SearchRoute(onBack = ::navigateBack, onShowClick = ::navigateToDetail) }
            entry<FavoritesDestination> { FavoritesRoute(onBack = ::navigateBack, onShowClick = ::navigateToDetail) }
            entry<DetailDestination> { destination ->
                val viewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
                    creationCallback = { factory -> factory.create(destination) },
                )
                DetailRoute(viewModel = viewModel, onBack = ::navigateBack)
            }
        },
    )
}
