package com.islami.presentation.feedback

import com.islami.core.error.Result
import com.islami.core.state.EventStateHolder
import com.islami.core.state.UiEvent
import com.islami.domain.usecases.feedback.GetFeedbackMessagesUseCase
import com.islami.domain.usecases.feedback.RespondToFeedbackParams
import com.islami.domain.usecases.feedback.RespondToFeedbackUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class FeedbackStateHolder(
    private val getFeedbackMessagesUseCase: GetFeedbackMessagesUseCase,
    private val respondToFeedbackUseCase: RespondToFeedbackUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : EventStateHolder<FeedbackState, UiEvent>(FeedbackState()) {

    init {
        loadMessages()
    }

    fun loadMessages(limit: Int = 100) = scope.launch {
        updateState { it.copy(isLoading = true, error = null) }
        val result = getFeedbackMessagesUseCase(limit)
        updateState {
            when (result) {
                is Result.Success -> it.copy(isLoading = false, messages = result.data)
                is Result.Error -> it.copy(isLoading = false, error = result.exception.message)
                is Result.Loading -> it
            }
        }
    }

    fun respondToFeedback(id: String, response: String) = scope.launch {
        updateState { it.copy(isLoading = true) }
        val result = respondToFeedbackUseCase(RespondToFeedbackParams(id, response))
        updateState { it.copy(isLoading = false) }
        
        if (result is Result.Success) {
            emitEvent(UiEvent.ShowMessage("Response sent successfully"))
            loadMessages() // Refresh list
        } else if (result is Result.Error) {
            emitEvent(UiEvent.ShowError(result.exception.message ?: "Failed to send response"))
        }
    }
}
