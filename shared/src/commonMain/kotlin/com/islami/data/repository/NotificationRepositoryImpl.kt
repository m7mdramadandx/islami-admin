package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.Notification
import com.islami.domain.repositories.NotificationRepository
import kotlinx.coroutines.flow.Flow

class NotificationRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient
) : NotificationRepository {

    override suspend fun sendNotification(notification: Notification): Result<Unit> {
        // In a real app, this might trigger a Cloud Function
        // For now, we save it to a 'notifications' collection which triggers a FCM hook
        return firestoreClient.setDocument(
            collection = "notifications",
            documentId = notification.id,
            data = notification
        )
    }

    override suspend fun getAllNotifications(): Result<List<Notification>> {
        return firestoreClient.getCollection(
            collection = "notifications",
            serializer = Notification.serializer()
        )
    }

    override suspend fun deleteNotification(id: String): Result<Unit> {
        return firestoreClient.deleteDocument(
            collection = "notifications",
            documentId = id
        )
    }

    override fun observeNotifications(): Flow<Result<List<Notification>>> {
        return firestoreClient.observeCollection(
            collection = "notifications",
            serializer = Notification.serializer()
        )
    }
}
