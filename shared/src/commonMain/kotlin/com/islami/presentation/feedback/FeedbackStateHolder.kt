package com.islami.presentation.feedback

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.FeedbackMessage
import com.islami.domain.usecases.feedback.DeleteFeedbackMessageUseCase
import com.islami.domain.usecases.feedback.ObserveFeedbackMessagesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FeedbackStateHolder(
    private val observeFeedbackMessagesUseCase: ObserveFeedbackMessagesUseCase,
    private val deleteFeedbackMessageUseCase: DeleteFeedbackMessageUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<FeedbackState>(FeedbackState()) {

    init {
        // Live listener: deletions (here or elsewhere) update the list without a refresh.
        scope.launch {
            observeFeedbackMessagesUseCase(NoParams).collect { result ->
                updateState {
                    when (result) {
                        is Result.Success -> it.copy(isLoading = false, messages = result.data, error = null)
                        is Result.Error -> it.copy(isLoading = false, error = result.exception.message ?: "Failed to load feedback")
                        is Result.Loading -> it
                    }
                }
            }
        }
    }

    fun onQueryChange(query: String) = updateState { it.copy(query = query) }

    fun onSortChange(sort: FeedbackSort) = updateState { it.copy(sort = sort) }

    fun requestDelete(message: FeedbackMessage) = updateState { it.copy(pendingDelete = message) }

    fun cancelDelete() = updateState { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val target = currentState().pendingDelete ?: return
        updateState { it.copy(pendingDelete = null, deletingIds = it.deletingIds + target.id) }
        scope.launch {
            val result = deleteFeedbackMessageUseCase(target.id)
            updateState {
                it.copy(
                    deletingIds = it.deletingIds - target.id,
                    message = when (result) {
                        is Result.Success -> "Feedback deleted"
                        is Result.Error -> "Delete failed: ${result.exception.message}"
                        is Result.Loading -> null
                    }
                )
            }
        }
    }

    fun onMessageShown() = updateState { it.copy(message = null) }
}
