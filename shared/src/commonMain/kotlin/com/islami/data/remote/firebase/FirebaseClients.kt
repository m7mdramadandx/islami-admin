package com.islami.data.remote.firebase

import com.islami.core.error.Result
import com.islami.domain.entities.User
import kotlinx.coroutines.flow.Flow

/**
 * Firebase Auth wrapper interface - platform-independent
 * Each platform provides its own implementation
 */
interface FirebaseAuthClient {
    /**
     * Emits current authenticated user or null
     */
    fun currentUser(): Flow<User?>

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<User>

    /**
     * Create account with email and password
     */
    suspend fun signUp(email: String, password: String): Result<User>

    /**
     * Sign out current user
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Send password reset email
     */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    /**
     * Get current user synchronously
     */
    suspend fun getCurrentUser(): User?

    /**
     * Verify email
     */
    suspend fun sendEmailVerification(): Result<Unit>

    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean
}

/**
 * Firebase Firestore wrapper interface - platform-independent
 * Each platform provides its own implementation
 */
interface FirebaseFirestoreClient {
    /**
     * Fetch a single document
     */
    suspend fun <T> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Result<T>

    /**
     * Fetch multiple documents (collection)
     */
    suspend fun <T> getCollection(
        collection: String,
        clazz: Class<T>
    ): Result<List<T>>

    /**
     * Fetch documents with query filter
     */
    suspend fun <T> query(
        collection: String,
        field: String,
        value: Any,
        clazz: Class<T>
    ): Result<List<T>>

    /**
     * Listen to document changes in real-time
     */
    fun <T> observeDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Flow<Result<T>>

    /**
     * Listen to collection changes in real-time
     */
    fun <T> observeCollection(
        collection: String,
        clazz: Class<T>
    ): Flow<Result<List<T>>>

    /**
     * Set/Create document
     */
    suspend fun setDocument(
        collection: String,
        documentId: String,
        data: Any
    ): Result<Unit>

    /**
     * Update existing document
     */
    suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any>
    ): Result<Unit>

    /**
     * Delete document
     */
    suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): Result<Unit>

    /**
     * Batch write operations
     */
    suspend fun batch(operations: List<BatchOperation>): Result<Unit>
}

/**
 * Represents a batch operation for Firestore
 */
sealed class BatchOperation {
    data class Set(val collection: String, val documentId: String, val data: Any) : BatchOperation()
    data class Update(val collection: String, val documentId: String, val data: Map<String, Any>) : BatchOperation()
    data class Delete(val collection: String, val documentId: String) : BatchOperation()
}

/**
 * Firebase Storage wrapper interface - platform-independent
 * Each platform provides its own implementation
 */
interface FirebaseStorageClient {
    /**
     * Upload file to Firebase Storage
     */
    suspend fun uploadFile(
        path: String,
        fileName: String,
        data: ByteArray
    ): Result<String> // Returns download URL

    /**
     * Download file from Firebase Storage
     */
    suspend fun downloadFile(
        path: String,
        fileName: String
    ): Result<ByteArray>

    /**
     * Delete file from Firebase Storage
     */
    suspend fun deleteFile(
        path: String,
        fileName: String
    ): Result<Unit>

    /**
     * Get download URL for file
     */
    suspend fun getDownloadUrl(
        path: String,
        fileName: String
    ): Result<String>
}

/**
 * Firebase Crashlytics wrapper interface
 */
interface FirebaseCrashlyticsClient {
    /**
     * Log an error to Crashlytics
     */
    fun recordError(throwable: Throwable, message: String? = null)

    /**
     * Log a message
     */
    fun log(message: String)

    /**
     * Set user ID for crash reports
     */
    fun setUserId(userId: String)

    /**
     * Set custom key-value data
     */
    fun setCustomKey(key: String, value: Any)

    /**
     * Enable/disable Crashlytics
     */
    fun setCrashlyticsCollectionEnabled(enabled: Boolean)
}
