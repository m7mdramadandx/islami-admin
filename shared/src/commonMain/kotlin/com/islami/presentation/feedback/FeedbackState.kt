package com.islami.presentation.feedback

import com.islami.domain.entities.FeedbackMessage

data class FeedbackState(
    val isLoading: Boolean = false,
    val messages: List<FeedbackMessage> = emptyList(),
    val error: String? = null
)
