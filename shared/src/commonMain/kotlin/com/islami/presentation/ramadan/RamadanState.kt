package com.islami.presentation.ramadan

import com.islami.domain.entities.StorageImage
import com.islami.domain.entities.RamadanDay
import com.islami.domain.ramadan.RamadanIssue

data class RamadanState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    /** What's live in `root/ar/ramadan`. */
    val published: List<RamadanDay> = emptyList(),
    val draft: List<RamadanDay> = emptyList(),
    val selectedId: String? = null,
    val issues: List<RamadanIssue> = emptyList(),
    val changedIds: Set<String> = emptySet(),
    /** Null until the picker is first opened. */
    val backgrounds: List<StorageImage>? = null,
    val isLoadingBackgrounds: Boolean = false,
    val pickerOpen: Boolean = false,
    val isUploading: Boolean = false,
    val confirmSave: Boolean = false,
    val isSaving: Boolean = false,
    /** Days someone else saved since they were loaded; non-empty shows the conflict dialog. */
    val conflictDays: List<Int> = emptyList(),
    val message: String? = null
) {
    val selected: RamadanDay? get() = draft.firstOrNull { it.id == selectedId }

    val hasChanges: Boolean get() = changedIds.isNotEmpty()

    val errorCount: Int get() = issues.count { it.isError }

    fun issuesFor(dayId: String) = issues.filter { it.dayId == dayId }
}
