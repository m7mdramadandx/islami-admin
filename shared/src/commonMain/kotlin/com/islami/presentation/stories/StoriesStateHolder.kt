package com.islami.presentation.stories

import com.islami.core.error.Result
import com.islami.core.state.StateHolder
import com.islami.core.usecase.NoParams
import com.islami.domain.entities.ImageFolder
import com.islami.domain.entities.Story
import com.islami.domain.entities.StoryBlockType
import com.islami.domain.entities.StoryConflictException
import com.islami.domain.stories.validateStory
import com.islami.domain.usecases.images.ListImagesUseCase
import com.islami.domain.usecases.images.UploadImageParams
import com.islami.domain.usecases.images.UploadImageUseCase
import com.islami.domain.usecases.stories.CreateStoryUseCase
import com.islami.domain.usecases.stories.DeleteStoryUseCase
import com.islami.domain.usecases.stories.LoadStoriesUseCase
import com.islami.domain.usecases.stories.SaveStoryParams
import com.islami.domain.usecases.stories.SaveStoryUseCase
import com.islami.presentation.components.PickedImage
import com.islami.presentation.stories.StoriesState.PendingSwitch
import com.islami.presentation.stories.StoryDraft.Companion.toEditableBlocks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StoriesStateHolder(
    private val loadStories: LoadStoriesUseCase,
    private val saveStory: SaveStoryUseCase,
    private val createStory: CreateStoryUseCase,
    private val deleteStory: DeleteStoryUseCase,
    private val listImages: ListImagesUseCase,
    private val uploadImage: UploadImageUseCase,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) : StateHolder<StoriesState>(StoriesState()) {

    private var keyCounter = 0L
    private val nextKey: () -> Long = { ++keyCounter }

    init {
        load()
    }

    fun load() = scope.launch {
        updateState { it.copy(isLoading = true, loadError = null) }
        when (val result = loadStories(NoParams)) {
            is Result.Success -> updateState { it.copy(isLoading = false, stories = result.data) }
            is Result.Error -> updateState { it.copy(isLoading = false, loadError = result.exception.readable()) }
            is Result.Loading -> Unit
        }
    }

    // region Choosing what to edit

    fun open(id: String) = switchTo(PendingSwitch.Open(id))

    fun newStory() = switchTo(PendingSwitch.New)

    fun close() = switchTo(PendingSwitch.Close)

    /** Leaving an edited story asks first; the confirmed switch is then applied by [confirmSwitch]. */
    private fun switchTo(target: PendingSwitch) {
        if (currentState().hasChanges) updateState { it.copy(pendingSwitch = target) } else apply(target)
    }

    fun confirmSwitch() {
        val target = currentState().pendingSwitch ?: return
        updateState { it.copy(pendingSwitch = null) }
        apply(target)
    }

    fun cancelSwitch() = updateState { it.copy(pendingSwitch = null) }

    private fun apply(target: PendingSwitch) {
        val draft = when (target) {
            is PendingSwitch.Open -> currentState().stories.firstOrNull { it.id == target.id }?.let { StoryDraft.of(it, nextKey) }
            PendingSwitch.New -> StoryDraft.new(nextKey)
            PendingSwitch.Close -> null
        }
        updateState { it.copy(undoBlock = null).withDraft(draft) }
    }

    fun confirmDiscardNew() = updateState { it.withDraft(null) }

    /** After a conflict: drop local edits, reload, and reopen the story as it is now. */
    fun reloadDiscarding() {
        val id = currentState().selectedId
        updateState { it.withDraft(null) }
        scope.launch {
            load().join()
            if (id != null) apply(PendingSwitch.Open(id))
        }
    }

    fun discardChanges() {
        val base = currentState().draft?.base
        updateState { it.withDraft(base?.let { story -> StoryDraft.of(story, nextKey) }) }
    }

    // endregion

    // region Fields and body

    fun onTitleChanged(value: String) = editDraft { it.copy(title = value) }

    fun onBriefChanged(value: String) = editDraft { it.copy(brief = value) }

    /** Turns imported website HTML into editable blocks; nothing changes in the app until saved. */
    fun convertBody() {
        val body = currentState().draft?.base?.body ?: return
        editDraft { it.copy(blocks = body.toEditableBlocks(nextKey)) }
        updateState { it.copy(message = "Converted. Check the text, then save to replace the old version") }
    }

    fun onBlockTextChanged(key: Long, text: String) = editBlocks { blocks ->
        blocks.map { if (it.key == key) it.copy(text = text) else it }
    }

    fun onBlockTypeChanged(key: Long, type: StoryBlockType) = editBlocks { blocks ->
        blocks.map { if (it.key == key) it.copy(type = type) else it }
    }

    /** Adds a block after [afterKey], or at the end when it's null. */
    fun addBlock(type: StoryBlockType, afterKey: Long? = null) = editBlocks { blocks ->
        val index = afterKey?.let { key -> blocks.indexOfFirst { it.key == key } + 1 }?.takeIf { it > 0 } ?: blocks.size
        blocks.toMutableList().apply { add(index, EditableBlock(nextKey(), type, "")) }
    }

    fun moveBlock(key: Long, offset: Int) = editBlocks { blocks ->
        val index = blocks.indexOfFirst { it.key == key }
        val target = index + offset
        if (index < 0 || target !in blocks.indices) blocks
        else blocks.toMutableList().apply { add(target, removeAt(index)) }
    }

    fun deleteBlock(key: Long) {
        val blocks = currentState().draft?.blocks ?: return
        val index = blocks.indexOfFirst { it.key == key }
        if (index < 0) return
        editBlocks { it.filter { block -> block.key != key } }
        // Empty blocks go quietly; deleting text gets an Undo.
        if (blocks[index].text.isNotBlank()) {
            updateState { it.copy(undoBlock = index to blocks[index], message = "Block deleted") }
        }
    }

    fun undoDeleteBlock() {
        val (index, block) = currentState().undoBlock ?: return
        editBlocks { it.toMutableList().apply { add(index.coerceAtMost(size), block) } }
        updateState { it.copy(undoBlock = null) }
    }

    // endregion

    // region Cover image

    fun openPicker() {
        updateState { it.copy(pickerOpen = true) }
        if (currentState().images == null) scope.launch {
            when (val result = listImages(ImageFolder.STORY_IMAGES)) {
                is Result.Success -> updateState { it.copy(images = result.data) }
                is Result.Error -> updateState {
                    it.copy(images = emptyList(), message = "Couldn't list images: ${result.exception.readable()}")
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun dismissPicker() = updateState { it.copy(pickerOpen = false) }

    fun chooseImage(url: String) = editDraft { it.copy(image = url) }

    fun onImagePicked(image: PickedImage?) {
        if (image == null) {
            updateState { it.copy(message = "Couldn't read that file") }
            return
        }
        // The admin may open another story while this uploads; the image belongs to this draft.
        val draftId = currentState().draft?.base?.id
        updateState { it.copy(isUploading = true) }
        scope.launch {
            when (val result = uploadImage(UploadImageParams(ImageFolder.STORY_IMAGES, image.bytes))) {
                is Result.Success -> {
                    val uploaded = result.data
                    updateState { it.copy(isUploading = false, images = it.images?.let { list -> list + uploaded }) }
                    if (currentState().draft?.base?.id == draftId) {
                        chooseImage(uploaded.url)
                        updateState { it.copy(message = "Uploaded and set as the cover. Save to publish it") }
                    } else {
                        updateState { it.copy(message = "Uploaded; it's in the image list") }
                    }
                }
                is Result.Error -> updateState { it.copy(isUploading = false, message = "Upload failed: ${result.exception.readable()}") }
                is Result.Loading -> Unit
            }
        }
    }

    // endregion

    // region Saving and deleting

    fun requestSave() {
        val state = currentState()
        state.issues.firstOrNull { it.isError }?.let { issue ->
            updateState { it.copy(message = issue.message) }
            return
        }
        if (state.hasChanges && !state.isSaving) updateState { it.copy(confirmSave = true) }
    }

    fun dismissDialogs() = updateState { it.copy(confirmSave = false, confirmDelete = false, conflict = false) }

    fun confirmSave(overwrite: Boolean = false) {
        val draft = currentState().draft ?: return
        val story = draft.toStory()
        updateState { it.copy(confirmSave = false, conflict = false, isSaving = true) }
        scope.launch {
            val result = draft.base?.let { saveStory(SaveStoryParams(story, it, overwrite)) } ?: createStory(story)
            when (result) {
                is Result.Success -> {
                    val saved = result.data
                    updateState { state ->
                        val stories = (state.stories.filter { it.id != saved.id } + saved).sortedBy { it.id }
                        state.copy(
                            isSaving = false,
                            stories = stories,
                            message = if (draft.isNew) "Story added. It's in the app now" else "Saved. The app shows it right away"
                        ).withDraft(StoryDraft.of(saved, nextKey))
                    }
                }
                is Result.Error -> when (result.exception) {
                    is StoryConflictException -> updateState { it.copy(isSaving = false, conflict = true) }
                    else -> updateState { it.copy(isSaving = false, message = "Save failed: ${result.exception.readable()}") }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun requestDelete() {
        if (currentState().draft?.base != null) updateState { it.copy(confirmDelete = true) }
    }

    fun confirmDelete() {
        val story = currentState().draft?.base ?: return
        updateState { it.copy(confirmDelete = false, isSaving = true) }
        scope.launch {
            when (val result = deleteStory(story)) {
                is Result.Success -> updateState { state ->
                    state.copy(
                        isSaving = false,
                        stories = state.stories.filter { it.id != story.id },
                        message = "Deleted \"${story.title.trim()}\". A backup is in Storage stories/backups/"
                    ).withDraft(null)
                }
                is Result.Error -> when (result.exception) {
                    is StoryConflictException -> updateState { it.copy(isSaving = false, conflict = true) }
                    else -> updateState { it.copy(isSaving = false, message = "Delete failed: ${result.exception.readable()}") }
                }
                is Result.Loading -> Unit
            }
        }
    }

    // endregion

    fun onMessageShown() = updateState { it.copy(message = null, undoBlock = null) }

    private fun editDraft(transform: (StoryDraft) -> StoryDraft) = updateState { state ->
        state.withDraft(state.draft?.let(transform))
    }

    private fun editBlocks(transform: (List<EditableBlock>) -> List<EditableBlock>) = editDraft { draft ->
        draft.blocks?.let { draft.copy(blocks = transform(it)) } ?: draft
    }

    private fun StoriesState.withDraft(draft: StoryDraft?) = copy(
        draft = draft,
        issues = draft?.let {
            validateStory(
                title = it.title,
                brief = it.brief,
                image = it.image,
                blocks = it.blocks?.map(EditableBlock::toBlock),
                existingIds = stories.map(Story::id),
                isNew = it.isNew
            )
        }.orEmpty()
    )

    private fun Exception.readable(): String = message?.takeIf { it.isNotBlank() } ?: "Unknown error"
}
