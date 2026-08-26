package com.kelvinsaputra.tvpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kelvinsaputra.tvpulse.data.local.entity.CachedShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedShowDao {
    @Query("SELECT * FROM cached_shows WHERE id = :showId LIMIT 1")
    fun observeById(showId: Long): Flow<CachedShowEntity?>

    @Query("SELECT * FROM cached_shows WHERE id = :showId LIMIT 1")
    suspend fun getById(showId: Long): CachedShowEntity?

    @Upsert
    suspend fun upsert(show: CachedShowEntity)

    @Upsert
    suspend fun upsertAll(shows: List<CachedShowEntity>)
}
