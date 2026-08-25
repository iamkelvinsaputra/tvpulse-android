package com.kelvinsaputra.tvpulse.data.mapper

import com.kelvinsaputra.tvpulse.data.remote.dto.TvShowDto
import com.kelvinsaputra.tvpulse.domain.model.TvShow

fun TvShowDto.toDomain(): TvShow = TvShow(
    id = id,
    name = name,
    imageUrl = image?.original ?: image?.medium,
    summaryHtml = summary,
    rating = rating?.average,
    genres = genres,
    schedule = schedule
        ?.let { value ->
            val days = value.days.joinToString(", ")
            listOf(days, value.time)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
                .ifBlank { null }
        },
    network = network?.name ?: webChannel?.name,
    premiered = premiered,
    language = language,
)
