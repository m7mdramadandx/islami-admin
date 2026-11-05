package com.islami.domain.usecases.stories

import com.islami.domain.entities.Story

data class SaveStoryParams(
    val story: Story,
    val base: Story,
    /** Save even though someone else changed the story since [base] was loaded. */
    val overwrite: Boolean = false
)
