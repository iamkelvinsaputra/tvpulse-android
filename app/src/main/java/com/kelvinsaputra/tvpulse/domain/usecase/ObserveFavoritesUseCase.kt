package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoritesUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    operator fun invoke(limit: Int = Int.MAX_VALUE): Flow<List<TvShow>> =
        repository.observeFavorites(limit)
}
