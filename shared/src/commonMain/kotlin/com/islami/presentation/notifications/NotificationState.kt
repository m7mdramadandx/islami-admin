package com.islami.presentation.notifications

data class NotificationState(
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
