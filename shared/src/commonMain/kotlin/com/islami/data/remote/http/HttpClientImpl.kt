package com.islami.data.remote.http

import com.islami.core.error.Result
import com.islami.core.serialization.jsonConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.KSerializer

class HttpClientImpl(
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
) : com.islami.data.remote.http.HttpClient {

    private val defaultHeaders = mutableMapOf<String, String>()
    private var authToken: String? = null

    override suspend fun <T> get(
        url: String,
        serializer: KSerializer<T>,
        headers: Map<String, String>
    ): Result<T> = try {
        val response = client.get(url) {
            headers {
                headers.forEach { (k, v) -> append(k, v) }
                defaultHeaders.forEach { (k, v) -> append(k, v) }
                authToken?.let { append("Authorization", "Bearer $it") }
            }
        }
        Result.Success(jsonConfig.decodeFromString(serializer, response.body<String>()))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun <T> post(
        url: String,
        body: Any,
        serializer: KSerializer<T>,
        headers: Map<String, String>
    ): Result<T> = try {
        val response = client.post(url) {
            setBody(body)
            headers {
                headers.forEach { (k, v) -> append(k, v) }
                defaultHeaders.forEach { (k, v) -> append(k, v) }
                authToken?.let { append("Authorization", "Bearer $it") }
            }
        }
        Result.Success(jsonConfig.decodeFromString(serializer, response.body<String>()))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun <T> put(
        url: String,
        body: Any,
        serializer: KSerializer<T>,
        headers: Map<String, String>
    ): Result<T> = try {
        val response = client.put(url) {
            setBody(body)
            headers {
                headers.forEach { (k, v) -> append(k, v) }
                defaultHeaders.forEach { (k, v) -> append(k, v) }
                authToken?.let { append("Authorization", "Bearer $it") }
            }
        }
        Result.Success(jsonConfig.decodeFromString(serializer, response.body<String>()))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun <T> delete(
        url: String,
        serializer: KSerializer<T>,
        headers: Map<String, String>
    ): Result<T> = try {
        val response = client.delete(url) {
            headers {
                headers.forEach { (k, v) -> append(k, v) }
                defaultHeaders.forEach { (k, v) -> append(k, v) }
                authToken?.let { append("Authorization", "Bearer $it") }
            }
        }
        Result.Success(jsonConfig.decodeFromString(serializer, response.body<String>()))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getRaw(url: String, headers: Map<String, String>): Result<HttpResponse> = try {
        val response = client.get(url) {
            headers {
                headers.forEach { (k, v) -> append(k, v) }
                defaultHeaders.forEach { (k, v) -> append(k, v) }
                authToken?.let { append("Authorization", "Bearer $it") }
            }
        }
        Result.Success(response)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override fun setDefaultHeader(key: String, value: String) {
        defaultHeaders[key] = value
    }

    override fun setAuthToken(token: String) {
        authToken = token
    }

    override fun clearAuthToken() {
        authToken = null
    }
}
