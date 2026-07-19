package com.islami.data.repository

import com.islami.core.error.ErrorResponse
import com.islami.core.error.Result
import com.islami.data.local.LocalDataSource
import com.islami.data.remote.firebase.FirebaseAuthClient
import com.islami.domain.entities.User
import com.islami.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val firebaseAuthClient: FirebaseAuthClient,
    private val localDataSource: LocalDataSource
) : AuthRepository {
    companion object {
        private const val KEY_USER = "current_user"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    override fun currentUser(): Flow<Result<User?>> = firebaseAuthClient.currentUser()
        .map { user ->
            if (user != null) {
                localDataSource.saveObject(KEY_USER, user)
                Result.Success(user)
            } else {
                Result.Success(null)
            }
        }
        .catch { e ->
            emit(Result.Error(ErrorResponse(message = "Failed to get current user: ${e.message}")))
        }

    override suspend fun signIn(email: String, password: String): Result<User> = try {
        val result = firebaseAuthClient.signIn(email, password)
        when (result) {
            is Result.Success -> {
                localDataSource.saveObject(KEY_USER, result.responseData)
                result
            }
            is Result.Error -> result
        }
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Sign in failed: ${e.message}"))
    }

    override suspend fun signUp(email: String, password: String): Result<User> = try {
        val result = firebaseAuthClient.signUp(email, password)
        when (result) {
            is Result.Success -> {
                localDataSource.saveObject(KEY_USER, result.responseData)
                result
            }
            is Result.Error -> result
        }
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Sign up failed: ${e.message}"))
    }

    override suspend fun signOut(): Result<Unit> = try {
        val result = firebaseAuthClient.signOut()
        when (result) {
            is Result.Success -> {
                localDataSource.delete(KEY_USER)
                localDataSource.delete(KEY_AUTH_TOKEN)
                result
            }
            is Result.Error -> result
        }
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Sign out failed: ${e.message}"))
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        firebaseAuthClient.sendPasswordReset(email)

    override suspend fun sendEmailVerification(): Result<Unit> =
        firebaseAuthClient.sendEmailVerification()

    override suspend fun isAuthenticated(): Boolean {
        val user = try {
            var result: User? = null
            firebaseAuthClient.currentUser()
                .catch { }
                .collect { result = it }
            result
        } catch (e: Exception) {
            null
        }
        return user != null
    }

    override suspend fun getCachedUser(): User? = try {
        localDataSource.getObject(KEY_USER)
    } catch (e: Exception) {
        null
    }

    override suspend fun saveAuthToken(token: String) {
        localDataSource.saveString(KEY_AUTH_TOKEN, token)
    }

    override suspend fun getAuthToken(): String? = try {
        localDataSource.getString(KEY_AUTH_TOKEN).takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }

    override suspend fun clearAuth() {
        localDataSource.delete(KEY_USER)
        localDataSource.delete(KEY_AUTH_TOKEN)
    }
}
