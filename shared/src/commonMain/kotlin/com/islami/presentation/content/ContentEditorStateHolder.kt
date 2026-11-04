package com.islami.presentation.content

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.time.currentTimeMillis
import com.islami.data.local.ContentDraft
import com.islami.data.local.ContentDraftStore
import com.islami.domain.content.changesFrom
import com.islami.domain.content.rekeyAgainst
import com.islami.domain.content.validate
import com.islami.domain.content.withField
import com.islami.domain.entities.ContentBackup
import com.islami.domain.entities.ContentCategory
import com.islami.domain.entities.ContentConflictException
import com.islami.domain.entities.ContentDocument
import com.islami.domain.entities.ContentItem
import com.islami.domain.entities.ContentKind
import com.islami.domain.entities.ContentVersionBumpException
import com.islami.domain.entities.titleOf
import com.islami.domain.usecases.content.ListContentBackupsUseCase
import com.islami.domain.usecases.content.LoadContentBackupUseCase
import com.islami.domain.usecases.content.LoadContentBackupParams
import com.islami.domain.usecases.content.LoadContentUseCase
import com.islami.domain.usecases.content.PublishContentParams
import com.islami.domain.usecases.content.PublishContentUseCase
import com.islami.domain.usecases.content.BumpContentVersionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Edits one content file (azkar or duas) as a local draft until the admin publishes it. */
class ContentEditorStateHolder(
    kind: ContentKind,
    private val loadContent: LoadContentUseCase,
    private val publishContent: PublishContentUseCase,
    private val bumpVersionUseCase: BumpContentVersionUseCase,
    private val listBackups: ListContentBackupsUseCase,
    private val loadBackup: LoadContentBackupUseCase,
    private val draftStore: ContentDraftStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<ContentEditorState>(ContentEditorState(kind)) {

    private val kind get() = currentState().kind
    private var draftSaveJob: Job? = null

    init {
        load()
    }

    // region Loading

    fun load() = scope.launch {
        updateState { it.copy(isLoading = true, loadError = null) }
        // Reloading restores edits from the draft store, so make sure the latest ones are in it.
        if (draftSaveJob?.isActive == true) {
            draftSaveJob?.cancel()
            saveDraftNow()
        }
        when (val result = loadContent(kind)) {
            is Result.Success -> {
                val snapshot = result.data
                val saved = draftStore.load(kind)?.takeIf { it.document != snapshot.document }
                val stale = saved != null && saved.baseMd5Hash != snapshot.md5Hash
                val draft = when {
                    saved == null -> snapshot.document
                    // Keys only line up with a fresh decode of the same file; re-match otherwise.
                    stale -> saved.document.rekeyAgainst(snapshot.document)
                    else -> saved.document
                }
                updateState {
                    it.copy(
                        isLoading = false,
                        published = snapshot,
                        restoredDraftSavedAt = saved?.savedAt,
                        restoredDraftIsStale = stale,
                        selectedCategoryKey = it.selectedCategoryKey?.takeIf { k -> draft.category(k) != null }
                            ?: draft.categories.firstOrNull()?.key
                    ).withDraft(draft)
                }
            }
            is Result.Error -> updateState {
                it.copy(isLoading = false, loadError = result.exception.readable())
            }
            is Result.Loading -> Unit
        }
    }

    fun dismissRestoredDraftNotice() = updateState { it.copy(restoredDraftSavedAt = null) }

    // endregion

    // region Navigation and search

    fun selectCategory(key: Long?) = updateState { it.copy(selectedCategoryKey = key) }

    fun onQueryChanged(query: String) = updateState { it.copy(query = query) }

    // endregion

    // region Categories

    fun addCategory() = edit { doc ->
        val category = ContentCategory(id = doc.nextCategoryId(), key = doc.nextKey())
        updateState { it.copy(selectedCategoryKey = category.key, query = "") }
        doc.copy(categories = doc.categories + category)
    }

    fun onCategoryFieldChanged(categoryKey: Long, field: String, value: String) = editCategory(categoryKey) {
        it.copy(fields = it.fields.withField(field, value, publishedCategory(categoryKey)?.fields))
    }

    fun moveCategory(categoryKey: Long, offset: Int) = edit { doc ->
        doc.copy(categories = doc.categories.moved(doc.categories.indexOfFirst { it.key == categoryKey }, offset))
    }

    fun deleteCategory(categoryKey: Long) = edit(undoable = true) { doc ->
        val index = doc.categories.indexOfFirst { it.key == categoryKey }
        val remaining = doc.categories.filter { it.key != categoryKey }
        val name = doc.category(categoryKey)?.let(kind::titleOf).orEmpty().ifEmpty { "Category" }
        updateState {
            it.copy(
                selectedCategoryKey = remaining.getOrNull(index.coerceAtMost(remaining.lastIndex))?.key,
                message = "Deleted \"$name\""
            )
        }
        doc.copy(categories = remaining)
    }

    /** Resolves a duplicate id by giving this (unpublished-id) category the next free one. */
    fun assignNewCategoryId(categoryKey: Long) = edit { doc ->
        val id = doc.nextCategoryId()
        doc.copy(categories = doc.categories.map { if (it.key == categoryKey) it.copy(id = id) else it })
    }

    // endregion

    // region Items

    fun addItem(categoryKey: Long) = edit { doc ->
        val item = ContentItem(key = doc.nextKey())
        updateState { it.copy(query = "") }
        doc.copy(categories = doc.categories.map { if (it.key == categoryKey) it.copy(items = it.items + item) else it })
    }

    fun onItemFieldChanged(categoryKey: Long, itemKey: Long, field: String, value: String) =
        editCategory(categoryKey) { category ->
            val published = publishedItem(itemKey)?.fields
            category.copy(items = category.items.map {
                if (it.key == itemKey) it.copy(fields = it.fields.withField(field, value, published)) else it
            })
        }

    fun moveItem(categoryKey: Long, itemKey: Long, offset: Int) = editCategory(categoryKey) { category ->
        category.copy(items = category.items.moved(category.items.indexOfFirst { it.key == itemKey }, offset))
    }

    fun moveItemToCategory(fromKey: Long, itemKey: Long, toKey: Long) = edit { doc ->
        val item = doc.category(fromKey)?.items?.firstOrNull { it.key == itemKey } ?: return@edit doc
        val target = doc.category(toKey)?.let(kind::titleOf).orEmpty()
        updateState { it.copy(message = "Moved to \"$target\"") }
        doc.copy(categories = doc.categories.map {
            when (it.key) {
                fromKey -> it.copy(items = it.items.filter { i -> i.key != itemKey })
                toKey -> it.copy(items = it.items + item)
                else -> it
            }
        })
    }

    fun duplicateItem(categoryKey: Long, itemKey: Long) = edit { doc ->
        val copyKey = doc.nextKey()
        doc.copy(categories = doc.categories.map { category ->
            if (category.key != categoryKey) return@map category
            val index = category.items.indexOfFirst { it.key == itemKey }
            if (index < 0) return@map category
            category.copy(items = category.items.toMutableList().apply {
                add(index + 1, get(index).copy(key = copyKey))
            })
        })
    }

    fun deleteItem(categoryKey: Long, itemKey: Long) = edit(undoable = true) { doc ->
        updateState { it.copy(message = "Deleted ${kind.itemNoun}") }
        doc.copy(categories = doc.categories.map { category ->
            if (category.key == categoryKey) category.copy(items = category.items.filter { it.key != itemKey })
            else category
        })
    }

    // endregion

    // region Undo, discard and messages

    fun undo() {
        val undo = currentState().undo ?: return
        updateState { it.copy(selectedCategoryKey = undo.selectedCategoryKey, undo = null).withDraft(undo.document) }
        scheduleDraftSave()
    }

    fun onMessageShown() = updateState { it.copy(message = null, undo = null) }

    fun discardChanges() {
        val published = currentState().published?.document ?: return
        updateState {
            it.copy(
                restoredDraftSavedAt = null,
                restoredDraftIsStale = false,
                selectedCategoryKey = it.selectedCategoryKey?.takeIf { k -> published.category(k) != null }
                    ?: published.categories.firstOrNull()?.key,
                message = "Changes discarded"
            ).withDraft(published)
        }
        draftSaveJob?.cancel()
        scope.launch { draftStore.clear(kind) }
    }

    // endregion

    // region Publishing

    fun requestPublish() {
        val state = currentState()
        val firstError = state.issues.firstOrNull { it.isError }
        if (firstError != null) {
            updateState {
                it.copy(
                    selectedCategoryKey = firstError.categoryKey,
                    query = "",
                    message = "Fix ${state.errorCount} problem${if (state.errorCount == 1) "" else "s"} before publishing"
                )
            }
            return
        }
        if (state.canPublish) updateState { it.copy(confirmPublish = true) }
    }

    fun dismissPublish() = updateState { it.copy(confirmPublish = false, conflict = false) }

    fun confirmPublish(overwrite: Boolean = false) {
        val state = currentState()
        val draft = state.draft ?: return
        val base = state.published ?: return
        updateState { it.copy(confirmPublish = false, conflict = false, isPublishing = true) }

        scope.launch {
            val result = publishContent(PublishContentParams(kind, draft, base, overwrite))
            when (result) {
                is Result.Success -> {
                    draftSaveJob?.cancel()
                    draftStore.clear(kind)
                    updateState {
                        it.copy(
                            isPublishing = false,
                            published = result.data,
                            restoredDraftSavedAt = null,
                            restoredDraftIsStale = false,
                            message = "Published. Apps download version ${result.data.version} next time they open"
                        ).withDraft(it.draft ?: draft)
                    }
                }
                is Result.Error -> when (val e = result.exception) {
                    is ContentConflictException -> updateState { it.copy(isPublishing = false, conflict = true) }
                    is ContentVersionBumpException -> {
                        draftSaveJob?.cancel()
                        draftStore.clear(kind)
                        updateState {
                            it.copy(
                                isPublishing = false,
                                published = e.uploaded,
                                versionBumpPending = true,
                                restoredDraftSavedAt = null
                            ).withDraft(it.draft ?: draft)
                        }
                    }
                    else -> updateState {
                        it.copy(isPublishing = false, message = "Publish failed: ${e.readable()}")
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun retryVersionBump() = scope.launch {
        updateState { it.copy(isPublishing = true) }
        when (val result = bumpVersionUseCase(kind)) {
            is Result.Success -> updateState {
                it.copy(
                    isPublishing = false,
                    versionBumpPending = false,
                    published = it.published?.copy(version = result.data),
                    message = "Version bumped to ${result.data}. Apps will download the update"
                )
            }
            is Result.Error -> updateState {
                it.copy(isPublishing = false, message = "Still failing: ${result.exception.readable()}")
            }
            is Result.Loading -> Unit
        }
    }

    // endregion

    // region Backups

    fun openBackups() = scope.launch {
        updateState { it.copy(backups = emptyList(), isLoadingBackups = true) }
        when (val result = listBackups(kind)) {
            is Result.Success -> updateState { it.copy(backups = result.data, isLoadingBackups = false) }
            is Result.Error -> updateState {
                it.copy(backups = null, isLoadingBackups = false, message = "Couldn't list backups: ${result.exception.readable()}")
            }
            is Result.Loading -> Unit
        }
    }

    fun dismissBackups() = updateState { it.copy(backups = null) }

    /** Loads a backup as the draft; nothing goes live until the admin reviews and publishes it. */
    fun restoreBackup(backup: ContentBackup) = scope.launch {
        updateState { it.copy(isLoadingBackups = true) }
        when (val result = loadBackup(LoadContentBackupParams(kind, backup))) {
            is Result.Success -> {
                val current = currentState().draft ?: return@launch
                val restored = result.data.rekeyAgainst(currentState().published?.document ?: current)
                updateState {
                    it.copy(
                        backups = null,
                        isLoadingBackups = false,
                        undo = ContentEditorState.Undo(current, it.selectedCategoryKey),
                        selectedCategoryKey = restored.categories.firstOrNull()?.key,
                        message = "Loaded backup from ${backup.label}. Review it, then publish to make it live"
                    ).withDraft(restored)
                }
                scheduleDraftSave()
            }
            is Result.Error -> updateState {
                it.copy(isLoadingBackups = false, message = "Couldn't load backup: ${result.exception.readable()}")
            }
            is Result.Loading -> Unit
        }
    }

    // endregion

    private fun edit(undoable: Boolean = false, transform: (ContentDocument) -> ContentDocument) {
        val before = currentState().draft ?: return
        val selectedBefore = currentState().selectedCategoryKey
        val after = transform(before)
        if (after == before) return
        updateState {
            // Any later edit drops the pending undo, so Undo can never roll back newer typing.
            it.copy(undo = if (undoable) ContentEditorState.Undo(before, selectedBefore) else null)
                .withDraft(after)
        }
        scheduleDraftSave()
    }

    private fun editCategory(categoryKey: Long, transform: (ContentCategory) -> ContentCategory) = edit { doc ->
        doc.copy(categories = doc.categories.map { if (it.key == categoryKey) transform(it) else it })
    }

    private fun publishedCategory(key: Long) = currentState().published?.document?.category(key)

    private fun publishedItem(key: Long) =
        currentState().published?.document?.categories?.firstNotNullOfOrNull { c -> c.items.firstOrNull { it.key == key } }

    /** Recomputes everything derived from the draft so the UI never sees them out of sync. */
    private fun ContentEditorState.withDraft(draft: ContentDocument): ContentEditorState = copy(
        draft = draft,
        issues = draft.validate(kind),
        changes = published?.let { draft.changesFrom(it.document) } ?: changes
    )

    /** Writing a 175 KB draft on every keystroke is wasteful; save once typing pauses. */
    private fun scheduleDraftSave() {
        draftSaveJob?.cancel()
        draftSaveJob = scope.launch {
            delay(DRAFT_SAVE_DELAY_MS)
            saveDraftNow()
        }
    }

    private suspend fun saveDraftNow() {
        val state = currentState()
        val draft = state.draft ?: return
        if (state.changes.hasChanges) {
            draftStore.save(kind, ContentDraft(draft, state.published?.md5Hash, currentTimeMillis()))
        } else {
            draftStore.clear(kind)
        }
    }

    private fun <T> List<T>.moved(index: Int, offset: Int): List<T> {
        val target = index + offset
        if (index < 0 || target !in indices) return this
        return toMutableList().apply { add(target, removeAt(index)) }
    }

    private fun Exception.readable(): String = message?.takeIf { it.isNotBlank() } ?: "Unknown error"

    private companion object {
        const val DRAFT_SAVE_DELAY_MS = 800L
    }
}
