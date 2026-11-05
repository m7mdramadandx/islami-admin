package com.islami.domain.usecases.ramadan

import com.islami.core.error.Result
import com.islami.core.usecase.NoParams
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.RamadanBackground
import com.islami.domain.repositories.RamadanRepository

class ListRamadanBackgroundsUseCase(
    private val ramadanRepository: RamadanRepository
) : UseCase<NoParams, List<RamadanBackground>>() {
    override suspend fun invoke(params: NoParams): Result<List<RamadanBackground>> =
        ramadanRepository.listBackgrounds()
}
