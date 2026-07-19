package com.islami.domain.usecases.auth

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.core.usecase.NoParams
import com.islami.domain.repositories.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) : UseCase<NoParams, Unit>() {
    override suspend fun invoke(params: NoParams): Result<Unit> {
        return authRepository.logout()
    }
}
