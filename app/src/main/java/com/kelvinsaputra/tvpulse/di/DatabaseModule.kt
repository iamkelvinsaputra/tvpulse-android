package com.kelvinsaputra.tvpulse.di

import android.content.Context
import androidx.room.Room
import com.kelvinsaputra.tvpulse.data.local.dao.FavoriteShowDao
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
    ).build()

    @Provides
    fun provideFavoriteShowDao(database: TVPulseDatabase): FavoriteShowDao =
        database.favoriteShowDao()

    private const val DATABASE_NAME = "tvpulse.db"
}
