package com.islami.presentation.content

import com.islami.domain.content.ContentChanges
import com.islami.domain.content.ContentIssue
import com.islami.domain.entities.ContentBackup
import com.islami.domain.entities.ContentCategory
import com.islami.domain.entities.ContentDocument
import com.islami.domain.entities.ContentKind
import com.islami.domain.entities.ContentSnapshot

data class ContentEditorState(
    val kind: ContentKind,
    val isLoading: Boolean = true,
    val loadError: String? = null,
    /** What's live in Storage; null until the first successful load. */
    val published: ContentSnapshot? = null,
    val draft: ContentDocument? = null,
    val selectedCategoryKey: Long? = null,
    val query: String = "",
    val issues: List<ContentIssue> = emptyList(),
    val changes: ContentChanges = ContentChanges(),
    /** Unpublished edits from an earlier session were restored on open. */
    val restoredDraftSavedAt: Long? = null,
    /** The restored draft started from a file that has since been republished. */
    val restoredDraftIsStale: Boolean = false,
    val confirmPublish: Boolean = false,
    val isPublishing: Boolean = false,
    /** Someone else published since this file was loaded; asks before overwriting their work. */
    val conflict: Boolean = false,
    /** The file is uploaded but apps won't fetch it until the version bump succeeds. */
    val versionBumpPending: Boolean = false,
    /** Non-null while the backups dialog is open. */
    val backups: List<ContentBackup>? = null,
    val isLoadingBackups: Boolean = false,
    /** Last destructive edit, offered back through the snackbar's Undo. */
    val undo: Undo? = null,
    val message: String? = null
) {
    val selectedCategory: ContentCategory?
        get() = selectedCategoryKey?.let { draft?.category(it) }

    val errorCount: Int get() = issues.count { it.isError }

    val canPublish: Boolean
        get() = draft != null && changes.hasChanges && !isPublishing

    data class Undo(val document: ContentDocument, val selectedCategoryKey: Long?)
}
