package com.kelvinsaputra.tvpulse.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kelvinsaputra.tvpulse.data.local.dao.CacheMetadataDao
import com.kelvinsaputra.tvpulse.data.local.dao.CachedShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.FavoriteShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.HomeShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.SearchResultDao
import com.kelvinsaputra.tvpulse.data.local.entity.CacheMetadataEntity
import com.kelvinsaputra.tvpulse.data.local.entity.CachedShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.FavoriteShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.HomeShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.SearchResultEntity

@Database(
    entities = [
        FavoriteShowEntity::class,
        CachedShowEntity::class,
        HomeShowEntity::class,
        SearchResultEntity::class,
        CacheMetadataEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class TVPulseDatabase : RoomDatabase() {
    abstract fun favoriteShowDao(): FavoriteShowDao
    abstract fun cacheMetadataDao(): CacheMetadataDao
    abstract fun cachedShowDao(): CachedShowDao
    abstract fun homeShowDao(): HomeShowDao
    abstract fun searchResultDao(): SearchResultDao
}
