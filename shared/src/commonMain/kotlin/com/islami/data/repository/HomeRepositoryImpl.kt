package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.AppSettings
import com.islami.domain.entities.HomeStats
import com.islami.domain.repositories.HomeRepository

class HomeRepositoryImpl(
    private val firestoreClient: FirebaseFirestoreClient
) : HomeRepository {

    override suspend fun getHomeStats(): Result<HomeStats> {
        return firestoreClient.getDocument(
            collection = "stats",
            documentId = "home",
            serializer = HomeStats.serializer()
        )
    }

    override suspend fun getAppSettings(): Result<AppSettings> {
        return firestoreClient.getDocument(
            collection = "settings",
            documentId = "app",
            serializer = AppSettings.serializer()
        )
    }
}
