package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.AdminUser
import com.islami.domain.repositories.UserManagementRepository
import kotlinx.coroutines.flow.Flow

class UserManagementRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient
) : UserManagementRepository {

    override suspend fun getAllAdminUsers(): Result<List<AdminUser>> {
        return firestoreClient.getCollection(
            collection = "admin_users",
            serializer = AdminUser.serializer()
        )
    }

    override suspend fun getAdminUser(uid: String): Result<AdminUser> {
        return firestoreClient.getDocument(
            collection = "admin_users",
            documentId = uid,
            serializer = AdminUser.serializer()
        )
    }

    override suspend fun createAdminUser(user: AdminUser, password: String): Result<Unit> {
        // password logic would normally be in a cloud function or Auth service
        // here we just save the metadata to firestore
        return firestoreClient.setDocument(
            collection = "admin_users",
            documentId = user.uid,
            data = user
        )
    }

    override suspend fun updateAdminUser(user: AdminUser): Result<Unit> {
        return firestoreClient.setDocument(
            collection = "admin_users",
            documentId = user.uid,
            data = user
        )
    }

    override suspend fun deleteAdminUser(uid: String): Result<Unit> {
        return firestoreClient.deleteDocument(
            collection = "admin_users",
            documentId = uid
        )
    }

    override suspend fun updateUserRole(uid: String, role: String): Result<Unit> {
        return firestoreClient.updateDocument(
            collection = "admin_users",
            documentId = uid,
            data = mapOf("role" to role)
        )
    }

    override fun observeAdminUsers(): Flow<Result<List<AdminUser>>> {
        return firestoreClient.observeCollection(
            collection = "admin_users",
            serializer = AdminUser.serializer()
        )
    }
}
