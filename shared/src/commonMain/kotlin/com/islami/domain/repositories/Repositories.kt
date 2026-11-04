package com.islami.domain.repositories

import com.islami.core.error.Result
import com.islami.domain.entities.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun currentUser(): Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
}

interface FeedbackRepository {
    fun observeFeedbackMessages(): Flow<Result<List<FeedbackMessage>>>
    suspend fun deleteFeedbackMessage(id: String): Result<Unit>
}

interface ContentRepository {
    suspend fun load(kind: ContentKind): Result<ContentSnapshot>

    /**
     * Backs up the live file, uploads [document] and bumps the app's version number. Fails with
     * [com.islami.domain.entities.ContentConflictException] if the live file no longer matches
     * [base], unless [overwrite] is set.
     */
    suspend fun publish(
        kind: ContentKind,
        document: ContentDocument,
        base: ContentSnapshot,
        overwrite: Boolean
    ): Result<ContentSnapshot>

    /** Makes apps re-download the file; also the retry for a [publish] whose bump failed. */
    suspend fun bumpVersion(kind: ContentKind): Result<Int>

    suspend fun listBackups(kind: ContentKind): Result<List<ContentBackup>>
    suspend fun loadBackup(kind: ContentKind, backup: ContentBackup): Result<ContentDocument>
}

interface AppReleaseRepository {
    suspend fun load(): Result<AppReleaseSnapshot>

    /**
     * Writes the release fields of `root/version` and records the change in history. Fails with
     * [com.islami.domain.entities.AppReleaseConflictException] if the live config no longer matches
     * [base], unless [overwrite] is set.
     */
    suspend fun save(config: AppReleaseConfig, base: AppReleaseConfig, overwrite: Boolean): Result<AppReleaseSnapshot>

    suspend fun history(): Result<List<AppReleaseHistoryEntry>>
}

interface NotificationRepository {
    suspend fun sendNotification(notification: Notification): Result<Unit>
}
