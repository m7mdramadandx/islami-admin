package com.islami.presentation.feedback

import com.islami.domain.entities.FeedbackMessage

data class FeedbackState(
    val isLoading: Boolean = true,
    val messages: List<FeedbackMessage> = emptyList(),
    val query: String = "",
    val sort: FeedbackSort = FeedbackSort.NEWEST,
    /** Message awaiting delete confirmation. */
    val pendingDelete: FeedbackMessage? = null,
    val deletingIds: Set<String> = emptySet(),
    val error: String? = null,
    /** One-shot snackbar text; cleared via [FeedbackStateHolder.onMessageShown]. */
    val message: String? = null
)
