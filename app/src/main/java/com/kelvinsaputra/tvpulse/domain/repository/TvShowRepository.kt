package com.kelvinsaputra.tvpulse.domain.repository

import com.kelvinsaputra.tvpulse.domain.model.TvShow

interface TvShowRepository {
    suspend fun getTopShows(): List<TvShow>
}