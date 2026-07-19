package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.local.LocalDataSource
import com.islami.data.remote.firebase.FirebaseAuthClient
import com.islami.domain.entities.User
import com.islami.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class AuthRepositoryImpl(
    private val firebaseAuthClient: FirebaseAuthClient,
    private val localDataSource: LocalDataSource
) : AuthRepository {
    companion object {
        private const val KEY_USER = "current_user"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    override fun currentUser(): Flow<User?> = firebaseAuthClient.currentUser()
        .onEach { user ->
            if (user != null) {
                localDataSource.saveObject(KEY_USER, user)
            } else {
                localDataSource.delete(KEY_USER)
            }
        }

    override suspend fun login(email: String, password: String): Result<User> = try {
        val result = firebaseAuthClient.signIn(email, password)
        if (result is Result.Success) {
            localDataSource.saveObject(KEY_USER, result.data)
        }
        result
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun signup(email: String, password: String, displayName: String): Result<User> = try {
        val result = firebaseAuthClient.signUp(email, password)
        if (result is Result.Success) {
            // TODO: Update display name in Firebase if needed
            localDataSource.saveObject(KEY_USER, result.data)
        }
        result
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun logout(): Result<Unit> = try {
        val result = firebaseAuthClient.signOut()
        if (result is Result.Success) {
            localDataSource.delete(KEY_USER)
            localDataSource.delete(KEY_AUTH_TOKEN)
        }
        result
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun resetPassword(email: String): Result<Unit> =
        firebaseAuthClient.sendPasswordReset(email)

    override suspend fun getCurrentUser(): User? {
        val user = firebaseAuthClient.getCurrentUser()
        if (user != null) {
            localDataSource.saveObject(KEY_USER, user)
        }
        return user
    }

    suspend fun getCachedUser(): User? = try {
        localDataSource.getObject(KEY_USER)
    } catch (e: Exception) {
        null
    }

    suspend fun saveAuthToken(token: String) {
        localDataSource.saveString(KEY_AUTH_TOKEN, token)
    }

    suspend fun getAuthToken(): String? = try {
        localDataSource.getString(KEY_AUTH_TOKEN).takeIf { it.isNotEmpty() }
    } catch (e: Exception) {
        null
    }

    suspend fun clearAuth() {
        localDataSource.delete(KEY_USER)
        localDataSource.delete(KEY_AUTH_TOKEN)
    }
}
