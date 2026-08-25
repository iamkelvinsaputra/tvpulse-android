package com.kelvinsaputra.tvpulse.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
fun SearchRoute(
    onBack: () -> Unit,
    onShowClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchScreen(
        query = query,
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onShowClick = onShowClick,
        onBack = onBack,
        onRetry = viewModel::retry,
    )
}

@Composable
fun SearchScreen(
    query: String,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onShowClick: (Long) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    var showErrorDialog by remember(uiState) {
        mutableStateOf(uiState is SearchUiState.Error)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Search", onBack = onBack)

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            label = { Text("Show title") },
            placeholder = { Text("e.g. The Bear") },
        )

        when (uiState) {
            is SearchUiState.Loading -> ShowListShimmer(modifier = Modifier.fillMaxSize())

            is SearchUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(uiState.message)
                }
            }

            is SearchUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Search failed.")
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

            is SearchUiState.Success -> {
                LazyColumn(
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
}
