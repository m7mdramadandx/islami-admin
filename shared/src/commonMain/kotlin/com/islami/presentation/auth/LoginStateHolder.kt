package com.islami.presentation.auth

import com.islami.core.error.Result
import com.islami.core.state.EventStateHolder
import com.islami.core.state.UiEvent
import com.islami.domain.usecases.auth.LoginParams
import com.islami.domain.usecases.auth.LoginUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class LoginStateHolder(
    private val loginUseCase: LoginUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : EventStateHolder<LoginState, UiEvent>(LoginState()) {

    fun onEmailChanged(email: String) {
        updateState { it.copy(email = email, error = null) }
    }

    fun login(password: String) = scope.launch {
        val currentEmail = currentState().email
        if (currentEmail.isBlank() || password.isBlank()) {
            updateState { it.copy(error = "Email and password are required") }
            return@launch
        }

        updateState { it.copy(isLoading = true, error = null) }

        val result = loginUseCase(LoginParams(currentEmail, password))

        updateState {
            when (result) {
                is Result.Success -> it.copy(isLoading = false, user = result.data)
                is Result.Error -> it.copy(isLoading = false, error = result.exception.message)
                is Result.Loading -> it // Should not happen with current UseCase pattern
            }
        }

        if (result is Result.Success) {
            emitEvent(UiEvent.Navigate("home"))
        }
    }
}
