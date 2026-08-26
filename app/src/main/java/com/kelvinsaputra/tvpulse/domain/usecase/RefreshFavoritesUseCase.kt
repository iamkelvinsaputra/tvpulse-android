package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class RefreshFavoritesUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    suspend operator fun invoke() {
        repository.refreshFavorites()
    }
}
