package com.islami.data.local

import com.islami.domain.entities.ContentDocument
import com.islami.domain.entities.ContentKind
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

@Serializable
data class ContentDraft(
    val document: ContentDocument,
    /** md5 of the live file the draft started from; a mismatch means it was published since. */
    val baseMd5Hash: String?,
    val savedAt: Long
)

/** Keeps unpublished edits on this device so a refresh or closed tab doesn't lose them. */
class ContentDraftStore(private val localDataSource: LocalDataSource) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun load(kind: ContentKind): ContentDraft? = try {
        localDataSource.getString(key(kind)).takeIf { it.isNotEmpty() }
            ?.let { json.decodeFromString(ContentDraft.serializer(), it) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // A draft from an older editor version that no longer parses isn't worth failing over.
        null
    }

    suspend fun save(kind: ContentKind, draft: ContentDraft) {
        localDataSource.saveString(key(kind), json.encodeToString(ContentDraft.serializer(), draft))
    }

    suspend fun clear(kind: ContentKind) {
        localDataSource.delete(key(kind))
    }

    private fun key(kind: ContentKind) = "content_draft_${kind.name.lowercase()}"
}
