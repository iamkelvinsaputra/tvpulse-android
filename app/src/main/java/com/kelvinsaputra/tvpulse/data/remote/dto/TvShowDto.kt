package com.kelvinsaputra.tvpulse.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TvShowDto(
    val id: Long,
    val name: String,
    val language: String? = null,
    val genres: List<String> = emptyList(),
    val premiered: String? = null,
    val schedule: ScheduleDto? = null,
    val rating: RatingDto? = null,
    val network: NetworkDto? = null,
    val webChannel: NetworkDto? = null,
    val image: ImageDto? = null,
    val summary: String? = null,
)

@Serializable
data class ScheduleDto(
    val time: String = "",
    val days: List<String> = emptyList(),
)

@Serializable
data class RatingDto(
    val average: Double? = null,
)

@Serializable
data class NetworkDto(
    val name: String,
)

@Serializable
data class ImageDto(
    val medium: String? = null,
    val original: String? = null,
)
