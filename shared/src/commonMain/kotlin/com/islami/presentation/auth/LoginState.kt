package com.islami.presentation.auth

import com.islami.domain.entities.User

data class LoginState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null
)
