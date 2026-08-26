package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class CanLoadMoreFavoritesUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    suspend operator fun invoke(visibleCount: Int): Boolean =
        repository.canLoadMoreFavorites(visibleCount)
}
