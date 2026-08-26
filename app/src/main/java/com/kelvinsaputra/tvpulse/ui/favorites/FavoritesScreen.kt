package com.kelvinsaputra.tvpulse.ui.favorites

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.kelvinsaputra.tvpulse.ui.components.FavoriteShowCard
import com.kelvinsaputra.tvpulse.ui.components.SyncStatusBanner
import com.kelvinsaputra.tvpulse.ui.components.TvPulseMainHeader
import com.kelvinsaputra.tvpulse.ui.components.TvPulseTab
import com.kelvinsaputra.tvpulse.ui.components.UiError
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun FavoritesRoute(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onShowClick: (Long) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    FavoritesScreen(
        uiState = uiState,
        actionError = actionError,
        onBack = onBack,
        onHomeClick = onHomeClick,
        onShowClick = onShowClick,
        onRemoveClick = viewModel::removeFavorite,
        onRetryRemove = viewModel::retryRemoval,
        onDismissError = viewModel::dismissActionError,
        onRetrySync = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
    )
}

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    actionError: UiError?,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onShowClick: (Long) -> Unit,
    onRemoveClick: (TvShow) -> Unit,
    onRetryRemove: () -> Unit,
    onDismissError: () -> Unit,
    onRetrySync: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TvPulseMainHeader(
                title = stringResource(R.string.favorites_title),
                selectedTab = TvPulseTab.FAVORITE,
                onHomeClick = onHomeClick,
                onFavoriteClick = {},
                showBack = true,
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            when {
                uiState.isInitialLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                uiState.shows.isEmpty() -> EmptyFavorites()

                else -> FavoritesContent(
                    uiState = uiState,
                    onShowClick = onShowClick,
                    onRemoveClick = onRemoveClick,
                    onRetrySync = onRetrySync,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }

    if (actionError != null) {
        ErrorDialog(
            title = stringResource(R.string.remove_favorite_error_title),
            error = actionError,
            onRetry = onRetryRemove,
            onDismiss = onDismissError,
        )
    }
}

@Composable
private fun FavoritesContent(
    uiState: FavoritesUiState,
    onShowClick: (Long) -> Unit,
    onRemoveClick: (TvShow) -> Unit,
    onRetrySync: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(
        listState,
        uiState.shows.size,
        uiState.canLoadMore,
        uiState.isLoadingMore,
        uiState.isSyncing,
    ) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (
                    uiState.canLoadMore &&
                    !uiState.isLoadingMore &&
                    !uiState.isSyncing &&
                    uiState.shows.isNotEmpty() &&
                    lastVisibleIndex >= uiState.shows.lastIndex - LOAD_MORE_THRESHOLD
                ) {
                    onLoadMore()
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            SyncStatusBanner(
                isSyncing = uiState.isSyncing,
                syncError = uiState.syncError,
                onRetry = onRetrySync,
            )

            Text(
                text = stringResource(
                    R.string.favorites_count,
                    uiState.shows.size,
                ),
                modifier = Modifier.padding(vertical = 12.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                items = uiState.shows,
                key = { it.id },
            ) { show ->
                FavoriteShowCard(
                    show = show,
                    onClick = { onShowClick(show.id) },
                    onRemoveClick = { onRemoveClick(show) },
                )
            }

            if (uiState.isLoadingMore) {
                item(key = "loading-more") {
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
private fun EmptyFavorites() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.favorites_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.favorites_empty_message),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private const val LOAD_MORE_THRESHOLD = 3
