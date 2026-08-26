package com.kelvinsaputra.tvpulse.ui.components

import java.io.IOException

enum class UiError {
    CONNECTION,
    GENERIC,
}

fun Throwable.toUiError(): UiError = when (this) {
    is IOException -> UiError.CONNECTION
    else -> UiError.GENERIC
}
