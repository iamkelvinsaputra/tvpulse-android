package com.kelvinsaputra.tvpulse.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelvinsaputra.tvpulse.ui.components.ScreenHeader
import com.kelvinsaputra.tvpulse.ui.components.ShowCard
import com.kelvinsaputra.tvpulse.ui.components.ShowListShimmer

@Composable
fun FavoritesRoute(
    onBack: () -> Unit,
    onShowClick: (Long) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreen(
        uiState = uiState,
        onBack = onBack,
        onShowClick = onShowClick,
    )
}

@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onBack: () -> Unit,
    onShowClick: (Long) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Favorites", onBack = onBack)

        when (uiState) {
            FavoritesUiState.Loading -> ShowListShimmer(modifier = Modifier.fillMaxSize())

            FavoritesUiState.Empty -> MessageState(
                "No favorites yet. Save a show from its detail page.",
            )

            is FavoritesUiState.Error -> MessageState(uiState.message)

            is FavoritesUiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = uiState.shows,
                    key = { it.id },
                ) { show ->
                    ShowCard(
                        show = show,
                        onClick = { onShowClick(show.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message)
    }
}
