package com.kelvinsaputra.tvpulse.ui.detail

import android.text.Html
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.ui.components.ErrorDialog

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
    actionError: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetryFavorite: () -> Unit,
    onDismissActionError: () -> Unit,
) {
    val errorMessage = (uiState as? DetailUiState.Error)?.message
    var showErrorDialog by remember(errorMessage) {
        mutableStateOf(errorMessage != null)
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
                            contentDescription = "Kembali",
                        )
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
                DetailUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )

                is DetailUiState.Error -> DetailErrorState(
                    message = uiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )

                is DetailUiState.Success -> DetailContent(
                    show = uiState.show,
                    isFavorite = uiState.isFavorite,
                    isFavoriteUpdating = uiState.isFavoriteUpdating,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }
    }

    if (errorMessage != null && showErrorDialog) {
        ErrorDialog(
            message = errorMessage,
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
            title = "Gagal Memperbarui Favorit",
            message = actionError,
            onRetry = onRetryFavorite,
            onDismiss = onDismissActionError,
        )
    }
}

@Composable
private fun DetailErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Gangguan Koneksi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("COBA LAGI")
        }
    }
}

@Composable
private fun DetailContent(
    show: TvShow,
    isFavorite: Boolean,
    isFavoriteUpdating: Boolean,
    onToggleFavorite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = show.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(12.dp))

        if (show.imageUrl != null) {
            AsyncImage(
                model = show.imageUrl,
                contentDescription = "Poster ${show.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.FillWidth,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }

        Spacer(Modifier.height(10.dp))

        ShowMetadataChips(show)

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onToggleFavorite,
            enabled = !isFavoriteUpdating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (isFavorite) {
                    "HAPUS DARI FAVORIT"
                } else {
                    "TAMBAH KE FAVORIT"
                },
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Sinopsis",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = show.summaryHtml
                ?.let { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString() }
                ?: "Sinopsis belum tersedia.",
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

private fun String.toDisplayStatus(): String = when (lowercase()) {
    "running" -> "Berjalan"
    "ended" -> "Selesai"
    "in development" -> "Dalam Pengembangan"
    else -> this
}
