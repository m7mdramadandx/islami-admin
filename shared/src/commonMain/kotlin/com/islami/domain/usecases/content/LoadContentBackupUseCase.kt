package com.islami.domain.usecases.content

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.ContentDocument
import com.islami.domain.repositories.ContentRepository

class LoadContentBackupUseCase(
    private val contentRepository: ContentRepository
) : UseCase<LoadContentBackupParams, ContentDocument>() {
    override suspend fun invoke(params: LoadContentBackupParams): Result<ContentDocument> =
        contentRepository.loadBackup(params.kind, params.backup)
}
