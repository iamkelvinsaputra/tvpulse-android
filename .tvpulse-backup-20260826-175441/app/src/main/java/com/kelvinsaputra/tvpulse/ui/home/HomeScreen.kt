package com.kelvinsaputra.tvpulse.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.ErrorDialog
import com.kelvinsaputra.tvpulse.ui.components.ShowGridCard
import com.kelvinsaputra.tvpulse.ui.components.TvPulseMainHeader
import com.kelvinsaputra.tvpulse.ui.components.TvPulseTab
import com.kelvinsaputra.tvpulse.ui.search.SearchUiState
import com.kelvinsaputra.tvpulse.ui.search.SearchViewModel

@Composable
fun HomeRoute(
    onShowClick: (Long) -> Unit,
    onFavoritesClick: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val query by searchViewModel.query.collectAsStateWithLifecycle()
    val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        homeUiState = homeUiState,
        query = query,
        searchUiState = searchUiState,
        onQueryChange = searchViewModel::onQueryChange,
        onShowClick = onShowClick,
        onFavoritesClick = onFavoritesClick,
        onHomeRetry = homeViewModel::retry,
        onSearchRetry = searchViewModel::retry,
    )
}

@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    query: String,
    searchUiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onShowClick: (Long) -> Unit,
    onFavoritesClick: () -> Unit,
    onHomeRetry: () -> Unit,
    onSearchRetry: () -> Unit,
) {
    val normalizedQuery = query.trim()
    val activeError = if (normalizedQuery.isBlank()) {
        (homeUiState as? HomeUiState.Error)?.message
    } else {
        (searchUiState as? SearchUiState.Error)
            ?.takeIf { it.query == normalizedQuery }
            ?.message
    }

    var showErrorDialog by remember(activeError) {
        mutableStateOf(activeError != null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TvPulseMainHeader(
                title = "TVPulse",
                selectedTab = TvPulseTab.HOME,
                onHomeClick = {},
                onFavoriteClick = onFavoritesClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(horizontal = 14.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                placeholder = {
                    Text("Cari Serial TV (misal: horror)...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus pencarian",
                            )
                        }
                    }
                },
                singleLine = true,
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (normalizedQuery.isBlank()) {
                    HomeContent(
                        uiState = homeUiState,
                        onShowClick = onShowClick,
                        onRetry = onHomeRetry,
                    )
                } else {
                    SearchContent(
                        query = normalizedQuery,
                        uiState = searchUiState,
                        onShowClick = onShowClick,
                        onRetry = onSearchRetry,
                    )
                }
            }
        }
    }

    if (activeError != null && showErrorDialog) {
        ErrorDialog(
            message = activeError,
            onRetry = {
                showErrorDialog = false
                if (normalizedQuery.isBlank()) {
                    onHomeRetry()
                } else {
                    onSearchRetry()
                }
            },
            onDismiss = {
                showErrorDialog = false
            },
        )
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onShowClick: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    when (uiState) {
        HomeUiState.Loading -> LoadingState()

        HomeUiState.Empty -> EmptyHome(onRetry = onRetry)

        is HomeUiState.Error -> ErrorState(
            message = uiState.message,
            onRetry = onRetry,
        )

        is HomeUiState.Success -> ShowGrid(
            title = "DAFTAR ACARA POPULER (${uiState.shows.size} Film)",
            shows = uiState.shows,
            onShowClick = onShowClick,
        )
    }
}

@Composable
private fun SearchContent(
    query: String,
    uiState: SearchUiState,
    onShowClick: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    if (uiState.queryValue() != query) {
        LoadingState()
        return
    }

    when (uiState) {
        is SearchUiState.Loading -> LoadingState()

        is SearchUiState.Success -> ShowGrid(
            title = "DAFTAR UTAMA (${uiState.shows.size} Film)",
            shows = uiState.shows,
            onShowClick = onShowClick,
        )

        is SearchUiState.Empty -> SearchEmptyState()

        is SearchUiState.Error -> ErrorState(
            message = uiState.message,
            onRetry = onRetry,
        )
    }
}

private fun SearchUiState.queryValue(): String = when (this) {
    is SearchUiState.Loading -> query
    is SearchUiState.Success -> query
    is SearchUiState.Empty -> query
    is SearchUiState.Error -> query
}

@Composable
private fun ShowGrid(
    title: String,
    shows: List<TvShow>,
    onShowClick: (Long) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(
                top = 4.dp,
                bottom = 8.dp,
            ),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = shows,
                key = { it.id },
            ) { show ->
                ShowGridCard(
                    show = show,
                    onClick = { onShowClick(show.id) },
                )
            }
        }
    }
}

@Composable
private fun SearchEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Pencarian Tidak Ditemukan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Hups! Tidak ditemukan apa pun.\nCoba kata kunci lain atau periksa ejaan.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Gangguan Koneksi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("COBA LAGI")
        }
    }
}

@Composable
private fun EmptyHome(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Tidak ada acara TV yang dapat ditampilkan.")

        Spacer(Modifier.height(12.dp))

        Button(onClick = onRetry) {
            Text("COBA LAGI")
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
