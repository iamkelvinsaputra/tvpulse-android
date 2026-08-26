package com.kelvinsaputra.tvpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kelvinsaputra.tvpulse.data.local.entity.CachedShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.HomeShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeShowDao {
    @Query(
        """
        SELECT cached_shows.* FROM home_shows
        INNER JOIN cached_shows ON cached_shows.id = home_shows.showId
        ORDER BY home_shows.position ASC
        LIMIT :limit
        """
    )
    fun observeShows(limit: Int): Flow<List<CachedShowEntity>>

    @Query("SELECT COUNT(*) FROM home_shows")
    suspend fun count(): Int

    @Query("SELECT MAX(remotePage) FROM home_shows")
    suspend fun maxRemotePage(): Int?

    @Query("SELECT MAX(position) FROM home_shows")
    suspend fun maxPosition(): Int?

    @Upsert
    suspend fun upsertCachedShows(shows: List<CachedShowEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<HomeShowEntity>)

    @Query("DELETE FROM home_shows")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(
        shows: List<CachedShowEntity>,
        entries: List<HomeShowEntity>,
    ) {
        upsertCachedShows(shows)
        clear()
        insertAll(entries)
    }

    @Transaction
    suspend fun append(
        shows: List<CachedShowEntity>,
        entries: List<HomeShowEntity>,
    ) {
        upsertCachedShows(shows)
        insertAll(entries)
    }
}
