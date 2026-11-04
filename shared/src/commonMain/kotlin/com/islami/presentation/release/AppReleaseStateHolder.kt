package com.islami.presentation.release

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.AppReleaseConfig
import com.islami.domain.entities.AppReleaseConflictException
import com.islami.domain.entities.AppReleaseHistoryEntry
import com.islami.domain.entities.AppReleaseHistoryException
import com.islami.domain.entities.ContentKind
import com.islami.domain.release.ReleaseField
import com.islami.domain.release.validate
import com.islami.domain.usecases.content.BumpContentVersionUseCase
import com.islami.domain.usecases.release.ListAppReleaseHistoryUseCase
import com.islami.domain.usecases.release.LoadAppReleaseUseCase
import com.islami.domain.usecases.release.SaveAppReleaseParams
import com.islami.domain.usecases.release.SaveAppReleaseUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppReleaseStateHolder(
    private val loadAppRelease: LoadAppReleaseUseCase,
    private val saveAppRelease: SaveAppReleaseUseCase,
    private val listHistory: ListAppReleaseHistoryUseCase,
    private val bumpContentVersion: BumpContentVersionUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<AppReleaseState>(AppReleaseState()) {

    init {
        load()
    }

    fun load() = scope.launch {
        updateState { it.copy(isLoading = true, loadError = null) }
        when (val result = loadAppRelease(NoParams)) {
            is Result.Success -> updateState {
                it.copy(
                    isLoading = false,
                    published = result.data,
                    testVersion = it.testVersion.ifEmpty { result.data.config.minVersion }
                ).withForm(result.data.config)
            }
            is Result.Error -> updateState { it.copy(isLoading = false, loadError = result.exception.readable()) }
            is Result.Loading -> Unit
        }
    }

    // region Form

    fun onStoreVersionChanged(value: String) = editForm { it.copy(storeVersion = value.filterVersionChars()) }

    fun onMinVersionChanged(value: String) = editForm { it.copy(minVersion = value.filterVersionChars()) }

    fun addNote(field: ReleaseField) = editNotes(field) { it + "" }

    fun onNoteChanged(field: ReleaseField, index: Int, value: String) = editNotes(field) { notes ->
        notes.mapIndexed { i, note -> if (i == index) value else note }
    }

    fun moveNote(field: ReleaseField, index: Int, offset: Int) = editNotes(field) { notes ->
        val target = index + offset
        if (index !in notes.indices || target !in notes.indices) notes
        else notes.toMutableList().apply { add(target, removeAt(index)) }
    }

    fun removeNote(field: ReleaseField, index: Int) = editNotes(field) { notes ->
        notes.filterIndexed { i, _ -> i != index }
    }

    fun onTestVersionChanged(value: String) = updateState { it.copy(testVersion = value.filterVersionChars()) }

    fun discardChanges() {
        val published = currentState().published?.config ?: return
        updateState { it.withForm(published) }
    }

    // endregion

    // region Saving

    fun requestSave() {
        val state = currentState()
        if (state.errorCount > 0) {
            updateState { it.copy(message = it.issues.first { i -> i.isError }.message) }
            return
        }
        if (state.canSave) updateState { it.copy(confirmSave = true) }
    }

    fun dismissSave() = updateState { it.copy(confirmSave = false, conflict = false) }

    fun confirmSave(overwrite: Boolean = false) {
        val state = currentState()
        val base = state.published?.config ?: return
        val form = state.form
        updateState { it.copy(confirmSave = false, conflict = false, isSaving = true) }

        scope.launch {
            when (val result = saveAppRelease(SaveAppReleaseParams(form, base, overwrite))) {
                is Result.Success -> updateState {
                    it.copy(isSaving = false, published = result.data, message = "Saved. Apps pick it up next time they open")
                        .withForm(result.data.config)
                }
                is Result.Error -> when (val e = result.exception) {
                    is AppReleaseConflictException -> updateState { it.copy(isSaving = false, conflict = true) }
                    is AppReleaseHistoryException -> updateState {
                        it.copy(isSaving = false, published = e.saved, message = e.message).withForm(e.saved.config)
                    }
                    else -> updateState { it.copy(isSaving = false, message = "Save failed: ${e.readable()}") }
                }
                is Result.Loading -> Unit
            }
        }
    }

    // endregion

    // region History

    fun openHistory() = scope.launch {
        updateState { it.copy(history = emptyList(), isLoadingHistory = true) }
        when (val result = listHistory(NoParams)) {
            is Result.Success -> updateState { it.copy(history = result.data, isLoadingHistory = false) }
            is Result.Error -> updateState {
                it.copy(history = null, isLoadingHistory = false, message = "Couldn't load history: ${result.exception.readable()}")
            }
            is Result.Loading -> Unit
        }
    }

    fun dismissHistory() = updateState { it.copy(history = null) }

    /** Fills the form from history; nothing changes for users until it's saved. */
    fun restore(entry: AppReleaseHistoryEntry, before: Boolean) {
        val config = if (before) entry.before else entry.after
        updateState {
            it.copy(history = null, message = "Loaded into the form. Review it, then save to make it live")
                .withForm(config)
        }
    }

    // endregion

    // region Content versions

    fun requestBump(kind: ContentKind) = updateState { it.copy(confirmBump = kind) }

    fun dismissBump() = updateState { it.copy(confirmBump = null) }

    fun confirmBump() {
        val kind = currentState().confirmBump ?: return
        updateState { it.copy(confirmBump = null) }
        scope.launch {
            when (val result = bumpContentVersion(kind)) {
                is Result.Success -> updateState { state ->
                    state.copy(
                        published = state.published?.let { it.copy(contentVersions = it.contentVersions + (kind to result.data)) },
                        message = "${kind.title} version is now ${result.data}. Apps re-download it next time they open"
                    )
                }
                is Result.Error -> updateState { it.copy(message = "Couldn't bump ${kind.title.lowercase()}: ${result.exception.readable()}") }
                is Result.Loading -> Unit
            }
        }
    }

    // endregion

    fun onMessageShown() = updateState { it.copy(message = null) }

    private fun editForm(transform: (AppReleaseConfig) -> AppReleaseConfig) =
        updateState { it.withForm(transform(it.form)) }

    private fun editNotes(field: ReleaseField, transform: (List<String>) -> List<String>) = editForm { form ->
        when (field) {
            ReleaseField.NOTES_AR -> form.copy(releaseNotesAr = transform(form.releaseNotesAr))
            ReleaseField.NOTES_EN -> form.copy(releaseNotesEn = transform(form.releaseNotesEn))
            else -> form
        }
    }

    private fun AppReleaseState.withForm(form: AppReleaseConfig) =
        copy(form = form, issues = form.validate(published?.config))

    /** Versions are digits and dots only; dropping anything else avoids "4.4.1 " style typos. */
    private fun String.filterVersionChars() = filter { it.isDigit() || it == '.' }

    private fun Exception.readable(): String = message?.takeIf { it.isNotBlank() } ?: "Unknown error"
}
