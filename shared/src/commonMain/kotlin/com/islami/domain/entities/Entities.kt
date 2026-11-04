package com.islami.domain.entities

import com.islami.core.time.currentTimeMillis
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis()
) {
    companion object {
        fun empty() = User(uid = "", email = null)
    }
}

@Serializable
data class FeedbackMessage(
    val id: String,
    @SerialName("app_version")
    val appVersion: String,
    val deviceName: String,
    val email: String,
    val phone: String,
    val date: String,
    val msg: String
)

@Serializable
data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val deepLink: String? = null,
    val sentAt: Long = currentTimeMillis(),
    val targetUsers: List<String>? = null,
    val targetDevices: List<String>? = null,
    val priority: String = "high",
    /** FCM topic to broadcast to; ignored when [fcmToken] is set. */
    val topic: String? = null,
    /** Single device token; takes priority over [topic]. */
    val fcmToken: String? = null,
    /** Islami app `RouteType` id the notification opens (0 = dashboard). */
    val routeId: Int = 0,
    /** Screen-specific id, e.g. an azkar category. */
    val extraRouteId: Int = 0,
    /** Devices on an older app version drop the notification. */
    val minVersion: String? = null
)
