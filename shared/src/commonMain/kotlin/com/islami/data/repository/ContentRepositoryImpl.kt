package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.content.ContentCodec
import com.islami.data.remote.firebase.FirebaseStorageClient
import com.islami.domain.entities.ContentBackup
import com.islami.domain.entities.ContentConflictException
import com.islami.domain.entities.ContentDocument
import com.islami.domain.entities.ContentKind
import com.islami.domain.entities.ContentSnapshot
import com.islami.domain.entities.ContentVersionBumpException
import com.islami.domain.repositories.ContentRepository
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

/**
 * Edits the azkar/duas JSON files the mobile app reads from Storage. Publishing is:
 * conflict check → backup of the live file → upload → bump `root/version`, in that order so the
 * app only sees a new version number once the new file is in place.
 */
class ContentRepositoryImpl(
    private val storage: FirebaseStorageClient,
    private val firestore: FirebaseFirestore
) : ContentRepository {

    override suspend fun load(kind: ContentKind): Result<ContentSnapshot> = attempt {
        val raw = storage.download(kind.storagePath).orThrow().decodeToString()
        ContentSnapshot(
            document = ContentCodec.decode(kind, raw),
            rawJson = raw,
            md5Hash = storage.md5Hash(kind.storagePath).orNull(),
            version = readVersion(kind)
        )
    }

    override suspend fun publish(
        kind: ContentKind,
        document: ContentDocument,
        base: ContentSnapshot,
        overwrite: Boolean
    ): Result<ContentSnapshot> = attempt {
        if (!overwrite && base.md5Hash != null) {
            val live = storage.md5Hash(kind.storagePath).orThrow()
            if (live != base.md5Hash) throw ContentConflictException()
        }

        storage.upload(backupPath(kind), base.rawJson.encodeToByteArray(), JSON).orThrow()

        // Keep the live file's line endings (duas.json uses CRLF) so backups diff cleanly. JSON strings
        // can't hold raw newlines, so this only touches the formatting between values.
        val json = ContentCodec.encode(kind, document)
            .let { if ("\r\n" in base.rawJson) it.replace("\n", "\r\n") else it }
        storage.upload(kind.storagePath, json.encodeToByteArray(), JSON).orThrow()
        val uploaded = ContentSnapshot(
            document = document,
            rawJson = json,
            md5Hash = storage.md5Hash(kind.storagePath).orNull(),
            version = base.version
        )

        val version = try {
            incrementVersion(kind)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ContentVersionBumpException(uploaded, e)
        }
        uploaded.copy(version = version)
    }

    override suspend fun bumpVersion(kind: ContentKind): Result<Int> = attempt { incrementVersion(kind) }

    override suspend fun listBackups(kind: ContentKind): Result<List<ContentBackup>> = attempt {
        storage.list(kind.backupFolder).orThrow()
            .map { path -> ContentBackup(path = path, label = backupLabel(path)) }
            .sortedByDescending { it.path }
    }

    override suspend fun loadBackup(kind: ContentKind, backup: ContentBackup): Result<ContentDocument> = attempt {
        ContentCodec.decode(kind, storage.download(backup.path).orThrow().decodeToString())
    }

    private suspend fun readVersion(kind: ContentKind): Int {
        val snapshot = versionDocument().get()
        if (!snapshot.exists || !snapshot.contains(kind.versionField)) return 0
        // The app parses this as a string ("1", "2", ...); tolerate a number in case it was hand-edited.
        return runCatching { snapshot.get<String?>(kind.versionField)?.trim()?.toIntOrNull() }.getOrNull()
            ?: runCatching { snapshot.get<Long?>(kind.versionField)?.toInt() }.getOrNull()
            ?: 0
    }

    private suspend fun incrementVersion(kind: ContentKind): Int {
        val next = readVersion(kind) + 1
        versionDocument().updateFields { kind.versionField to next.toString() }
        return next
    }

    private fun versionDocument() = firestore.collection(VERSION_COLLECTION).document(VERSION_DOCUMENT)

    /** e.g. `json/backups/azkar/2026-11-04T09-30-12Z.json`; names sort chronologically. */
    private fun backupPath(kind: ContentKind): String {
        val stamp = Clock.System.now().toString().substringBefore('.').removeSuffix("Z").replace(':', '-')
        return "${kind.backupFolder}/${stamp}Z.json"
    }

    private fun backupLabel(path: String): String {
        val stamp = path.substringAfterLast('/').removeSuffix(".json").removeSuffix("Z")
        val (date, time) = stamp.split('T').let { it.first() to it.getOrElse(1) { "" } }
        return "$date ${time.replace('-', ':')} UTC".trim()
    }

    private fun <T> Result<T>.orThrow(): T = when (this) {
        is Result.Success -> data
        is Result.Error -> throw exception
        is Result.Loading -> error("Unexpected loading result")
    }

    private fun <T> Result<T>.orNull(): T? = (this as? Result.Success)?.data

    private inline fun <T> attempt(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e)
    }

    private companion object {
        const val JSON = "application/json"
        const val VERSION_COLLECTION = "root"
        const val VERSION_DOCUMENT = "version"
    }
}
