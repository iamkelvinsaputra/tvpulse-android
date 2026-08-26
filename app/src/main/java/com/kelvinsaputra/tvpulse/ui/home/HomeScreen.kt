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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelvinsaputra.tvpulse.R
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.ErrorDialog
import com.kelvinsaputra.tvpulse.ui.components.ShowGridCard
import com.kelvinsaputra.tvpulse.ui.components.SyncStatusBanner
import com.kelvinsaputra.tvpulse.ui.components.TvPulseMainHeader
import com.kelvinsaputra.tvpulse.ui.components.TvPulseTab
import com.kelvinsaputra.tvpulse.ui.components.UiError
import com.kelvinsaputra.tvpulse.ui.components.asMessage
import com.kelvinsaputra.tvpulse.ui.search.SearchUiState
import com.kelvinsaputra.tvpulse.ui.search.SearchViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

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
        onHomeLoadMore = homeViewModel::loadMore,
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
    onHomeLoadMore: () -> Unit,
    onSearchRetry: () -> Unit,
) {
    val normalizedQuery = query.trim()
    val activeError = if (normalizedQuery.isBlank()) {
        homeUiState.blockingError
    } else {
        searchUiState
            .takeIf { it.query == normalizedQuery }
            ?.blockingError
    }

    var showErrorDialog by remember(activeError) {
        mutableStateOf(activeError != null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TvPulseMainHeader(
                title = stringResource(R.string.app_name),
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
                    Text(stringResource(R.string.search_placeholder))
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
                                contentDescription = stringResource(R.string.clear_search),
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
                        onLoadMore = onHomeLoadMore,
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
            error = activeError,
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
    onLoadMore: () -> Unit,
) {
    when {
        uiState.isInitialLoading && uiState.shows.isEmpty() -> LoadingState()
        uiState.blockingError != null && uiState.shows.isEmpty() -> ErrorState(
            error = uiState.blockingError,
            onRetry = onRetry,
        )
        uiState.shows.isEmpty() -> Column(modifier = Modifier.fillMaxSize()) {
            SyncStatusBanner(
                isSyncing = uiState.isSyncing,
                syncError = uiState.syncError,
                onRetry = onRetry,
            )
            Box(modifier = Modifier.weight(1f)) {
                EmptyHome(onRetry = onRetry)
            }
        }
        else -> Column(modifier = Modifier.fillMaxSize()) {
            SyncStatusBanner(
                isSyncing = uiState.isSyncing,
                syncError = uiState.syncError,
                onRetry = onRetry,
            )
            ShowGrid(
                title = stringResource(
                    R.string.popular_shows_count,
                    uiState.shows.size,
                ),
                shows = uiState.shows,
                isLoadingMore = uiState.isLoadingMore,
                canLoadMore = uiState.canLoadMore,
                onShowClick = onShowClick,
                onLoadMore = onLoadMore,
            )
        }
    }
}

@Composable
private fun SearchContent(
    query: String,
    uiState: SearchUiState,
    onShowClick: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    if (uiState.query != query) {
        LoadingState()
        return
    }

    when {
        uiState.isInitialLoading && uiState.shows.isEmpty() -> LoadingState()
        uiState.blockingError != null && uiState.shows.isEmpty() -> ErrorState(
            error = uiState.blockingError,
            onRetry = onRetry,
        )
        uiState.hasSearched && uiState.shows.isEmpty() -> Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            SyncStatusBanner(
                isSyncing = uiState.isSyncing,
                syncError = uiState.syncError,
                onRetry = onRetry,
            )
            Box(modifier = Modifier.weight(1f)) {
                SearchEmptyState()
            }
        }
        else -> Column(modifier = Modifier.fillMaxSize()) {
            SyncStatusBanner(
                isSyncing = uiState.isSyncing,
                syncError = uiState.syncError,
                onRetry = onRetry,
            )
            ShowGrid(
                title = stringResource(
                    R.string.search_results_count,
                    uiState.shows.size,
                ),
                shows = uiState.shows,
                isLoadingMore = false,
                canLoadMore = false,
                onShowClick = onShowClick,
                onLoadMore = {},
            )
        }
    }
}

@Composable
private fun ShowGrid(
    title: String,
    shows: List<TvShow>,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onShowClick: (Long) -> Unit,
    onLoadMore: () -> Unit,
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, shows.size, canLoadMore, isLoadingMore) {
        snapshotFlow {
            gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (
                    canLoadMore &&
                    !isLoadingMore &&
                    shows.isNotEmpty() &&
                    lastVisibleIndex >= shows.lastIndex - LOAD_MORE_THRESHOLD
                ) {
                    onLoadMore()
                }
            }
    }

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
            state = gridState,
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

            if (isLoadingMore) {
                item(
                    key = "loading-more",
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
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
            text = stringResource(R.string.search_not_found),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.search_not_found_message),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(
    error: UiError,
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
            text = stringResource(R.string.connection_error_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = error.asMessage(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
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
        Text(stringResource(R.string.home_empty))

        Spacer(Modifier.height(12.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
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

private const val LOAD_MORE_THRESHOLD = 4
