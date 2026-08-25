package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class SearchShowsUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    suspend operator fun invoke(query: String): List<TvShow> = repository.searchShows(query)
}
