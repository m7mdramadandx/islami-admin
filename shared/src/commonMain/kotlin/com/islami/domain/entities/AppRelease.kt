package com.islami.domain.entities

import kotlinx.serialization.Serializable

/**
 * The editable part of `root/version`, which the mobile app reads on every launch with
 * `toObject(ConfigVersion)`. Every field must stay a string (or list of strings): a wrong type makes
 * that read fail, which silently disables update prompts *and* azkar/duas content updates.
 */
@Serializable
data class AppReleaseConfig(
    /** Latest version in the store; older apps get a dismissible update prompt (once a day). */
    val storeVersion: String = "",
    /** Apps below this are blocked by a force-update dialog with no Cancel. */
    val minVersion: String = "",
    /** Shown on the app's "App versions" screen to Arabic readers. */
    val releaseNotesAr: List<String> = emptyList(),
    val releaseNotesEn: List<String> = emptyList()
)

data class AppReleaseSnapshot(
    val config: AppReleaseConfig,
    /** `azkarVersion` / `duasVersion`, shown read-only; the content editor owns them. */
    val contentVersions: Map<ContentKind, Int>
)

/** One save, recorded in `root/version/history` so any earlier configuration can be restored. */
@Serializable
data class AppReleaseHistoryEntry(
    val id: String,
    val before: AppReleaseConfig,
    val after: AppReleaseConfig,
    val savedAt: Long,
    val savedBy: String? = null
)

/** The live config changed after it was loaded; saving now would overwrite those changes. */
class AppReleaseConflictException : Exception("The release config was changed by someone else since you opened it")

/** The config is live but its history entry couldn't be written. */
class AppReleaseHistoryException(
    val saved: AppReleaseSnapshot,
    cause: Throwable
) : Exception("Saved, but the change couldn't be added to history: ${cause.message}", cause)
