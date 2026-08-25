package com.kelvinsaputra.tvpulse.data.repository

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import jakarta.inject.Inject

class FakeTvShowRepository @Inject constructor() : TvShowRepository {

    override suspend fun getTopShows(): List<TvShow> {
        return listOf(
            TvShow(
                id = 1,
                name = "Under the Dome",
                imageUrl = null,
            ),
            TvShow(
                id = 2,
                name = "Person of Interest",
                imageUrl = null,
            ),
        )
    }
}