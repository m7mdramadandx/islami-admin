package com.islami.domain.usecases.azkar

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.Azkar
import com.islami.domain.repositories.AzkarRepository

class GetAzkarUseCase(
    private val azkarRepository: AzkarRepository
) : UseCase<NoParams, List<Azkar>>() {
    override suspend fun invoke(params: NoParams): Result<List<Azkar>> {
        return azkarRepository.getAzkar()
    }
}
