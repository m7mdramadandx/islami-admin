package com.islami.domain.usecases.home

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.HomeStats
import com.islami.domain.repositories.HomeRepository

class GetHomeStatsUseCase(
    private val homeRepository: HomeRepository
) : UseCase<NoParams, HomeStats>() {
    override suspend fun invoke(params: NoParams): Result<HomeStats> {
        return homeRepository.getHomeStats()
    }
}
