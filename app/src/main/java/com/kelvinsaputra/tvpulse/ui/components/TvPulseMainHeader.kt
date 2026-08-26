package com.kelvinsaputra.tvpulse.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

internal enum class TvPulseTab {
    HOME,
    FAVORITE,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TvPulseMainHeader(
    title: String,
    selectedTab: TvPulseTab,
    onHomeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    showBack: Boolean = false,
    onBack: () -> Unit = {},
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        )

        TabRow(
            selectedTabIndex = if (selectedTab == TvPulseTab.HOME) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Tab(
                selected = selectedTab == TvPulseTab.HOME,
                onClick = onHomeClick,
                text = {
                    Text(
                        text = "HOME",
                        fontWeight = if (selectedTab == TvPulseTab.HOME) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                    )
                },
            )

            Tab(
                selected = selectedTab == TvPulseTab.FAVORITE,
                onClick = onFavoriteClick,
                text = {
                    Text(
                        text = "FAVORITE",
                        fontWeight = if (selectedTab == TvPulseTab.FAVORITE) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                    )
                },
            )
        }
    }
}
