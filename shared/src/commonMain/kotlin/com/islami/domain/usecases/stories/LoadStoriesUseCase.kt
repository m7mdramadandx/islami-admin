package com.islami.domain.usecases.stories

import com.islami.core.error.Result
import com.islami.core.usecase.NoParams
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.Story
import com.islami.domain.repositories.StoriesRepository

class LoadStoriesUseCase(
    private val storiesRepository: StoriesRepository
) : UseCase<NoParams, List<Story>>() {
    override suspend fun invoke(params: NoParams): Result<List<Story>> = storiesRepository.loadStories()
}
