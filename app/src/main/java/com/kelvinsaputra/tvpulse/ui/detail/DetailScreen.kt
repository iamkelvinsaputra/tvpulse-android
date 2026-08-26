package com.kelvinsaputra.tvpulse.ui.detail

import android.text.Html
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelvinsaputra.tvpulse.R
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.ErrorDialog
import com.kelvinsaputra.tvpulse.ui.components.RetryableNetworkImage
import com.kelvinsaputra.tvpulse.ui.components.LanguageSwitcher
import com.kelvinsaputra.tvpulse.ui.components.SyncStatusBanner
import com.kelvinsaputra.tvpulse.ui.components.UiError
import com.kelvinsaputra.tvpulse.ui.components.asMessage

@Composable
fun DetailRoute(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    DetailScreen(
        uiState = uiState,
        actionError = actionError,
        onBack = onBack,
        onRetry = viewModel::retry,
        onToggleFavorite = viewModel::toggleFavorite,
        onRetryFavorite = viewModel::retryFavoriteUpdate,
        onDismissActionError = viewModel::dismissActionError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    actionError: UiError?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetryFavorite: () -> Unit,
    onDismissActionError: () -> Unit,
) {
    val activeError = uiState.blockingError
    var showErrorDialog by remember(activeError) {
        mutableStateOf(activeError != null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    LanguageSwitcher()
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
            when {
                uiState.isInitialLoading && uiState.show == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.blockingError != null && uiState.show == null -> {
                    DetailErrorState(
                        error = uiState.blockingError,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                uiState.show != null -> {
                    DetailContent(
                        show = uiState.show,
                        isSyncing = uiState.isSyncing,
                        syncError = uiState.syncError,
                        isFavorite = uiState.isFavorite,
                        isFavoriteUpdating = uiState.isFavoriteUpdating,
                        onRetrySync = onRetry,
                        onToggleFavorite = onToggleFavorite,
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
                onRetry()
            },
            onDismiss = {
                showErrorDialog = false
            },
        )
    }

    if (actionError != null) {
        ErrorDialog(
            title = stringResource(R.string.favorite_update_error_title),
            error = actionError,
            onRetry = onRetryFavorite,
            onDismiss = onDismissActionError,
        )
    }
}

@Composable
private fun DetailErrorState(
    error: UiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.connection_error_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = error.asMessage(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun DetailContent(
    show: TvShow,
    isSyncing: Boolean,
    syncError: UiError?,
    isFavorite: Boolean,
    isFavoriteUpdating: Boolean,
    onRetrySync: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        SyncStatusBanner(
            isSyncing = isSyncing,
            syncError = syncError,
            onRetry = onRetrySync,
        )

        Text(
            text = show.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        RetryableNetworkImage(
            imageUrl = show.imageUrl,
            contentDescription = stringResource(
                R.string.poster_content_description,
                show.name,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .wrapContentHeight(),
            contentScale = ContentScale.FillWidth,
        )

        Spacer(Modifier.height(10.dp))

        ShowMetadataChips(show)

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onToggleFavorite,
            enabled = !isFavoriteUpdating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(
                    if (isFavorite) {
                        R.string.remove_from_favorite
                    } else {
                        R.string.add_favorite
                    }
                ),
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.synopsis),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = show.summaryHtml
                ?.let { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString() }
                ?: stringResource(R.string.synopsis_unavailable),
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ShowMetadataChips(show: TvShow) {
    val metadata = buildList {
        addAll(show.genres)
        show.runtime?.let { add("$it min") }
        show.status?.let { add(it.toDisplayStatus()) }
    }

    if (metadata.isEmpty()) return

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(end = 16.dp),
    ) {
        items(metadata) { item ->
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun String.toDisplayStatus(): String = when (lowercase()) {
    "running" -> stringResource(R.string.status_running)
    "ended" -> stringResource(R.string.status_ended)
    "in development" -> stringResource(R.string.status_in_development)
    else -> this
}
