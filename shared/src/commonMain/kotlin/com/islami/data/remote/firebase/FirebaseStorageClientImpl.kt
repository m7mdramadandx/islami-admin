package com.islami.data.remote.firebase

import com.islami.core.error.Result
import dev.gitlive.firebase.storage.FirebaseStorage

class FirebaseStorageClientImpl(
    private val storage: FirebaseStorage
) : FirebaseStorageClient {

    override suspend fun uploadFile(
        path: String,
        fileName: String,
        data: ByteArray
    ): Result<String> = try {
        val ref = storage.reference("$path/$fileName")
        ref.putData(data)
        val url = ref.getDownloadURL()
        Result.Success(url)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun downloadFile(
        path: String,
        fileName: String
    ): Result<ByteArray> = try {
        val ref = storage.reference("$path/$fileName")
        // GitLive might have different download methods
        // Usually it's better to use the URL and Ktor for downloading bytes
        // But if we want to use the SDK:
        // ByteArray is not always directly supported in KMP SDK for download yet in some versions
        // Let's assume it is or use a placeholder
        Result.Error(Exception("Download not implemented yet using SDK - use download URL"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteFile(
        path: String,
        fileName: String
    ): Result<Unit> = try {
        storage.reference("$path/$fileName").delete()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun getDownloadUrl(
        path: String,
        fileName: String
    ): Result<String> = try {
        val url = storage.reference("$path/$fileName").getDownloadURL()
        Result.Success(url)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
