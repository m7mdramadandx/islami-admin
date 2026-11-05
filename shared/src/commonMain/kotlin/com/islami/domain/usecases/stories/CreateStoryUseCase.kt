package com.islami.domain.usecases.stories

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.Story
import com.islami.domain.repositories.StoriesRepository

class CreateStoryUseCase(
    private val storiesRepository: StoriesRepository
) : UseCase<Story, Story>() {
    override suspend fun invoke(params: Story): Result<Story> {
        if (params.title.isBlank() || params.brief.isBlank() || params.body.isBlank()) {
            return Result.Error(IllegalStateException("A story needs a title, a brief and some text"))
        }
        if ('/' in params.id) return Result.Error(IllegalArgumentException("The title can't contain \"/\""))
        return storiesRepository.createStory(params)
    }
}
