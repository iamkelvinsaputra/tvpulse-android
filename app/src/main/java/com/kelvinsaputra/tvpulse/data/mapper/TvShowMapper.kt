package com.kelvinsaputra.tvpulse.data.mapper

import com.kelvinsaputra.tvpulse.data.local.entity.FavoriteShowEntity
import com.kelvinsaputra.tvpulse.data.remote.dto.TvShowDto
import com.kelvinsaputra.tvpulse.domain.model.TvShow

private const val GENRE_SEPARATOR = "\u001F"

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
    runtime = runtime ?: averageRuntime,
    status = status,
)

fun TvShow.toFavoriteEntity(): FavoriteShowEntity = FavoriteShowEntity(
    id = id,
    name = name,
    imageUrl = imageUrl,
    summaryHtml = summaryHtml,
    rating = rating,
    genres = genres.joinToString(GENRE_SEPARATOR),
    schedule = schedule,
    network = network,
    premiered = premiered,
    language = language,
)

fun FavoriteShowEntity.toDomain(): TvShow = TvShow(
    id = id,
    name = name,
    imageUrl = imageUrl,
    summaryHtml = summaryHtml,
    rating = rating,
    genres = genres.takeIf { it.isNotEmpty() }?.split(GENRE_SEPARATOR).orEmpty(),
    schedule = schedule,
    network = network,
    premiered = premiered,
    language = language,
)
