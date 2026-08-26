package com.kelvinsaputra.tvpulse.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.ErrorDialog
import com.kelvinsaputra.tvpulse.ui.components.FavoriteShowCard
import com.kelvinsaputra.tvpulse.ui.components.TvPulseMainHeader
import com.kelvinsaputra.tvpulse.ui.components.TvPulseTab

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
    )
}

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    actionError: String?,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onShowClick: (Long) -> Unit,
    onRemoveClick: (TvShow) -> Unit,
    onRetryRemove: () -> Unit,
    onDismissError: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TvPulseMainHeader(
                title = "Favorit Saya",
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
            when (uiState) {
                FavoritesUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                FavoritesUiState.Empty -> EmptyFavorites()

                is FavoritesUiState.Error -> Text(
                    text = uiState.message,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    textAlign = TextAlign.Center,
                )

                is FavoritesUiState.Success -> Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = "Favorit Saya (${uiState.shows.size} Serial TV)",
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    LazyColumn(
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
                    }
                }
            }
        }
    }

    if (actionError != null) {
        ErrorDialog(
            title = "Gagal Menghapus Favorit",
            message = actionError,
            onRetry = onRetryRemove,
            onDismiss = onDismissError,
        )
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
            text = "Belum Ada Favorit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Jelajahi acara TV dan tambahkan acara favoritmu.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
