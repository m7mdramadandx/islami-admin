package com.islami.data.remote.firebase

import com.islami.core.error.AuthException
import com.islami.core.error.Result
import com.islami.domain.entities.User
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAuthClientImpl(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthClient {

    override fun currentUser(): Flow<User?> = firebaseAuth.authStateChanged.map { it?.toUser() }

    override suspend fun signIn(email: String, password: String): Result<User> = try {
        val firebaseUser = firebaseAuth.signInWithEmailAndPassword(email, password).user
            ?: throw Exception("Sign in failed: no user returned")
        Result.Success(firebaseUser.toUser())
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.Error(AuthException("Invalid email or password", e))
    } catch (e: FirebaseAuthInvalidUserException) {
        Result.Error(AuthException("User not found or disabled", e))
    } catch (e: Exception) {
        Result.Error(AuthException("Sign in failed: ${e.message}", e))
    }

    override suspend fun signOut(): Result<Unit> = try {
        firebaseAuth.signOut()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AuthException("Sign out failed: ${e.message}", e))
    }

    private fun FirebaseUser.toUser() = User(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoURL,
        isEmailVerified = isEmailVerified
    )
}
