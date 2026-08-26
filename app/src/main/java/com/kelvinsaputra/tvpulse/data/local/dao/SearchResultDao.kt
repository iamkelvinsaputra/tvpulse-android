package com.kelvinsaputra.tvpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.kelvinsaputra.tvpulse.data.local.entity.CachedShowEntity
import com.kelvinsaputra.tvpulse.data.local.entity.SearchResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchResultDao {
    @Query(
        """
        SELECT cached_shows.* FROM search_results
        INNER JOIN cached_shows ON cached_shows.id = search_results.showId
        WHERE search_results.query = :query
        ORDER BY search_results.position ASC
        LIMIT :limit
        """
    )
    fun observeShows(query: String, limit: Int): Flow<List<CachedShowEntity>>

    @Upsert
    suspend fun upsertCachedShows(shows: List<CachedShowEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SearchResultEntity>)

    @Query("DELETE FROM search_results WHERE query = :query")
    suspend fun clearQuery(query: String)

    @Transaction
    suspend fun replaceResults(
        query: String,
        shows: List<CachedShowEntity>,
        entries: List<SearchResultEntity>,
    ) {
        upsertCachedShows(shows)
        clearQuery(query)
        insertAll(entries)
    }
}
