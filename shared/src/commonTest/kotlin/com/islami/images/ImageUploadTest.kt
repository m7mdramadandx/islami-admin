package com.islami.images

import com.islami.core.error.Result
import com.islami.domain.entities.ImageFolder
import com.islami.domain.entities.StorageImage
import com.islami.domain.images.ImageType
import com.islami.domain.images.MAX_IMAGE_BYTES
import com.islami.domain.images.detectImageType
import com.islami.domain.images.imageUploadProblem
import com.islami.domain.images.storagePathOf
import com.islami.domain.repositories.ImageLibraryRepository
import com.islami.domain.usecases.images.UploadImageParams
import com.islami.domain.usecases.images.UploadImageUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageUploadTest {

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    private val jpeg = bytes(0xFF, 0xD8, 0xFF, 0xE0, 0, 0x10)
    private val png = bytes(0x89, 'P'.code, 'N'.code, 'G'.code, 0x0D, 0x0A)
    private val webp = "RIFF".encodeToByteArray() + ByteArray(4) + "WEBPVP8 ".encodeToByteArray()

    @Test
    fun detectsFormatsFromFileSignatures() {
        assertEquals(ImageType.JPEG, detectImageType(jpeg))
        assertEquals(ImageType.PNG, detectImageType(png))
        assertEquals(ImageType.WEBP, detectImageType(webp))
        assertEquals(null, detectImageType("GIF89a".encodeToByteArray()))
        assertEquals(null, detectImageType(ByteArray(2)))
    }

    @Test
    fun rejectsEmptyUnknownAndOversizedFiles() {
        assertEquals(null, imageUploadProblem(jpeg))
        assertTrue(imageUploadProblem(ByteArray(0))!!.contains("empty"))
        assertTrue(imageUploadProblem("hello".encodeToByteArray())!!.contains("JPEG, PNG or WebP"))
        assertTrue(imageUploadProblem(jpeg + ByteArray(MAX_IMAGE_BYTES))!!.contains("1.0 MB"))
    }

    @Test
    fun badFilesNeverReachStorageAndGoodOnesLandInTheRightFolder() = runTest {
        val repo = RecordingRepository()
        val result = UploadImageUseCase(repo)(UploadImageParams(ImageFolder.STORY_IMAGES, "not an image".encodeToByteArray()))
        assertTrue(result is Result.Error)
        assertEquals(null, repo.uploaded)

        UploadImageUseCase(repo)(UploadImageParams(ImageFolder.STORY_IMAGES, png))
        assertEquals(ImageFolder.STORY_IMAGES to ImageType.PNG, repo.uploaded)
    }

    @Test
    fun storagePathIsReadFromDownloadUrls() {
        val url = "https://firebasestorage.googleapis.com/v0/b/islami-ecc03.appspot.com/o/islamic_pictures%2F0c73.jpg?alt=media&token=9f13"
        assertEquals("islamic_pictures/0c73.jpg", storagePathOf(url))
        assertEquals(null, storagePathOf(""))
    }

    private class RecordingRepository : ImageLibraryRepository {
        var uploaded: Pair<ImageFolder, ImageType>? = null
        override suspend fun list(folder: ImageFolder): Result<List<StorageImage>> = Result.Success(emptyList())
        override suspend fun upload(folder: ImageFolder, bytes: ByteArray, type: ImageType): Result<StorageImage> {
            uploaded = folder to type
            return Result.Success(StorageImage("${folder.path}/x.${type.extension}", "https://x"))
        }
    }
}
