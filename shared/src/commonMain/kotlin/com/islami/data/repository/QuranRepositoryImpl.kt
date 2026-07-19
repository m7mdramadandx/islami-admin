package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.Ayah
import com.islami.domain.entities.Surah
import com.islami.domain.repositories.QuranRepository
import kotlinx.coroutines.flow.Flow

class QuranRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient
) : QuranRepository {

    override suspend fun getAllSurahs(): Result<List<Surah>> {
        return firestoreClient.getCollection(
            collection = "surahs",
            serializer = Surah.serializer()
        )
    }

    override suspend fun getSurah(number: Int): Result<Surah> {
        return firestoreClient.getDocument(
            collection = "surahs",
            documentId = number.toString(),
            serializer = Surah.serializer()
        )
    }

    override suspend fun getSurahAyahs(surahNumber: Int): Result<List<Ayah>> {
        return firestoreClient.query(
            collection = "ayahs",
            field = "surahNumber",
            value = surahNumber,
            serializer = Ayah.serializer()
        )
    }

    override suspend fun searchAyahs(query: String): Result<List<Ayah>> {
        // Simple search might not work well with Firestore query
        // Usually we need Algolia or some other search engine
        // For now, let's assume we search by a 'keywords' array or similar
        return firestoreClient.query(
            collection = "ayahs",
            field = "text",
            value = query,
            serializer = Ayah.serializer()
        )
    }

    override fun observeSurahs(): Flow<Result<List<Surah>>> {
        return firestoreClient.observeCollection(
            collection = "surahs",
            serializer = Surah.serializer()
        )
    }
}
