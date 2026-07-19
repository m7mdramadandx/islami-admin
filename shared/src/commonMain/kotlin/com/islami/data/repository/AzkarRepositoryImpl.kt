package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.Azkar
import com.islami.domain.repositories.AzkarRepository
import kotlinx.coroutines.flow.Flow

class AzkarRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient
) : AzkarRepository {

    override suspend fun getAzkar(): Result<List<Azkar>> {
        return firestoreClient.getCollection(
            collection = "azkar",
            serializer = Azkar.serializer()
        )
    }

    override suspend fun getAzkarByCategory(category: String): Result<Azkar> {
        return firestoreClient.getDocument(
            collection = "azkar",
            documentId = category,
            serializer = Azkar.serializer()
        )
    }

    override suspend fun saveAzkar(azkar: Azkar): Result<Unit> {
        return firestoreClient.setDocument(
            collection = "azkar",
            documentId = azkar.id,
            data = azkar
        )
    }

    override fun observeAzkar(): Flow<Result<List<Azkar>>> {
        return firestoreClient.observeCollection(
            collection = "azkar",
            serializer = Azkar.serializer()
        )
    }
}
