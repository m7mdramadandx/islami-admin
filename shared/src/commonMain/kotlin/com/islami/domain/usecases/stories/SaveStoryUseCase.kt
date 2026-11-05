package com.islami.domain.usecases.stories

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.Story
import com.islami.domain.repositories.StoriesRepository

class SaveStoryUseCase(
    private val storiesRepository: StoriesRepository
) : UseCase<SaveStoryParams, Story>() {
    override suspend fun invoke(params: SaveStoryParams): Result<Story> {
        val story = params.story
        // An empty title or brief shows as a blank row in the app's list.
        if (story.title.isBlank() || story.brief.isBlank() || story.body.isBlank()) {
            return Result.Error(IllegalStateException("A story needs a title, a brief and some text"))
        }
        return storiesRepository.saveStory(story, params.base, params.overwrite)
    }
}
