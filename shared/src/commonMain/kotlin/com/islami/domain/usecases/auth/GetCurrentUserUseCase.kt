package com.islami.domain.usecases.auth

import com.islami.core.error.Result
import com.islami.core.usecase.FlowUseCase
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.User
import com.islami.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) : FlowUseCase<NoParams, User?>() {
    override fun invoke(params: NoParams): Flow<Result<User?>> {
        return authRepository.currentUser().map { Result.Success(it) }
    }
}
