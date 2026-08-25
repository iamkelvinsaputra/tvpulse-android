package com.kelvinsaputra.tvpulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchShowDto(
    val score: Double = 0.0,
    val show: TvShowDto,
)
