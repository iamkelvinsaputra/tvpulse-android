package com.kelvinsaputra.tvpulse.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kelvinsaputra.tvpulse.data.local.dao.FavoriteShowDao
import com.kelvinsaputra.tvpulse.data.local.entity.FavoriteShowEntity

@Database(
    entities = [FavoriteShowEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class TVPulseDatabase : RoomDatabase() {
    abstract fun favoriteShowDao(): FavoriteShowDao
}
