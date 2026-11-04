package com.islami.data.remote.firebase

import com.islami.core.error.Result
import com.islami.domain.entities.User
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

/**
 * Platform-specific Firebase initialization
 */
expect fun initializeFirebase()

interface FirebaseAuthClient {
    /** Emits the signed-in user, or null; Firebase restores a persisted session on launch. */
    fun currentUser(): Flow<User?>

    suspend fun signIn(email: String, password: String): Result<User>

    suspend fun signOut(): Result<Unit>
}

interface FirebaseFirestoreClient {
    suspend fun <T> getCollection(
        collection: String,
        serializer: KSerializer<T>
    ): Result<List<T>>

    /** Creates or replaces the document. */
    suspend fun <T : Any> setDocument(
        collection: String,
        documentId: String,
        data: T,
        serializer: KSerializer<T>
    ): Result<Unit>
}

interface FirebaseStorageClient {
    /** Writes [data] to [path] (e.g. `json/azkar.json`), replacing any existing file. */
    suspend fun upload(path: String, data: ByteArray, contentType: String? = null): Result<Unit>

    suspend fun download(path: String): Result<ByteArray>

    /** Content hash Storage keeps for the file; changes whenever the file's bytes do. */
    suspend fun md5Hash(path: String): Result<String?>

    /** Full paths of the files directly inside [folder]. */
    suspend fun list(folder: String): Result<List<String>>
}
