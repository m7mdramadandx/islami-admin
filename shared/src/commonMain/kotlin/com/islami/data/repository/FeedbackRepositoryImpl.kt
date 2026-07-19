package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.FeedbackMessage
import com.islami.domain.repositories.FeedbackRepository

class FeedbackRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient
) : FeedbackRepository {

    override suspend fun getFeedbackMessages(limit: Int): Result<List<FeedbackMessage>> {
        return firestoreClient.getCollection(
            collection = "feedback",
            serializer = FeedbackMessage.serializer()
        )
    }

    override suspend fun getFeedbackMessage(id: String): Result<FeedbackMessage> {
        return firestoreClient.getDocument(
            collection = "feedback",
            documentId = id,
            serializer = FeedbackMessage.serializer()
        )
    }

    override suspend fun deleteFeedbackMessage(id: String): Result<Unit> {
        return firestoreClient.deleteDocument(
            collection = "feedback",
            documentId = id
        )
    }

    override suspend fun respondToFeedback(id: String, response: String): Result<Unit> {
        return firestoreClient.updateDocument(
            collection = "feedback",
            documentId = id,
            data = mapOf("adminResponse" to response, "respondedAt" to System.currentTimeMillis())
        )
    }
}
