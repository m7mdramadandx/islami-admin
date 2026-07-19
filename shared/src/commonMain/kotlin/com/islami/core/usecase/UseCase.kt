package com.islami.core.usecase

import com.islami.core.error.Result
import kotlinx.coroutines.flow.Flow

/**
 * Base class for synchronous use cases
 */
abstract class UseCase<in Params, out Type> {
    abstract suspend operator fun invoke(params: Params): Result<Type>
}

/**
 * Base class for streaming use cases
 */
abstract class FlowUseCase<in Params, out Type> {
    abstract operator fun invoke(params: Params): Flow<Result<Type>>
}

/**
 * Empty parameters for use cases that don't require any
 */
object NoParams
