package com.islami.domain.usecases.quran

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.Ayah
import com.islami.domain.repositories.QuranRepository

class GetSurahAyahsUseCase(
    private val quranRepository: QuranRepository
) : UseCase<Int, List<Ayah>>() {
    override suspend fun invoke(params: Int): Result<List<Ayah>> {
        return quranRepository.getSurahAyahs(params)
    }
}
