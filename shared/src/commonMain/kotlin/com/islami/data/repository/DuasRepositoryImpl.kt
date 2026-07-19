package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.Dua
import com.islami.domain.repositories.DuasRepository
import kotlinx.coroutines.flow.Flow

class DuasRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient
) : DuasRepository {

    override suspend fun getAllDuas(): Result<List<Dua>> {
        return firestoreClient.getCollection(
            collection = "duas",
            serializer = Dua.serializer()
        )
    }

    override suspend fun getDuasByCategory(category: String): Result<List<Dua>> {
        return firestoreClient.query(
            collection = "duas",
            field = "category",
            value = category,
            serializer = Dua.serializer()
        )
    }

    override suspend fun searchDuas(query: String): Result<List<Dua>> {
        return firestoreClient.query(
            collection = "duas",
            field = "arabicText",
            value = query,
            serializer = Dua.serializer()
        )
    }

    override suspend fun saveDua(dua: Dua): Result<Unit> {
        return firestoreClient.setDocument(
            collection = "duas",
            documentId = dua.id,
            data = dua
        )
    }

    override fun observeDuas(): Flow<Result<List<Dua>>> {
        return firestoreClient.observeCollection(
            collection = "duas",
            serializer = Dua.serializer()
        )
    }
}
