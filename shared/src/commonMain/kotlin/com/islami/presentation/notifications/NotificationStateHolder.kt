package com.islami.presentation.notifications

import com.islami.core.error.Result
import com.islami.core.state.EventStateHolder
import com.islami.core.state.UiEvent
import com.islami.domain.entities.Notification
import com.islami.domain.usecases.notifications.SendNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlin.random.Random

class NotificationStateHolder(
    private val sendNotificationUseCase: SendNotificationUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : EventStateHolder<NotificationState, UiEvent>(NotificationState()) {

    fun onTitleChanged(title: String) {
        updateState { it.copy(title = title) }
    }

    fun onBodyChanged(body: String) {
        updateState { it.copy(body = body) }
    }

    fun sendNotification() = scope.launch {
        val title = currentState().title
        val body = currentState().body

        if (title.isBlank() || body.isBlank()) {
            updateState { it.copy(error = "Title and body are required") }
            return@launch
        }

        updateState { it.copy(isLoading = true, error = null) }

        val notification = Notification(
            id = Random.nextInt().toString(),
            title = title,
            body = body
        )

        val result = sendNotificationUseCase(notification)

        updateState {
            when (result) {
                is Result.Success -> it.copy(isLoading = false, title = "", body = "")
                is Result.Error -> it.copy(isLoading = false, error = result.exception.message)
                is Result.Loading -> it
            }
        }

        if (result is Result.Success) {
            emitEvent(UiEvent.ShowMessage("Notification sent successfully"))
        }
    }
}
