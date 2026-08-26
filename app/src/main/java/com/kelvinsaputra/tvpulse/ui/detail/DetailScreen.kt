package com.kelvinsaputra.tvpulse.ui.detail

import android.text.Html
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.kelvinsaputra.tvpulse.ui.components.ScreenHeader
import com.kelvinsaputra.tvpulse.ui.components.ShowListShimmer

@Composable
fun DetailRoute(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    var showErrorDialog by remember(uiState) {
        mutableStateOf(uiState is DetailUiState.Error)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Show detail",
            onBack = onBack,
            actions = {
                if (uiState is DetailUiState.Success) {
                    TextButton(
                        onClick = onToggleFavorite,
                        enabled = !uiState.isFavoriteUpdating,
                    ) {
                        Text(if (uiState.isFavorite) "♥ Saved" else "♡ Favorite")
                    }
                }
            },
        )

        when (uiState) {
            DetailUiState.Loading -> ShowListShimmer(modifier = Modifier.fillMaxSize())

            is DetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(onClick = onRetry) { Text("Try again") }
                }
                if (showErrorDialog) {
                    ErrorDialog(
                        message = uiState.message,
                        onRetry = {
                            showErrorDialog = false
                            onRetry()
                        },
                        onDismiss = { showErrorDialog = false },
                    )
                }
            }

            is DetailUiState.Success -> DetailContent(uiState.show)
        }
    }
}

@Composable
private fun DetailContent(show: TvShow) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        show.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = "${show.name} poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
            )
        }

        Text(
            text = show.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            show.rating?.let { Text("★ %.1f".format(it)) }
            show.premiered?.let { Text(it.take(4)) }
            show.language?.let { Text(it) }
        }

        if (show.genres.isNotEmpty()) {
            Text(show.genres.joinToString(" · "))
        }
        show.schedule?.let { Metadata("Schedule", it) }
        show.network?.let { Metadata("Network", it) }

        show.summaryHtml?.let { summary ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = Html.fromHtml(summary, Html.FROM_HTML_MODE_LEGACY).toString(),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun Metadata(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value)
    }
}
