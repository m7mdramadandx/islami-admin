package com.islami.domain.usecases.quran

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.Surah
import com.islami.domain.repositories.QuranRepository

class GetAllSurahsUseCase(
    private val quranRepository: QuranRepository
) : UseCase<NoParams, List<Surah>>() {
    override suspend fun invoke(params: NoParams): Result<List<Surah>> {
        return quranRepository.getAllSurahs()
    }
}
