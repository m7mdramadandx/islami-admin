package com.islami.domain.repositories

import com.islami.core.error.Result
import com.islami.domain.entities.*
import com.islami.domain.images.ImageType
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

    /** Backs up, uploads and bumps the version; fails on a conflict with [base] unless [overwrite]. */
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

    /** Writes `root/version` and logs history; fails on a conflict with [base] unless [overwrite]. */
    suspend fun save(config: AppReleaseConfig, base: AppReleaseConfig, overwrite: Boolean): Result<AppReleaseSnapshot>

    suspend fun history(): Result<List<AppReleaseHistoryEntry>>
}

interface RamadanRepository {
    suspend fun loadDays(): Result<List<RamadanDay>>

    /** Writes [changed] in one batch; fails on a conflict with [base] unless [overwrite]. */
    suspend fun saveDays(changed: List<RamadanDay>, base: List<RamadanDay>, overwrite: Boolean): Result<List<RamadanDay>>
}

interface StoriesRepository {
    suspend fun loadStories(): Result<List<Story>>

    /** Backs up, then updates; fails on a conflict with [base] unless [overwrite]. */
    suspend fun saveStory(story: Story, base: Story, overwrite: Boolean): Result<Story>

    /** Fails with [com.islami.domain.entities.StoryExistsException] if the id is taken. */
    suspend fun createStory(story: Story): Result<Story>

    /** Backs up, then deletes, [story] as it was last loaded. */
    suspend fun deleteStory(story: Story): Result<Unit>
}

interface ImageLibraryRepository {
    suspend fun list(folder: ImageFolder): Result<List<StorageImage>>

    /** Uploads under a new random name in [folder]; never replaces an existing image. */
    suspend fun upload(folder: ImageFolder, bytes: ByteArray, type: ImageType): Result<StorageImage>
}

interface NotificationRepository {
    suspend fun sendNotification(notification: Notification): Result<Unit>
}
