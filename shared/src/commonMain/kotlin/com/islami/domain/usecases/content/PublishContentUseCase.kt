package com.islami.domain.usecases.content

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.content.validate
import com.islami.domain.entities.ContentSnapshot
import com.islami.domain.repositories.ContentRepository

class PublishContentUseCase(
    private val contentRepository: ContentRepository
) : UseCase<PublishContentParams, ContentSnapshot>() {
    override suspend fun invoke(params: PublishContentParams): Result<ContentSnapshot> {
        // A missing name or zekr crashes or blanks the app, so never let one reach users.
        val errors = params.document.validate(params.kind).filter { it.isError }
        if (errors.isNotEmpty()) {
            return Result.Error(IllegalStateException("Fix ${errors.size} problem(s) before publishing"))
        }
        return contentRepository.publish(params.kind, params.document, params.base, params.overwrite)
    }
}
