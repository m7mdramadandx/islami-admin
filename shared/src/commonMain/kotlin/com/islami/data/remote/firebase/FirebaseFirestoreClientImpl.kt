package com.islami.data.remote.firebase

import com.islami.core.error.Result
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer

class FirebaseFirestoreClientImpl(
    private val firestore: FirebaseFirestore
) : FirebaseFirestoreClient {

    override suspend fun <T> getDocument(
        collection: String,
        documentId: String,
        serializer: KSerializer<T>
    ): Result<T> = try {
        val snapshot = firestore.collection(collection).document(documentId).get()
        if (snapshot.exists) {
            Result.Success(snapshot.data(serializer))
        } else {
            Result.Error(Exception("Document $documentId not found in $collection"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun <T> getCollection(
        collection: String,
        serializer: KSerializer<T>
    ): Result<List<T>> = try {
        val snapshot = firestore.collection(collection).get()
        val data = snapshot.documents.map { it.data(serializer) }
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun <T> query(
        collection: String,
        field: String,
        value: Any,
        serializer: KSerializer<T>
    ): Result<List<T>> = try {
        val snapshot = firestore.collection(collection).where(field, equalTo = value).get()
        val data = snapshot.documents.map { it.data(serializer) }
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override fun <T> observeDocument(
        collection: String,
        documentId: String,
        serializer: KSerializer<T>
    ): Flow<Result<T>> {
        return firestore.collection(collection).document(documentId).snapshots.map { snapshot ->
            if (snapshot.exists) {
                Result.Success(snapshot.data(serializer))
            } else {
                Result.Error(Exception("Document $documentId not found in $collection"))
            }
        }.catch { e ->
            emit(Result.Error(Exception(e)))
        }
    }

    override fun <T> observeCollection(
        collection: String,
        serializer: KSerializer<T>
    ): Flow<Result<List<T>>> {
        return firestore.collection(collection).snapshots.map { snapshot ->
            val data = snapshot.documents.map { it.data(serializer) }
            Result.Success(data)
        }.catch { e ->
            emit(Result.Error(Exception(e)))
        }
    }

    override suspend fun setDocument(
        collection: String,
        documentId: String,
        data: Any
    ): Result<Unit> = try {
        // For setDocument, we might need a serializer too if 'data' is a complex object
        // GitLive's set expects a serializable object
        firestore.collection(collection).document(documentId).set(data)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>
    ): Result<Unit> = try {
        firestore.collection(collection).document(documentId).update(data)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): Result<Unit> = try {
        firestore.collection(collection).document(documentId).delete()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun batch(operations: List<BatchOperation>): Result<Unit> = try {
        // GitLive might have a different way to handle batches
        // For now, simple implementation if supported
        // firestore.batch { ... }
        // If not directly supported, we might need to loop or use platform specific
        // Actually GitLive supports writeBatch
        val batch = firestore.batch()
        operations.forEach { op ->
            when (op) {
                is BatchOperation.Set -> batch.set(firestore.collection(op.collection).document(op.documentId), op.data)
                is BatchOperation.Update -> batch.update(firestore.collection(op.collection).document(op.documentId), op.data)
                is BatchOperation.Delete -> batch.delete(firestore.collection(op.collection).document(op.documentId))
            }
        }
        batch.commit()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
