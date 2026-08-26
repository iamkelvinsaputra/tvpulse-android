package com.kelvinsaputra.tvpulse.domain.model

data class TvShow(
    val id: Long,
    val name: String,
    val imageUrl: String?,
    val summaryHtml: String?,
    val rating: Double?,
    val genres: List<String>,
    val schedule: String?,
    val network: String?,
    val premiered: String?,
    val language: String?,
    val runtime: Int? = null,
    val status: String? = null,
)
