package com.islami.presentation.auth

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.domain.usecases.auth.LoginParams
import com.islami.domain.usecases.auth.LoginUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LoginStateHolder(
    private val loginUseCase: LoginUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<LoginState>(LoginState()) {

    fun onEmailChanged(email: String) {
        updateState { it.copy(email = email, error = null) }
    }

    /** The password itself stays in the UI; editing it just clears a stale error. */
    fun onPasswordChanged() {
        if (currentState().error != null) updateState { it.copy(error = null) }
    }

    fun login(password: String) = scope.launch {
        val currentEmail = currentState().email.trim()
        if (currentEmail.isBlank() || password.isBlank()) {
            updateState { it.copy(error = "Enter your email and password") }
            return@launch
        }

        updateState { it.copy(email = currentEmail, isLoading = true, error = null) }

        val result = loginUseCase(LoginParams(currentEmail, password))

        updateState {
            when (result) {
                // SessionStateHolder sees the new session and moves the app to Home.
                is Result.Success -> it.copy(isLoading = false)
                is Result.Error -> it.copy(isLoading = false, error = result.exception.toLoginMessage())
                is Result.Loading -> it // Should not happen with current UseCase pattern
            }
        }
    }

    /** Firebase's raw messages ("Firebase: Error (auth/invalid-credential).") aren't meant for people. */
    private fun Exception.toLoginMessage(): String {
        val text = listOfNotNull(message, cause?.message).joinToString(" ").lowercase()
        return when {
            listOf("invalid-credential", "wrong-password", "user-not-found", "invalid email or password", "invalid-login", "user not found or disabled")
                .any { it in text } -> "Incorrect email or password"
            "invalid-email" in text || "badly formatted" in text -> "That doesn't look like a valid email address"
            "user-disabled" in text -> "This account has been disabled"
            "too-many-requests" in text -> "Too many attempts. Wait a few minutes and try again"
            "network" in text -> "Can't reach the server. Check your connection"
            else -> message?.removePrefix("Sign in failed: ")?.takeIf { it.isNotBlank() } ?: "Sign in failed"
        }
    }
}
