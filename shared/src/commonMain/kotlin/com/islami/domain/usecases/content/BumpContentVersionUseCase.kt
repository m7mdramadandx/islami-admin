package com.islami.domain.usecases.content

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.ContentKind
import com.islami.domain.repositories.ContentRepository

class BumpContentVersionUseCase(
    private val contentRepository: ContentRepository
) : UseCase<ContentKind, Int>() {
    override suspend fun invoke(params: ContentKind): Result<Int> =
        contentRepository.bumpVersion(params)
}
