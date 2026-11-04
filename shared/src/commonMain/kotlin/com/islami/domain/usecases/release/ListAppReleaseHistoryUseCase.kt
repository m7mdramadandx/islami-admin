package com.islami.domain.usecases.release

import com.islami.core.error.Result
import com.islami.core.usecase.NoParams
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.AppReleaseHistoryEntry
import com.islami.domain.repositories.AppReleaseRepository

class ListAppReleaseHistoryUseCase(
    private val appReleaseRepository: AppReleaseRepository
) : UseCase<NoParams, List<AppReleaseHistoryEntry>>() {
    override suspend fun invoke(params: NoParams): Result<List<AppReleaseHistoryEntry>> =
        appReleaseRepository.history()
}
