package com.kelvinsaputra.tvpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kelvinsaputra.tvpulse.ui.home.HomeUiState
import com.kelvinsaputra.tvpulse.ui.home.HomeViewModel
import com.kelvinsaputra.tvpulse.ui.navigation.DetailDestination
import com.kelvinsaputra.tvpulse.ui.navigation.FavoritesDestination
import com.kelvinsaputra.tvpulse.ui.navigation.HomeDestination
import com.kelvinsaputra.tvpulse.ui.navigation.SearchDestination
import com.kelvinsaputra.tvpulse.ui.theme.TVPulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TVPulseTheme {
                TvPulseApp()
            }
        }
    }
}

@Composable
fun TvPulseApp() {
    val backStack =
        rememberNavBackStack(HomeDestination)

    NavDisplay(
        backStack = backStack,

        onBack = {
            backStack.removeLastOrNull()
        },

        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),

        entryProvider = entryProvider {
            entry<HomeDestination> {
                HomeRoute(
                    onSearchClick = {
                        backStack.add(SearchDestination)
                    },
                    onFavoritesClick = {
                        backStack.add(FavoritesDestination)
                    },
                    onShowClick = { showId ->
                        backStack.add(
                            DetailDestination(
                                showId = showId
                            )
                        )
                    }
                )
            }

            entry<SearchDestination> {
                SearchScreen()
            }

            entry<FavoritesDestination> {
                FavoritesScreen()
            }

            entry<DetailDestination> { destination ->
                DetailScreen(
                    showId = destination.showId
                )
            }
        },
    )
}

@Composable
fun HomeRoute(
    onShowClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onShowClick = onShowClick,
        onSearchClick = onSearchClick,
        onFavoritesClick = onFavoritesClick
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onShowClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Column {
        Button(onClick = onSearchClick) {
            Text("Search")
        }

        Button(onClick = onFavoritesClick) {
            Text("Favorites")
        }

        when(uiState) {
            HomeUiState.Loading -> {
                Text("Loading")
            }

            HomeUiState.Empty -> {
                Text("No shows")
            }

            is HomeUiState.Error -> {
                Text(uiState.message)
            }

            is HomeUiState.Success -> {
                uiState.shows.forEach { show ->
                    TextButton(
                        onClick = {
                            onShowClick(show.id)
                        }
                    ) {
                        Text(show.name)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen() {
    Text("Search")
}

@Composable
fun FavoritesScreen() {
    Text("Favorites")
}

@Composable
fun DetailScreen(showId: Long) {
    Text("Detail: $showId")
}