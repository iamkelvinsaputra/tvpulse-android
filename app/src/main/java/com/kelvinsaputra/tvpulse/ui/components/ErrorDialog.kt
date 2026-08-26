package com.kelvinsaputra.tvpulse.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.kelvinsaputra.tvpulse.R

@Composable
fun ErrorDialog(
    error: UiError,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    title: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title ?: stringResource(R.string.connection_error_title),
                fontWeight = FontWeight.Bold,
            )
        },
        text = { Text(error.asMessage()) },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
fun UiError.asMessage(): String = stringResource(
    when (this) {
        UiError.CONNECTION -> R.string.connection_error_message
        UiError.GENERIC -> R.string.generic_error_message
    }
)
