package com.islami.presentation.notifications

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.domain.entities.Notification
import com.islami.domain.usecases.notifications.SendNotificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.random.Random

class NotificationStateHolder(
    private val sendNotificationUseCase: SendNotificationUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<NotificationState>(NotificationState()) {

    /** Next body to use per template, so tapping a card again cycles its messages. */
    private val templateCursor = mutableMapOf<String, Int>()

    fun onTitleChanged(value: String) = edit(NotificationField.TITLE) { it.copy(title = value) }
    fun onBodyChanged(value: String) = edit(NotificationField.BODY) { it.copy(body = value) }
    fun onImageUrlChanged(value: String) = edit(NotificationField.IMAGE_URL) { it.copy(imageUrl = value) }
    fun onTopicChanged(value: String) = edit(NotificationField.TOPIC) { it.copy(topic = value) }
    fun onFcmTokenChanged(value: String) = edit(NotificationField.TOPIC) { it.copy(fcmToken = value) }
    fun onMinVersionChanged(value: String) = edit(NotificationField.MIN_VERSION) { it.copy(minVersion = value) }

    fun onExtraRouteIdChanged(value: String) {
        if (value.all { it.isDigit() }) edit(NotificationField.EXTRA_ROUTE_ID) { it.copy(extraRouteId = value) }
    }

    /** Picking a route pre-fills its suggested copy; the admin can still edit it afterwards. */
    fun onRouteSelected(route: NotificationRoute?) {
        updateState { state ->
            state.copy(
                route = route,
                title = route?.defaultTitle ?: state.title,
                body = route?.defaultBody ?: state.body,
                selectedTemplate = null,
                fieldErrors = if (route?.defaultTitle != null) {
                    state.fieldErrors - NotificationField.TITLE - NotificationField.BODY
                } else state.fieldErrors
            )
        }
    }

    fun applyTemplate(template: NotificationTemplate) {
        val index = templateCursor.getOrElse(template.key) { 0 }
        templateCursor[template.key] = (index + 1) % template.bodies.size
        fill(template, template.bodies[index])
    }

    fun applyRandomTemplate() {
        val current = currentState().selectedTemplate
        val pool = notificationTemplates.filter { it.key != current }.ifEmpty { notificationTemplates }
        val template = pool.random()
        fill(template, template.bodies.random())
    }

    fun clear() {
        templateCursor.clear()
        updateState { NotificationState(message = it.message) }
    }

    fun onMessageShown() = updateState { it.copy(message = null) }

    fun dismissBroadcast() = updateState { it.copy(confirmBroadcast = false) }

    /** Validates, then sends directly to a device or asks for confirmation before a topic broadcast. */
    fun requestSend() {
        val state = currentState()
        if (state.isLoading) return
        val errors = validate(state)
        if (errors.isNotEmpty()) {
            updateState { it.copy(fieldErrors = errors) }
            return
        }
        if (state.sendsToDevice) send() else updateState { it.copy(confirmBroadcast = true) }
    }

    fun confirmBroadcast() {
        updateState { it.copy(confirmBroadcast = false) }
        send()
    }

    private fun send() = scope.launch {
        val state = currentState()
        updateState { it.copy(isLoading = true) }

        val notification = Notification(
            id = Random.nextLong().toULong().toString(16),
            title = state.title.trim(),
            body = state.body.trim(),
            imageUrl = state.imageUrl.trim().ifBlank { null },
            topic = state.topic.trim().takeUnless { state.sendsToDevice || it.isBlank() },
            fcmToken = state.fcmToken.trim().ifBlank { null },
            routeId = state.route?.id ?: 0,
            extraRouteId = state.extraRouteId.toIntOrNull() ?: 0,
            minVersion = state.minVersion.trim().ifBlank { null }
        )

        when (val result = sendNotificationUseCase(notification)) {
            is Result.Success -> updateState {
                it.copy(isLoading = false, message = "Notification sent to ${state.targetLabel}")
            }
            is Result.Error -> updateState {
                it.copy(isLoading = false, message = "Failed to send: ${result.exception.readable()}")
            }
            is Result.Loading -> Unit
        }
    }

    private fun fill(template: NotificationTemplate, body: String) {
        updateState {
            it.copy(
                title = template.title,
                body = body,
                route = template.route,
                extraRouteId = template.extraRouteId.takeIf { id -> id != 0 }?.toString().orEmpty(),
                selectedTemplate = template.key,
                fieldErrors = it.fieldErrors - NotificationField.TITLE - NotificationField.BODY -
                    NotificationField.EXTRA_ROUTE_ID
            )
        }
    }

    private inline fun edit(field: NotificationField, crossinline block: (NotificationState) -> NotificationState) {
        updateState { block(it).copy(fieldErrors = it.fieldErrors - field) }
    }

    private fun validate(state: NotificationState): Map<NotificationField, String> = buildMap {
        if (state.title.isBlank()) put(NotificationField.TITLE, "Title is required")
        if (state.body.isBlank()) put(NotificationField.BODY, "Message is required")

        val imageUrl = state.imageUrl.trim()
        if (imageUrl.isNotEmpty() && !(imageUrl.startsWith("https://") || imageUrl.startsWith("http://"))) {
            put(NotificationField.IMAGE_URL, "Must be an http(s) URL")
        }

        if (!state.sendsToDevice) {
            val topic = state.topic.trim()
            when {
                topic.isEmpty() -> put(NotificationField.TOPIC, "Enter a topic or an FCM token")
                !TOPIC_PATTERN.matches(topic) -> put(NotificationField.TOPIC, "Letters, digits and -_.~% only")
            }
        }

        if (state.extraRouteId.isNotEmpty() && state.extraRouteId.toIntOrNull() == null) {
            put(NotificationField.EXTRA_ROUTE_ID, "Must be a number")
        }

        val minVersion = state.minVersion.trim()
        if (minVersion.isNotEmpty() && !VERSION_PATTERN.matches(minVersion)) {
            put(NotificationField.MIN_VERSION, "Use a version like 1.0.0")
        }
    }

    /** Callable errors carry the function's HttpsError message; drop its "FCM Error:" prefix. */
    private fun Exception.readable(): String =
        message?.takeIf { it.isNotBlank() }?.substringAfter("FCM Error: ") ?: "Unknown error"

    private companion object {
        val TOPIC_PATTERN = Regex("[a-zA-Z0-9\\-_.~%]+")
        val VERSION_PATTERN = Regex("\\d+(\\.\\d+)*")
    }
}
