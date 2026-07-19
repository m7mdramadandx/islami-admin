package com.islami.core.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Sealed class for API responses - mirrors Islami app's DataState pattern
 * Provides a single source of truth for async data states
 */
sealed class DataState<out T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val errorResponse: ErrorResponse? = null
) {
    data object Idle : DataState<Nothing>(false)
    
    class Loading<T>(val cachedData: T? = null) : DataState<T>(true, cachedData)
    
    data class Error<T>(val e: ErrorResponse) : DataState<T>(false, errorResponse = e)
    
    data class Success<T>(val responseData: T) : DataState<T>(false, data = responseData)

    fun getOrNull(): T? = data

    fun exceptionOrNull(): ErrorResponse? = errorResponse

    fun fold(
        onSuccess: (T) -> Unit,
        onError: (ErrorResponse) -> Unit = {},
        onLoading: () -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(responseData)
            is Error -> onError(e)
            is Loading -> onLoading()
            is Idle -> onLoading()
        }
    }
}

/**
 * Kotlin Multiplatform Result type - simpler than DataState, for direct operations
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
        is Loading -> null
    }

    fun exceptionOrNull(): Exception? = when (this) {
        is Error -> exception
        else -> null
    }

    fun fold(
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit = {},
        onLoading: () -> Unit = {}
    ) {
        when (this) {
            is Success -> onSuccess(data)
            is Error -> onError(exception)
            is Loading -> onLoading()
        }
    }
}

/**
 * API Error Response - mirrors Islami app's ErrorResponse
 * Represents standardized API error responses
 */
@Serializable
data class ErrorResponse(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("error")
    val error: String? = null
) : Exception(message ?: error ?: "Unknown error")

/**
 * Sealed class for different error types
 */
sealed class Failure {
    data object Unknown : Failure()
    data object NetworkError : Failure()
    data class ServerError(val code: Int, val message: String) : Failure()
    data class ParseError(val message: String) : Failure()
    data object AuthenticationError : Failure()
    data object AuthorizationError : Failure()
}

fun Exception.toFailure(): Failure = when (this) {
    is NetworkException -> Failure.NetworkError
    is ServerException -> Failure.ServerError(this.code, this.message ?: "Unknown error")
    is ParseException -> Failure.ParseError(this.message ?: "Parse error")
    is AuthException -> Failure.AuthenticationError
    else -> Failure.Unknown
}

open class NetworkException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
open class ServerException(val code: Int, message: String? = null, cause: Throwable? = null) : Exception(message, cause)
open class ParseException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
open class AuthException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
