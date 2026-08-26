package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class CanLoadMoreSearchShowsUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    suspend operator fun invoke(query: String, visibleCount: Int): Boolean =
        repository.canLoadMoreSearchShows(query, visibleCount)
}
