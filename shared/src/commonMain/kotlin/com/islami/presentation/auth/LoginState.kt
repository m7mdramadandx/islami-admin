package com.islami.presentation.auth

data class LoginState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
