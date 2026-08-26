package com.kelvinsaputra.tvpulse.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import com.kelvinsaputra.tvpulse.R

@Composable
internal fun RetryableNetworkImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    if (imageUrl == null) {
        Box(
            modifier = modifier.background(backgroundColor),
        )
        return
    }

    val context = LocalPlatformContext.current
    val sizeResolver = rememberConstraintsSizeResolver()
    val request = remember(imageUrl, context, sizeResolver) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(sizeResolver)
            .build()
    }
    val painter = rememberAsyncImagePainter(
        model = request,
        contentScale = contentScale,
    )
    val state by painter.state.collectAsState()

    Box {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
                .then(sizeResolver)
                .background(backgroundColor),
        )

        when (state) {
            is AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp),
                    strokeWidth = 2.dp,
                )
            }

            is AsyncImagePainter.State.Error -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(MaterialTheme.shapes.extraLarge),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    tonalElevation = 2.dp,
                ) {
                    IconButton(
                        onClick = painter::restart,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.retry_image),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            is AsyncImagePainter.State.Success -> Unit
        }
    }
}
