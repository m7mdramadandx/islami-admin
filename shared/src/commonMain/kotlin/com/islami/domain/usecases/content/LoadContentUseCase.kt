package com.islami.domain.usecases.content

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.ContentKind
import com.islami.domain.entities.ContentSnapshot
import com.islami.domain.repositories.ContentRepository

class LoadContentUseCase(
    private val contentRepository: ContentRepository
) : UseCase<ContentKind, ContentSnapshot>() {
    override suspend fun invoke(params: ContentKind): Result<ContentSnapshot> =
        contentRepository.load(params)
}
