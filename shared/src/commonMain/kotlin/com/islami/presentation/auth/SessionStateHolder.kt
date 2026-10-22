package com.islami.presentation.auth

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.User
import com.islami.domain.usecases.auth.GetCurrentUserUseCase
import com.islami.domain.usecases.auth.LogoutUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

sealed class SessionState {
    /** Firebase is still restoring the persisted session. */
    data object Loading : SessionState()
    data class SignedIn(val user: User) : SessionState()
    data object SignedOut : SessionState()
}

/**
 * Single source of truth for whether an admin is signed in.
 * Firebase Auth persists the session itself, so this only mirrors its auth state.
 */
class SessionStateHolder(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<SessionState>(SessionState.Loading) {

    init {
        scope.launch {
            getCurrentUserUseCase(NoParams).collect { result ->
                val user = (result as? Result.Success)?.data
                setState(if (user != null) SessionState.SignedIn(user) else SessionState.SignedOut)
            }
        }
    }

    fun logout() {
        scope.launch { logoutUseCase(NoParams) }
    }
}
