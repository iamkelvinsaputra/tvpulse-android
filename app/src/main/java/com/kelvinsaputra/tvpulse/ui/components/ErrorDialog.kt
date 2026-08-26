package com.kelvinsaputra.tvpulse.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun ErrorDialog(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Gangguan Koneksi",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
            )
        },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text("COBA LAGI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("TUTUP")
            }
        },
    )
}
