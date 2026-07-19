package com.islami.data.remote.http

import com.islami.core.error.Result
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * HTTP Client wrapper interface - platform-independent
 * Handles cross-platform API calls with error handling
 */
interface HttpClient {
    /**
     * Make GET request and deserialize to type T
     */
    suspend inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): Result<T>

    /**
     * Make POST request with JSON body and deserialize response
     */
    suspend inline fun <reified T> post(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): Result<T>

    /**
     * Make PUT request with JSON body and deserialize response
     */
    suspend inline fun <reified T> put(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): Result<T>

    /**
     * Make DELETE request
     */
    suspend inline fun <reified T> delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): Result<T>

    /**
     * Make raw HTTP request and get raw response
     */
    suspend fun getRaw(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): Result<HttpResponse>

    /**
     * Set default headers (e.g., auth token)
     */
    fun setDefaultHeader(key: String, value: String)

    /**
     * Add auth token to all requests
     */
    fun setAuthToken(token: String)

    /**
     * Clear auth token
     */
    fun clearAuthToken()
}

/**
 * Response wrapper for API responses
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    @SerialName("error_code")
    val errorCode: Int? = null,
    val error: String? = null
)

/**
 * Pagination wrapper
 */
@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val page: Int,
    @SerialName("per_page")
    val perPage: Int,
    val total: Int,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("has_next")
    val hasNext: Boolean
)
