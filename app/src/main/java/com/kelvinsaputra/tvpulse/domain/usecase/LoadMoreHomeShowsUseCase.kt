package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class LoadMoreHomeShowsUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    suspend operator fun invoke(visibleCount: Int): Boolean =
        repository.loadMoreHomeShows(visibleCount)
}
