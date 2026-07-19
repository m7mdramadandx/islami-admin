package com.islami.core.error

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
