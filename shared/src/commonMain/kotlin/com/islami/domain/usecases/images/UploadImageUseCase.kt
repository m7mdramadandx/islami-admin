package com.islami.domain.usecases.images

import com.islami.core.error.Result
import com.islami.core.usecase.UseCase
import com.islami.domain.entities.StorageImage
import com.islami.domain.images.detectImageType
import com.islami.domain.images.imageUploadProblem
import com.islami.domain.repositories.ImageLibraryRepository

class UploadImageUseCase(
    private val imageLibraryRepository: ImageLibraryRepository
) : UseCase<UploadImageParams, StorageImage>() {
    override suspend fun invoke(params: UploadImageParams): Result<StorageImage> {
        imageUploadProblem(params.bytes)?.let { return Result.Error(IllegalArgumentException(it)) }
        return imageLibraryRepository.upload(params.folder, params.bytes, detectImageType(params.bytes)!!)
    }
}
