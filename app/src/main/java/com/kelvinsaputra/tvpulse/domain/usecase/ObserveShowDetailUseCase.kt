package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.model.TvShow
import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveShowDetailUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    operator fun invoke(showId: Long): Flow<TvShow?> =
        repository.observeShowDetail(showId)
}
