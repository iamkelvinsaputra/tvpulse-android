package com.kelvinsaputra.tvpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kelvinsaputra.tvpulse.data.local.entity.CacheMetadataEntity

@Dao
interface CacheMetadataDao {
    @Query("SELECT * FROM cache_metadata WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CacheMetadataEntity?

    @Upsert
    suspend fun upsert(metadata: CacheMetadataEntity)
}
