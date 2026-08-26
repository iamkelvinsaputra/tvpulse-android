package com.kelvinsaputra.tvpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kelvinsaputra.tvpulse.data.local.entity.FavoriteShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteShowDao {
    @Query("SELECT * FROM favorite_shows ORDER BY name COLLATE NOCASE ASC LIMIT :limit")
    fun observeAll(limit: Int): Flow<List<FavoriteShowEntity>>

    @Query("SELECT * FROM favorite_shows ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<FavoriteShowEntity>

    @Query("SELECT COUNT(*) FROM favorite_shows")
    suspend fun count(): Int

    @Query("SELECT * FROM favorite_shows WHERE id = :showId LIMIT 1")
    fun observeById(showId: Long): Flow<FavoriteShowEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_shows WHERE id = :showId)")
    fun observeIsFavorite(showId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_shows WHERE id = :showId)")
    suspend fun isFavorite(showId: Long): Boolean

    @Upsert
    suspend fun upsert(show: FavoriteShowEntity)

    @Upsert
    suspend fun upsertAll(shows: List<FavoriteShowEntity>)

    @Query("DELETE FROM favorite_shows WHERE id = :showId")
    suspend fun deleteById(showId: Long)
}
