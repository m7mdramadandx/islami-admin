package com.islami.data.remote.firebase

import android.util.Log
import com.islami.core.error.AuthException
import com.islami.core.error.Result
import com.islami.domain.entities.User
import dev.gitlive.firebase.auth.AuthErrorCode
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

actual class FirebaseAuthClientImpl(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthClient {
    private val currentUserChannel = Channel<User?>(capacity = Channel.UNLIMITED)

    init {
        // Listen to auth state changes
        firebaseAuth.authStateChanged.map { user ->
            user?.let {
                User(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoURL,
                    isEmailVerified = it.isEmailVerified
                )
            }
        }.let {  /* Subscribe in platform-specific way */ }
    }

    override fun currentUser(): Flow<User?> {
        return firebaseAuth.authStateChanged.map { firebaseUser ->
            firebaseUser?.let {
                User(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoURL,
                    isEmailVerified = it.isEmailVerified
                )
            }
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password)
        val firebaseUser = result.user ?: throw Exception("Sign in failed: no user returned")
        
        Result.Success(
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoURL,
                isEmailVerified = firebaseUser.isEmailVerified
            )
        )
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.Error(AuthException("Invalid email or password", e))
    } catch (e: FirebaseAuthInvalidUserException) {
        Result.Error(AuthException("User not found or disabled", e))
    } catch (e: Exception) {
        Log.e("FirebaseAuth", "Sign in error", e)
        Result.Error(AuthException("Sign in failed: ${e.message}", e))
    }

    override suspend fun signUp(email: String, password: String): Result<User> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password)
        val firebaseUser = result.user ?: throw Exception("Sign up failed: no user returned")
        
        Result.Success(
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                photoUrl = firebaseUser.photoURL,
                isEmailVerified = firebaseUser.isEmailVerified
            )
        )
    } catch (e: FirebaseAuthUserCollisionException) {
        Result.Error(AuthException("Email already in use", e))
    } catch (e: Exception) {
        Log.e("FirebaseAuth", "Sign up error", e)
        Result.Error(AuthException("Sign up failed: ${e.message}", e))
    }

    override suspend fun signOut(): Result<Unit> = try {
        firebaseAuth.signOut()
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseAuth", "Sign out error", e)
        Result.Error(AuthException("Sign out failed: ${e.message}", e))
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = try {
        firebaseAuth.sendPasswordResetEmail(email)
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseAuth", "Password reset error", e)
        Result.Error(AuthException("Password reset failed: ${e.message}", e))
    }

    override suspend fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let {
            User(
                uid = it.uid,
                email = it.email,
                displayName = it.displayName,
                photoUrl = it.photoURL,
                isEmailVerified = it.isEmailVerified
            )
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> = try {
        firebaseAuth.currentUser?.sendEmailVerification()
        Result.Success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseAuth", "Email verification error", e)
        Result.Error(AuthException("Email verification failed: ${e.message}", e))
    }

    override suspend fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
