package com.kelvinsaputra.tvpulse.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kelvinsaputra.tvpulse.ui.home.HomeRoute

@Composable
fun TvPulseApp(
    onExit: () -> Unit,
) {
    val backStack = rememberNavBackStack(HomeDestination)

    fun navigateBack() {
        if (backStack.size > 1) backStack.removeLastOrNull() else onExit()
    }

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
                    onShowClick = { showId -> backStack.add(DetailDestination(showId)) },
                )
            }
            entry<SearchDestination> { PlaceholderScreen("Search") }
            entry<FavoritesDestination> { PlaceholderScreen("Favorites") }
            entry<DetailDestination> { destination -> PlaceholderScreen("Detail: ${destination.showId}") }
        },
    )
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text) }
}
