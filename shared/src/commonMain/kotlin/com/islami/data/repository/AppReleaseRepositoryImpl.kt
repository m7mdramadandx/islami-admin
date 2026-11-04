package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.core.time.currentTimeMillis
import com.islami.data.remote.firebase.FirebaseFirestoreClient
import com.islami.domain.entities.AppReleaseConfig
import com.islami.domain.entities.AppReleaseConflictException
import com.islami.domain.entities.AppReleaseHistoryEntry
import com.islami.domain.entities.AppReleaseHistoryException
import com.islami.domain.entities.AppReleaseSnapshot
import com.islami.domain.entities.ContentKind
import com.islami.domain.repositories.AppReleaseRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.cancellation.CancellationException

/**
 * Edits the release fields of `root/version`. Only those four fields are ever written (a field
 * update, not a document set), so release notes edits can't clobber `azkarVersion` and friends.
 */
class AppReleaseRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val firestoreClient: FirebaseFirestoreClient,
    private val auth: FirebaseAuth
) : AppReleaseRepository {

    override suspend fun load(): Result<AppReleaseSnapshot> = attempt { readSnapshot() }

    override suspend fun save(
        config: AppReleaseConfig,
        base: AppReleaseConfig,
        overwrite: Boolean
    ): Result<AppReleaseSnapshot> = attempt {
        val live = readSnapshot()
        if (!overwrite && live.config != base) throw AppReleaseConflictException()

        versionDocument().updateFields {
            STORE_VERSION to config.storeVersion
            MIN_VERSION to config.minVersion
            RELEASE_NOTES_AR to config.releaseNotesAr
            RELEASE_NOTES_EN to config.releaseNotesEn
        }
        val saved = live.copy(config = config)

        val savedAt = currentTimeMillis()
        val entry = AppReleaseHistoryEntry(
            id = savedAt.toString(),
            before = live.config,
            after = config,
            savedAt = savedAt,
            savedBy = auth.currentUser?.email
        )
        val logged = firestoreClient.setDocument(HISTORY_COLLECTION, entry.id, entry, AppReleaseHistoryEntry.serializer())
        if (logged is Result.Error) throw AppReleaseHistoryException(saved, logged.exception)
        saved
    }

    override suspend fun history(): Result<List<AppReleaseHistoryEntry>> =
        when (val result = firestoreClient.getCollection(HISTORY_COLLECTION, AppReleaseHistoryEntry.serializer())) {
            is Result.Success -> Result.Success(result.data.sortedByDescending { it.savedAt })
            else -> result
        }

    private suspend fun readSnapshot(): AppReleaseSnapshot {
        val doc = versionDocument().get()
        return AppReleaseSnapshot(
            config = AppReleaseConfig(
                storeVersion = doc.string(STORE_VERSION),
                minVersion = doc.string(MIN_VERSION),
                releaseNotesAr = doc.strings(RELEASE_NOTES_AR),
                releaseNotesEn = doc.strings(RELEASE_NOTES_EN)
            ),
            contentVersions = ContentKind.entries.associateWith { doc.string(it.versionField).toIntOrNull() ?: 0 }
        )
    }

    // Tolerant reads: a hand-edited field of the wrong type shows as empty instead of failing the screen.
    private fun DocumentSnapshot.string(field: String): String =
        if (!exists || !contains(field)) "" else runCatching { get<String?>(field) }.getOrNull()?.trim().orEmpty()

    private fun DocumentSnapshot.strings(field: String): List<String> =
        if (!exists || !contains(field)) emptyList()
        else runCatching { get<List<String>?>(field) }.getOrNull().orEmpty()

    private fun versionDocument() = firestore.collection(VERSION_COLLECTION).document(VERSION_DOCUMENT)

    private inline fun <T> attempt(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e)
    }

    private companion object {
        const val VERSION_COLLECTION = "root"
        const val VERSION_DOCUMENT = "version"
        const val HISTORY_COLLECTION = "root/version/history"
        const val STORE_VERSION = "storeVersion"
        const val MIN_VERSION = "minVersion"
        const val RELEASE_NOTES_AR = "releaseNotesAr"
        const val RELEASE_NOTES_EN = "releaseNotesEn"
    }
}
