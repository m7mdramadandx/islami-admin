package com.islami.domain.usecases.home

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.AppSettings
import com.islami.domain.repositories.HomeRepository

class GetAppSettingsUseCase(
    private val homeRepository: HomeRepository
) : UseCase<NoParams, AppSettings>() {
    override suspend fun invoke(params: NoParams): Result<AppSettings> {
        return homeRepository.getAppSettings()
    }
}
