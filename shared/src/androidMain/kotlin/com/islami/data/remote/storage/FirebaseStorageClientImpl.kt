package com.islami.data.remote.storage

import com.islami.core.error.ErrorResponse
import com.islami.core.error.Result
import com.islami.data.remote.firebase.FirebaseStorageClient
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.storage

actual class FirebaseStorageClientImpl : FirebaseStorageClient {
    private val storage = FirebaseStorage.storage

    override suspend fun uploadFile(
        path: String,
        fileName: String,
        data: ByteArray
    ): Result<String> = try {
        val ref = storage.reference.child(path).child(fileName)
        ref.putBytes(data)
        val downloadUrl = ref.getDownloadUrl()
        Result.Success(downloadUrl)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to upload file: ${e.message}"))
    }

    override suspend fun downloadFile(
        path: String,
        fileName: String
    ): Result<ByteArray> = try {
        val ref = storage.reference.child(path).child(fileName)
        val data = ref.getBytes(Long.MAX_VALUE)
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to download file: ${e.message}"))
    }

    override suspend fun deleteFile(
        path: String,
        fileName: String
    ): Result<Unit> = try {
        val ref = storage.reference.child(path).child(fileName)
        ref.delete()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to delete file: ${e.message}"))
    }

    override suspend fun getDownloadUrl(
        path: String,
        fileName: String
    ): Result<String> = try {
        val ref = storage.reference.child(path).child(fileName)
        val url = ref.getDownloadUrl()
        Result.Success(url)
    } catch (e: Exception) {
        Result.Error(ErrorResponse(message = "Failed to get download URL: ${e.message}"))
    }
}
