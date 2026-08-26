package com.kelvinsaputra.tvpulse.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kelvinsaputra.tvpulse.data.local.dao.CacheMetadataDao
import com.kelvinsaputra.tvpulse.data.local.dao.CachedShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.FavoriteShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.HomeShowDao
import com.kelvinsaputra.tvpulse.data.local.dao.SearchResultDao
import com.kelvinsaputra.tvpulse.data.local.database.TVPulseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): TVPulseDatabase = Room.databaseBuilder(
        context,
        TVPulseDatabase::class.java,
        DATABASE_NAME,
    )
        .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    fun provideCacheMetadataDao(database: TVPulseDatabase): CacheMetadataDao =
        database.cacheMetadataDao()

    @Provides
    fun provideFavoriteShowDao(database: TVPulseDatabase): FavoriteShowDao =
        database.favoriteShowDao()

    @Provides
    fun provideCachedShowDao(database: TVPulseDatabase): CachedShowDao =
        database.cachedShowDao()

    @Provides
    fun provideHomeShowDao(database: TVPulseDatabase): HomeShowDao =
        database.homeShowDao()

    @Provides
    fun provideSearchResultDao(database: TVPulseDatabase): SearchResultDao =
        database.searchResultDao()

    private const val DATABASE_NAME = "tvpulse.db"

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE favorite_shows ADD COLUMN runtime INTEGER")
            db.execSQL("ALTER TABLE favorite_shows ADD COLUMN status TEXT")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS cached_shows (
                    id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    imageUrl TEXT,
                    summaryHtml TEXT,
                    rating REAL,
                    genres TEXT NOT NULL,
                    schedule TEXT,
                    network TEXT,
                    premiered TEXT,
                    language TEXT,
                    runtime INTEGER,
                    status TEXT,
                    updatedAtEpochMillis INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS home_shows (
                    showId INTEGER NOT NULL,
                    position INTEGER NOT NULL,
                    remotePage INTEGER NOT NULL,
                    PRIMARY KEY(showId)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS search_results (
                    query TEXT NOT NULL,
                    showId INTEGER NOT NULL,
                    position INTEGER NOT NULL,
                    PRIMARY KEY(query, showId)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS cache_metadata (
                    `key` TEXT NOT NULL,
                    lastSyncedAtEpochMillis INTEGER NOT NULL,
                    resultCount INTEGER NOT NULL,
                    PRIMARY KEY(`key`)
                )
                """.trimIndent()
            )
        }
    }
}
