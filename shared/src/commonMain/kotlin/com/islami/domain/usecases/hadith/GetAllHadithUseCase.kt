package com.islami.domain.usecases.hadith

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.Hadith
import com.islami.domain.repositories.HadithRepository

class GetAllHadithUseCase(
    private val hadithRepository: HadithRepository
) : UseCase<NoParams, List<Hadith>>() {
    override suspend fun invoke(params: NoParams): Result<List<Hadith>> {
        return hadithRepository.getAllHadith()
    }
}
