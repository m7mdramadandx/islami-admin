package com.islami.domain.stories

import com.islami.domain.entities.StoryBlock

data class StoryIssue(val isError: Boolean, val message: String)

/** [blocks] is null while the body is unconverted imported HTML. */
fun validateStory(
    title: String,
    brief: String,
    image: String,
    blocks: List<StoryBlock>?,
    existingIds: Collection<String>,
    isNew: Boolean
): List<StoryIssue> = buildList {
    val trimmedTitle = title.trim()
    if (trimmedTitle.isEmpty()) add(StoryIssue(true, "The title is empty"))
    if (brief.isBlank()) add(StoryIssue(true, "The brief is empty; it's the line under the title in the stories list"))
    if (blocks != null && blocks.none { it.text.isNotBlank() }) add(StoryIssue(true, "The story has no text"))
    if (image.isBlank()) add(StoryIssue(false, "No cover image; the list will show an empty space"))
    if (isNew && trimmedTitle.isNotEmpty()) {
        idProblem(trimmedTitle, existingIds)?.let { add(StoryIssue(true, it)) }
    }
}

/** New stories use their title as id, like existing ones, so the app's id order stays alphabetical. */
fun storyIdFor(title: String): String = title.trim()

private fun idProblem(title: String, existingIds: Collection<String>): String? = when {
    '/' in title -> "The title can't contain \"/\" (it becomes the story's id)"
    title == "." || title == ".." || (title.startsWith("__") && title.endsWith("__")) -> "That title can't be used as an id"
    existingIds.any { it.trim() == title } -> "A story with this title already exists"
    else -> null
}
