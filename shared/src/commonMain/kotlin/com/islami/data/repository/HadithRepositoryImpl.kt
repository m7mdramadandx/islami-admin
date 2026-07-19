package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.Hadith
import com.islami.domain.repositories.HadithRepository
import kotlinx.coroutines.flow.Flow

class HadithRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient
) : HadithRepository {

    override suspend fun getAllHadith(): Result<List<Hadith>> {
        return firestoreClient.getCollection(
            collection = "hadiths",
            serializer = Hadith.serializer()
        )
    }

    override suspend fun getHadithByBook(book: String): Result<List<Hadith>> {
        return firestoreClient.query(
            collection = "hadiths",
            field = "book",
            value = book,
            serializer = Hadith.serializer()
        )
    }

    override suspend fun searchHadith(query: String): Result<List<Hadith>> {
        // Placeholder for search logic
        return firestoreClient.query(
            collection = "hadiths",
            field = "arabicText",
            value = query,
            serializer = Hadith.serializer()
        )
    }

    override fun observeHadith(): Flow<Result<List<Hadith>>> {
        return firestoreClient.observeCollection(
            collection = "hadiths",
            serializer = Hadith.serializer()
        )
    }
}
