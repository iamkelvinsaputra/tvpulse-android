package com.kelvinsaputra.tvpulse.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "search_results",
    primaryKeys = ["query", "showId"],
)
data class SearchResultEntity(
    val query: String,
    val showId: Long,
    val position: Int,
)
