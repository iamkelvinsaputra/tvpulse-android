package com.kelvinsaputra.tvpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kelvinsaputra.tvpulse.data.local.entity.FavoriteShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteShowDao {
    @Query("SELECT * FROM favorite_shows ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FavoriteShowEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_shows WHERE id = :showId)")
    fun observeIsFavorite(showId: Long): Flow<Boolean>

    @Upsert
    suspend fun upsert(show: FavoriteShowEntity)

    @Query("DELETE FROM favorite_shows WHERE id = :showId")
    suspend fun deleteById(showId: Long)
}
