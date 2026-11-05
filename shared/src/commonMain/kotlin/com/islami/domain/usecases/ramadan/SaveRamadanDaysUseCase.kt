package com.islami.domain.usecases.ramadan

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.RamadanDay
import com.islami.domain.ramadan.changedFrom
import com.islami.domain.ramadan.validate
import com.islami.domain.repositories.RamadanRepository

class SaveRamadanDaysUseCase(
    private val ramadanRepository: RamadanRepository
) : UseCase<SaveRamadanDaysParams, List<RamadanDay>>() {
    override suspend fun invoke(params: SaveRamadanDaysParams): Result<List<RamadanDay>> {
        // An empty dua means an empty screen in the app on that day of Ramadan.
        val errors = params.days.validate().filter { it.isError }
        if (errors.isNotEmpty()) return Result.Error(IllegalStateException(errors.first().message))

        val changedIds = params.days.changedFrom(params.base)
        if (changedIds.isEmpty()) return Result.Success(params.base)
        return ramadanRepository.saveDays(params.days.filter { it.id in changedIds }, params.base, params.overwrite)
    }
}
