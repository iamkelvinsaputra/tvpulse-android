package com.kelvinsaputra.tvpulse.domain.usecase

import com.kelvinsaputra.tvpulse.domain.repository.TvShowRepository
import javax.inject.Inject

class HasHomeCacheUseCase @Inject constructor(
    private val repository: TvShowRepository,
) {
    suspend operator fun invoke(): Boolean = repository.hasHomeCache()
}
