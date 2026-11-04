package com.islami.domain.usecases.release

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.AppReleaseSnapshot
import com.islami.domain.release.normalized
import com.islami.domain.release.validate
import com.islami.domain.repositories.AppReleaseRepository

class SaveAppReleaseUseCase(
    private val appReleaseRepository: AppReleaseRepository
) : UseCase<SaveAppReleaseParams, AppReleaseSnapshot>() {
    override suspend fun invoke(params: SaveAppReleaseParams): Result<AppReleaseSnapshot> {
        val config = params.config.normalized()
        // A bad version can lock every user out of the app, so never let one through.
        val errors = config.validate(params.base).filter { it.isError }
        if (errors.isNotEmpty()) {
            return Result.Error(IllegalStateException(errors.joinToString("; ") { it.message }))
        }
        return appReleaseRepository.save(config, params.base, params.overwrite)
    }
}
