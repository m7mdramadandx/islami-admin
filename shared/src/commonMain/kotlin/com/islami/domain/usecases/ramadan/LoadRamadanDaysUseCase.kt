package com.islami.domain.usecases.ramadan

import com.islami.core.error.Result
import com.islami.core.usecase.NoParams
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.RamadanDay
import com.islami.domain.repositories.RamadanRepository

class LoadRamadanDaysUseCase(
    private val ramadanRepository: RamadanRepository
) : UseCase<NoParams, List<RamadanDay>>() {
    override suspend fun invoke(params: NoParams): Result<List<RamadanDay>> = ramadanRepository.loadDays()
}
