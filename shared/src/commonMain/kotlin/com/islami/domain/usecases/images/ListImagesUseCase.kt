package com.islami.domain.usecases.images

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.ImageFolder
import com.islami.domain.entities.StorageImage
import com.islami.domain.repositories.ImageLibraryRepository

class ListImagesUseCase(
    private val imageLibraryRepository: ImageLibraryRepository
) : UseCase<ImageFolder, List<StorageImage>>() {
    override suspend fun invoke(params: ImageFolder): Result<List<StorageImage>> = imageLibraryRepository.list(params)
}
