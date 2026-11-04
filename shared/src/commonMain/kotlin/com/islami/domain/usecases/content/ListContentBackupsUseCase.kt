package com.islami.domain.usecases.content

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.ContentBackup
import com.islami.domain.entities.ContentKind
import com.islami.domain.repositories.ContentRepository

class ListContentBackupsUseCase(
    private val contentRepository: ContentRepository
) : UseCase<ContentKind, List<ContentBackup>>() {
    override suspend fun invoke(params: ContentKind): Result<List<ContentBackup>> =
        contentRepository.listBackups(params)
}
