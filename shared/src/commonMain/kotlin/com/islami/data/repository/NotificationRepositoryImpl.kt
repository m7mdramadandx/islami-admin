package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.Notification
import com.islami.domain.repositories.NotificationRepository
import dev.gitlive.firebase.functions.FirebaseFunctions
import kotlinx.serialization.Serializable
import kotlin.coroutines.cancellation.CancellationException

class NotificationRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient,
    private val functions: FirebaseFunctions
) : NotificationRepository {

    /**
     * Sends through the `sendFcmNotification` callable (functions/index.js), then records the
     * notification in the `notifications` collection. The history write is best effort: the push
     * has already gone out, so failing to log it must not be reported as a failed send.
     */
    override suspend fun sendNotification(notification: Notification): Result<Unit> {
        try {
            // Use the reified overload: in gitlive 2.7.0 invoke(strategy, data) re-encodes the
            // already-encoded value as `Any`, failing with "Serializer for class 'Any' is not found".
            functions.httpsCallable(SEND_FUNCTION)
                .invoke(SendFcmRequest(notification.toPayload()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.Error(e)
        }

        firestoreClient.setDocument(
            collection = COLLECTION,
            documentId = notification.id,
            data = notification,
            serializer = Notification.serializer()
        )
        return Result.Success(Unit)
    }

    private fun Notification.toPayload() = FcmPayload(
        title = title,
        body = body,
        imageUrl = imageUrl.orEmpty(),
        topic = topic.orEmpty(),
        fcmToken = fcmToken.orEmpty(),
        routeID = routeId.toString(),
        extraRouteID = extraRouteId.toString(),
        minVersion = minVersion.orEmpty()
    )

    private companion object {
        const val SEND_FUNCTION = "sendFcmNotification"
        const val COLLECTION = "notifications"
    }
}

/** The function reads `data.payload`; field names must match functions/index.js. */
@Serializable
private data class SendFcmRequest(val payload: FcmPayload)

@Serializable
private data class FcmPayload(
    val title: String,
    val body: String,
    val imageUrl: String,
    val topic: String,
    val fcmToken: String,
    val routeID: String,
    val extraRouteID: String,
    val minVersion: String
)
