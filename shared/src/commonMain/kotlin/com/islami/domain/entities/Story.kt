package com.islami.domain.entities

/** A story in `root/ar/stories`; the app lists them in id order and renders [body] as HTML. */
data class Story(
    val id: String,
    val title: String,
    val brief: String,
    /** Download URL of the cover image shown on the stories list. */
    val image: String,
    val body: String
)

enum class StoryBlockType { HEADING, PARAGRAPH, QUOTE }

/** One piece of a story body. Paragraph and quote text mark bold with `**double stars**`. */
data class StoryBlock(val type: StoryBlockType, val text: String)

/** Another admin saved or deleted the story after it was loaded. */
class StoryConflictException : Exception("This story changed since you opened it")

/** Creating a story whose id (its title) is already taken. */
class StoryExistsException(title: String) : Exception("A story called \"$title\" already exists")
