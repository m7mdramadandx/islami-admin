package com.islami.domain.usecases.release

import com.islami.core.error.Result
import com.islami.core.usecase.NoParams
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.AppReleaseSnapshot
import com.islami.domain.repositories.AppReleaseRepository

class LoadAppReleaseUseCase(
    private val appReleaseRepository: AppReleaseRepository
) : UseCase<NoParams, AppReleaseSnapshot>() {
    override suspend fun invoke(params: NoParams): Result<AppReleaseSnapshot> = appReleaseRepository.load()
}
