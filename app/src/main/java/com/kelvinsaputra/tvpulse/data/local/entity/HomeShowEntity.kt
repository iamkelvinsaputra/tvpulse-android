package com.kelvinsaputra.tvpulse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_shows")
data class HomeShowEntity(
    @PrimaryKey val showId: Long,
    val position: Int,
    val remotePage: Int,
)
