package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class LoadMoreSearchShowsUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    suspend operator fun invoke(query: String, targetCount: Int): Boolean =
        repository.loadMoreSearchShows(query, targetCount)
}
