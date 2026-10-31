package com.islami.presentation.notifications

const val DEFAULT_TOPIC = "general"

data class NotificationState(
    val title: String = "",
    val body: String = "",
    val imageUrl: String = "",
    val topic: String = DEFAULT_TOPIC,
    val fcmToken: String = "",
    val route: NotificationRoute? = null,
    val extraRouteId: String = "",
    val minVersion: String = "",
    /** Key of the last applied template, used to highlight its card. */
    val selectedTemplate: String? = null,
    val fieldErrors: Map<NotificationField, String> = emptyMap(),
    val isLoading: Boolean = false,
    /** Topic broadcast waiting for the admin to confirm. */
    val confirmBroadcast: Boolean = false,
    val message: String? = null
) {
    val sendsToDevice: Boolean get() = fcmToken.isNotBlank()

    val targetLabel: String
        get() = if (sendsToDevice) "a single device" else "topic \"${topic.trim()}\""

    val isEmpty: Boolean
        get() = this == NotificationState(message = message)
}

enum class NotificationField { TITLE, BODY, IMAGE_URL, TOPIC, EXTRA_ROUTE_ID, MIN_VERSION }
