package com.kelvinsaputra.tvpulse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_shows")
data class CachedShowEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val imageUrl: String?,
    val summaryHtml: String?,
    val rating: Double?,
    val genres: String,
    val schedule: String?,
    val network: String?,
    val premiered: String?,
    val language: String?,
    val runtime: Int?,
    val status: String?,
    val updatedAtEpochMillis: Long,
)
