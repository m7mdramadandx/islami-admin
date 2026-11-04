package com.islami.core.error

/**
 * Outcome of a data operation, returned by repositories and use cases.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

open class AuthException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)
