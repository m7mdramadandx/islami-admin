package com.islami.presentation.release

import com.islami.domain.entities.AppReleaseConfig
import com.islami.domain.entities.AppReleaseHistoryEntry
import com.islami.domain.entities.AppReleaseSnapshot
import com.islami.domain.entities.ContentKind
import com.islami.domain.release.ReleaseField
import com.islami.domain.release.ReleaseIssue
import com.islami.domain.release.normalized
import com.islami.domain.release.raisesMinVersion

data class AppReleaseState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    /** What's live in `root/version`; null until the first successful load. */
    val published: AppReleaseSnapshot? = null,
    /** The form as typed, including blank note rows still being filled in. */
    val form: AppReleaseConfig = AppReleaseConfig(),
    val issues: List<ReleaseIssue> = emptyList(),
    val confirmSave: Boolean = false,
    val isSaving: Boolean = false,
    /** Someone else saved since this config was loaded; asks before overwriting their change. */
    val conflict: Boolean = false,
    /** Non-null while the history dialog is open. */
    val history: List<AppReleaseHistoryEntry>? = null,
    val isLoadingHistory: Boolean = false,
    /** Version typed into the "what does this app see" checker. */
    val testVersion: String = "",
    val confirmBump: ContentKind? = null,
    val message: String? = null
) {
    val hasChanges: Boolean
        get() = published != null && form.normalized() != published.config

    val errorCount: Int get() = issues.count { it.isError }

    val canSave: Boolean get() = hasChanges && !isSaving && errorCount == 0

    /** Raising the minimum blocks real users, so saving it needs the version typed out. */
    val raisesMinVersion: Boolean get() = form.raisesMinVersion(published?.config)

    fun issuesFor(field: ReleaseField) = issues.filter { it.field == field }

    /** "Field: old to new" lines for the confirmation dialog. */
    val changeSummary: List<String>
        get() {
            val old = published?.config ?: return emptyList()
            val new = form.normalized()
            return buildList {
                if (old.storeVersion != new.storeVersion) add("Store version: ${old.storeVersion.ifEmpty { "—" }} to ${new.storeVersion}")
                if (old.minVersion != new.minVersion) add("Minimum version: ${old.minVersion.ifEmpty { "—" }} to ${new.minVersion}")
                if (old.releaseNotesAr != new.releaseNotesAr) add("Arabic release notes: ${new.releaseNotesAr.size} lines")
                if (old.releaseNotesEn != new.releaseNotesEn) add("English release notes: ${new.releaseNotesEn.size} lines")
            }
        }
}
