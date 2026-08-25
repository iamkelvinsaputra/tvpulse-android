package com.kelvinsaputra.tvpulse.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelvinsaputra.tvpulse.ui.components.ErrorDialog
import com.kelvinsaputra.tvpulse.ui.components.ScreenHeader
import com.kelvinsaputra.tvpulse.ui.components.ShowCard
import com.kelvinsaputra.tvpulse.ui.components.ShowListShimmer

@Composable
fun HomeRoute(
    onShowClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onShowClick = onShowClick,
        onSearchClick = onSearchClick,
        onFavoritesClick = onFavoritesClick,
        onRetry = viewModel::retry,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onShowClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onRetry: () -> Unit,
) {
    var showErrorDialog by remember(uiState) {
        mutableStateOf(uiState is HomeUiState.Error)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ScreenHeader(
                title = "TVPulse",
                actions = {
                    TextButton(onClick = onSearchClick) {
                        Text("Search")
                    }

                    TextButton(onClick = onFavoritesClick) {
                        Text("Favorites")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            when (uiState) {
                HomeUiState.Loading -> {
                    ShowListShimmer(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                HomeUiState.Empty -> {
                    EmptyHome(onRetry = onRetry)
                }

                is HomeUiState.Error -> {
                    EmptyHome(onRetry = onRetry)

                    if (showErrorDialog) {
                        ErrorDialog(
                            message = uiState.message,
                            onRetry = {
                                showErrorDialog = false
                                onRetry()
                            },
                            onDismiss = {
                                showErrorDialog = false
                            },
                        )
                    }
                }

                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text(
                                text = "Top 30 shows",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }

                        items(
                            items = uiState.shows,
                            key = { it.id },
                        ) { show ->
                            ShowCard(
                                show = show,
                                onClick = {
                                    onShowClick(show.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHome(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("No shows available.")
            Button(onClick = onRetry) {
                Text("Try again")
            }
        }
    }
}
