package com.islami.domain.usecases.ramadan

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.RamadanBackground
import com.islami.domain.ramadan.backgroundUploadProblem
import com.islami.domain.ramadan.detectBackgroundType
import com.islami.domain.repositories.RamadanRepository

/** Takes the image file's bytes; returns the uploaded background, ready to assign to a day. */
class UploadRamadanBackgroundUseCase(
    private val ramadanRepository: RamadanRepository
) : UseCase<ByteArray, RamadanBackground>() {
    override suspend fun invoke(params: ByteArray): Result<RamadanBackground> {
        backgroundUploadProblem(params)?.let { return Result.Error(IllegalArgumentException(it)) }
        return ramadanRepository.uploadBackground(params, detectBackgroundType(params)!!)
    }
}
