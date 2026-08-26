package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIsFavoriteUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    operator fun invoke(showId: Long): Flow<Boolean> = repository.observeIsFavorite(showId)
}
