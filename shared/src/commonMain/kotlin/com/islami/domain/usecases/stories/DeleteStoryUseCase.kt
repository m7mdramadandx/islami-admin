package com.islami.domain.usecases.stories

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.Story
import com.islami.domain.repositories.StoriesRepository

/** Takes the story as last loaded, so a newer edit by someone else isn't deleted unseen. */
class DeleteStoryUseCase(
    private val storiesRepository: StoriesRepository
) : UseCase<Story, Unit>() {
    override suspend fun invoke(params: Story): Result<Unit> = storiesRepository.deleteStory(params)
}
