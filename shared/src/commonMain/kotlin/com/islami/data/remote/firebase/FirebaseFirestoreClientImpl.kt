package com.islami.data.remote.firebase

import com.islami.core.error.Result
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.KSerializer

class FirebaseFirestoreClientImpl(
    private val firestore: FirebaseFirestore
) : FirebaseFirestoreClient {

    override suspend fun <T> getCollection(
        collection: String,
        serializer: KSerializer<T>
    ): Result<List<T>> = try {
        val snapshot = firestore.collection(collection).get()
        Result.Success(snapshot.documents.map { it.data(serializer) })
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun <T : Any> setDocument(
        collection: String,
        documentId: String,
        data: T,
        serializer: KSerializer<T>
    ): Result<Unit> = try {
        firestore.collection(collection).document(documentId).set(serializer, data)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
