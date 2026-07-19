package com.islami.data.remote.http

import android.util.Log
import com.islami.core.error.ErrorResponse
import com.islami.core.error.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

actual class HttpClientImpl : HttpClient {
    private val defaultHeaders = mutableMapOf<String, String>()
    private var authToken: String? = null

    private val httpClient = HttpClient(OkHttp) {
        // Timeout configuration
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000L
            connectTimeoutMillis = 60_000L
            socketTimeoutMillis = 60_000L
        }

        // JSON serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }

        // Logging in debug mode
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.BODY
        }

        // Configure OkHttp engine with interceptors
        engine {
            preconfigured = createOkHttpClient()
        }
    }

    private fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            // Add default headers
            defaultHeaders.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }

            // Add auth token if available
            authToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            val newRequest = requestBuilder.build()
            chain.proceed(newRequest)
        }
        .build()

    override suspend inline fun <reified T> get(
        url: String,
        headers: Map<String, String>
    ): Result<T> = try {
        val response: T = httpClient.get(url) {
            headers {
                defaultHeaders.forEach { (key, value) ->
                    append(key, value)
                }
                headers.forEach { (key, value) ->
                    append(key, value)
                }
            }
        }.body()
        Result.Success(response)
    } catch (e: Exception) {
        Log.e("HttpClient", "GET request failed: $url", e)
        handleError(e)
    }

    override suspend inline fun <reified T> post(
        url: String,
        body: Any,
        headers: Map<String, String>
    ): Result<T> = try {
        val response: T = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
            headers {
                defaultHeaders.forEach { (key, value) ->
                    append(key, value)
                }
                headers.forEach { (key, value) ->
                    append(key, value)
                }
            }
        }.body()
        Result.Success(response)
    } catch (e: Exception) {
        Log.e("HttpClient", "POST request failed: $url", e)
        handleError(e)
    }

    override suspend inline fun <reified T> put(
        url: String,
        body: Any,
        headers: Map<String, String>
    ): Result<T> = try {
        val response: T = httpClient.put(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
            headers {
                defaultHeaders.forEach { (key, value) ->
                    append(key, value)
                }
                headers.forEach { (key, value) ->
                    append(key, value)
                }
            }
        }.body()
        Result.Success(response)
    } catch (e: Exception) {
        Log.e("HttpClient", "PUT request failed: $url", e)
        handleError(e)
    }

    override suspend inline fun <reified T> delete(
        url: String,
        headers: Map<String, String>
    ): Result<T> = try {
        val response: T = httpClient.delete(url) {
            headers {
                defaultHeaders.forEach { (key, value) ->
                    append(key, value)
                }
                headers.forEach { (key, value) ->
                    append(key, value)
                }
            }
        }.body()
        Result.Success(response)
    } catch (e: Exception) {
        Log.e("HttpClient", "DELETE request failed: $url", e)
        handleError(e)
    }

    override suspend fun getRaw(
        url: String,
        headers: Map<String, String>
    ): Result<HttpResponse> = try {
        val response = httpClient.get(url) {
            headers {
                defaultHeaders.forEach { (key, value) ->
                    append(key, value)
                }
                headers.forEach { (key, value) ->
                    append(key, value)
                }
            }
        }
        Result.Success(response)
    } catch (e: Exception) {
        Log.e("HttpClient", "Raw GET request failed: $url", e)
        handleError(e)
    }

    override fun setDefaultHeader(key: String, value: String) {
        defaultHeaders[key] = value
    }

    override fun setAuthToken(token: String) {
        authToken = token
        setDefaultHeader("Authorization", "Bearer $token")
    }

    override fun clearAuthToken() {
        authToken = null
        defaultHeaders.remove("Authorization")
    }

    private suspend inline fun <reified T> handleError(e: Exception): Result<T> {
        val errorResponse = when {
            e.message?.contains("timeout", ignoreCase = true) == true -> {
                ErrorResponse(
                    code = 0,
                    message = "Request timeout"
                )
            }
            e.message?.contains("connection", ignoreCase = true) == true -> {
                ErrorResponse(
                    code = 0,
                    message = "Connection error"
                )
            }
            else -> {
                ErrorResponse(
                    code = -1,
                    message = e.message ?: "Unknown error"
                )
            }
        }
        return Result.Error(errorResponse)
    }
}
