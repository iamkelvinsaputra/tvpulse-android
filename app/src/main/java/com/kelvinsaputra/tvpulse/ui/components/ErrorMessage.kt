package com.kelvinsaputra.tvpulse.ui.components

import java.io.IOException

fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "Terjadi kesalahan saat menghubungi server kami. Pastikan perangkat Anda terhubung ke internet dan coba lagi."
    else -> message?.takeIf { it.isNotBlank() } ?: "Terjadi kesalahan. Silakan coba lagi."
}
