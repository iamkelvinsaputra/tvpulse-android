package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class GetTopShowsUseCase @Inject constructor(private val repository: TvShowRepository) {
    suspend operator fun invoke(): List<TvShow> {
        return repository.getTopShows()
    }
}