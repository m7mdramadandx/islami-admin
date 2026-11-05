package com.islami.presentation.stories

import com.islami.domain.entities.StorageImage
import com.islami.domain.entities.Story
import com.islami.domain.entities.StoryBlock
import com.islami.domain.entities.StoryBlockType
import com.islami.domain.stories.StoryIssue
import com.islami.domain.stories.htmlToStoryBlocks
import com.islami.domain.stories.isEditorFormat
import com.islami.domain.stories.storyBlocksToHtml
import com.islami.domain.stories.storyIdFor

/** A body block with an editor-only key, so text fields keep their state while blocks move. */
data class EditableBlock(val key: Long, val type: StoryBlockType, val text: String) {
    fun toBlock() = StoryBlock(type, text)
}

data class StoryDraft(
    /** The story as loaded; null while creating a new one. */
    val base: Story?,
    val title: String,
    val brief: String,
    val image: String,
    /** Null while the body is still imported website HTML, which is then saved unchanged. */
    val blocks: List<EditableBlock>?
) {
    val isNew: Boolean get() = base == null

    fun toStory(): Story = Story(
        id = base?.id ?: storyIdFor(title),
        title = title.trim(),
        brief = brief.trim(),
        image = image,
        body = blocks?.let { storyBlocksToHtml(it.map(EditableBlock::toBlock)) } ?: base?.body.orEmpty()
    )

    val hasChanges: Boolean
        get() = if (base == null) {
            title.isNotBlank() || brief.isNotBlank() || image.isNotBlank() || blocks.orEmpty().any { it.text.isNotBlank() }
        } else {
            // Untrimmed, so opening " إدريس" doesn't count as an edit.
            title != base.title || brief != base.brief || image != base.image ||
                (blocks != null && toStory().body != base.body)
        }

    companion object {
        fun of(story: Story, nextKey: () -> Long) = StoryDraft(
            base = story,
            title = story.title,
            brief = story.brief,
            image = story.image,
            blocks = if (isEditorFormat(story.body)) story.body.toEditableBlocks(nextKey) else null
        )

        fun new(nextKey: () -> Long) = StoryDraft(
            base = null, title = "", brief = "", image = "",
            blocks = listOf(EditableBlock(nextKey(), StoryBlockType.PARAGRAPH, ""))
        )

        fun String.toEditableBlocks(nextKey: () -> Long) =
            htmlToStoryBlocks(this).map { EditableBlock(nextKey(), it.type, it.text) }
    }
}

data class StoriesState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    /** Live stories in the app's order (document id). */
    val stories: List<Story> = emptyList(),
    val draft: StoryDraft? = null,
    val issues: List<StoryIssue> = emptyList(),
    /** Set when leaving an edited story was requested; the dialog asks before discarding. */
    val pendingSwitch: PendingSwitch? = null,
    /** Null until the image picker is first opened. */
    val images: List<StorageImage>? = null,
    val pickerOpen: Boolean = false,
    val isUploading: Boolean = false,
    val confirmSave: Boolean = false,
    val confirmDelete: Boolean = false,
    val isSaving: Boolean = false,
    /** Someone else changed or deleted the story since it was loaded. */
    val conflict: Boolean = false,
    /** Last deleted block, offered back through the snackbar's Undo. */
    val undoBlock: Pair<Int, EditableBlock>? = null,
    val message: String? = null
) {
    val selectedId: String? get() = draft?.base?.id

    val hasChanges: Boolean get() = draft?.hasChanges == true

    val errorCount: Int get() = issues.count { it.isError }

    sealed interface PendingSwitch {
        data class Open(val id: String) : PendingSwitch
        data object New : PendingSwitch
        data object Close : PendingSwitch
    }
}
