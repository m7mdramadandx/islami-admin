package com.islami.data.remote.firestore

import com.islami.core.error.ErrorResponse
import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import dev.gitlive.firebase.firestore.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

actual class FirebaseFirestoreClientImpl : FirebaseFirestoreClient {
    private val db: FirebaseFirestore = Firebase.firestore

    override suspend inline fun <reified T> getDocument(
        collection: String,
        id: String
    ): Result<T> = try {
        val doc = db.collection(collection).document(id).get()
        val data = doc.data(T::class)
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to fetch document: ${e.message}"))
    }

    override suspend inline fun <reified T> getCollection(
        collection: String
    ): Result<List<T>> = try {
        val docs = db.collection(collection).get()
        val data = docs.documents.map { it.data(T::class) }
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to fetch collection: ${e.message}"))
    }

    override suspend inline fun <reified T> query(
        collection: String,
        field: String,
        value: Any?
    ): Result<List<T>> = try {
        val docs = if (value == null) {
            db.collection(collection)
                .where { field to null }
                .get()
        } else {
            db.collection(collection)
                .where { field to value }
                .get()
        }
        val data = docs.documents.map { it.data(T::class) }
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to query collection: ${e.message}"))
    }

    override suspend inline fun <reified T> queryMultiple(
        collection: String,
        filters: Map<String, Any?>
    ): Result<List<T>> = try {
        var query = db.collection(collection)
        
        for ((field, value) in filters) {
            query = if (value == null) {
                query.where { field to null }
            } else {
                query.where { field to value }
            }
        }
        
        val docs = query.get()
        val data = docs.documents.map { it.data(T::class) }
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to query collection: ${e.message}"))
    }

    override fun <T> observeDocument(
        collection: String,
        id: String,
        clazz: Class<T>
    ): Flow<Result<T>> = db.collection(collection)
        .document(id)
        .snapshots
        .map { snapshot ->
            try {
                @Suppress("UNCHECKED_CAST")
                val data = snapshot.data(clazz as kotlin.reflect.KClass<T>)
                Result.Success(data)
            } catch (e: Exception) {
                Result.Error(ErrorResponse(message = "Failed to observe document: ${e.message}"))
            }
        }

    override fun <T> observeCollection(
        collection: String,
        clazz: Class<T>
    ): Flow<Result<List<T>>> = db.collection(collection)
        .snapshots
        .map { snapshot ->
            try {
                @Suppress("UNCHECKED_CAST")
                val data = snapshot.documents.map { it.data(clazz as kotlin.reflect.KClass<T>) }
                Result.Success(data)
            } catch (e: Exception) {
                Result.Error(ErrorResponse(message = "Failed to observe collection: ${e.message}"))
            }
        }

    override suspend inline fun <reified T> setDocument(
        collection: String,
        id: String,
        data: T,
        merge: Boolean
    ): Result<Unit> = try {
        db.collection(collection).document(id).set(data, SetOptions(merge = merge))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to set document: ${e.message}"))
    }

    override suspend inline fun <reified T> updateDocument(
        collection: String,
        id: String,
        data: Map<String, Any?>
    ): Result<Unit> = try {
        @Suppress("UNCHECKED_CAST")
        db.collection(collection).document(id).update(data as Map<String, Any?>)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to update document: ${e.message}"))
    }

    override suspend fun deleteDocument(
        collection: String,
        id: String
    ): Result<Unit> = try {
        db.collection(collection).document(id).delete()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to delete document: ${e.message}"))
    }

    override suspend inline fun batch(
        operations: List<FirebaseFirestoreClient.BatchOperation>
    ): Result<Unit> = try {
        val batch = db.batch()
        for (operation in operations) {
            when (operation) {
                is FirebaseFirestoreClient.BatchOperation.Set -> {
                    @Suppress("UNCHECKED_CAST")
                    batch.set(
                        db.collection(operation.collection).document(operation.id),
                        operation.data,
                        SetOptions(merge = operation.merge)
                    )
                }
                is FirebaseFirestoreClient.BatchOperation.Update -> {
                    @Suppress("UNCHECKED_CAST")
                    batch.update(
                        db.collection(operation.collection).document(operation.id),
                        operation.data as Map<String, Any?>
                    )
                }
                is FirebaseFirestoreClient.BatchOperation.Delete -> {
                    batch.delete(db.collection(operation.collection).document(operation.id))
                }
            }
        }
        batch.commit()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to commit batch: ${e.message}"))
    }
}

@Serializable
private data class SetOptions(val merge: Boolean = false)
