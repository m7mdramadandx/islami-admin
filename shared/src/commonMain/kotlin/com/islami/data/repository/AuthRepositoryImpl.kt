package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseAuthClient
import com.islami.domain.entities.User
import com.islami.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow

/** Firebase Auth persists the session itself, so there is nothing to cache locally. */
class AuthRepositoryImpl(
    private val firebaseAuthClient: FirebaseAuthClient
) : AuthRepository {

    override fun currentUser(): Flow<User?> = firebaseAuthClient.currentUser()

    override suspend fun login(email: String, password: String): Result<User> = try {
        firebaseAuthClient.signIn(email, password)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun logout(): Result<Unit> = firebaseAuthClient.signOut()
}
