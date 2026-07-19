package com.islami.domain.usecases.auth

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.User
import com.islami.domain.repositories.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) : UseCase<LoginParams, User>() {
    override suspend fun invoke(params: LoginParams): Result<User> {
        return authRepository.login(params.email, params.password)
    }
}

data class LoginParams(val email: String, val password: String)
