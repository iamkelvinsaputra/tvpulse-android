package com.kelvinsaputra.tvpulse.ui.components

import java.io.IOException

fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "Unable to reach TVmaze. Check your connection and try again."
    else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
}
