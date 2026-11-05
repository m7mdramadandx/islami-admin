package com.islami.data.repository

import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseStorageClient
import com.islami.domain.entities.ImageFolder
import com.islami.domain.entities.StorageImage
import com.islami.domain.images.ImageType
import com.islami.domain.repositories.ImageLibraryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random

class ImageLibraryRepositoryImpl(
    private val storage: FirebaseStorageClient
) : ImageLibraryRepository {

    // Only files directly in the folder: subfolders (like backups) aren't images to pick.
    override suspend fun list(folder: ImageFolder): Result<List<StorageImage>> = attempt {
        val paths = storage.list(folder.path).orThrow()
        coroutineScope {
            paths.sorted().map { path ->
                async {
                    (storage.downloadUrl(path) as? Result.Success)?.data?.let { StorageImage(path.removePrefix("/"), it) }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override suspend fun upload(folder: ImageFolder, bytes: ByteArray, type: ImageType): Result<StorageImage> = attempt {
        // Random 32-hex names, like the existing images, so uploads never overwrite one another.
        val name = (1..32).joinToString("") { Random.nextInt(16).toString(16) }
        val path = "${folder.path}/$name.${type.extension}"
        storage.upload(path, bytes, type.contentType).orThrow()
        StorageImage(path, storage.downloadUrl(path).orThrow())
    }

    private fun <T> Result<T>.orThrow(): T = when (this) {
        is Result.Success -> data
        is Result.Error -> throw exception
        is Result.Loading -> error("Unexpected loading result")
    }

    private inline fun <T> attempt(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e)
    }
}
