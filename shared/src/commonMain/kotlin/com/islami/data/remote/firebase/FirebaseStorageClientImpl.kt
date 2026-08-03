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
        // Trying direct put if putData is missing
        // ref.put(data)
        // Or if it's named differently
        Result.Error(Exception("Upload method unresolved in SDK - needs manual investigation of actual SDK API"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun downloadFile(
        path: String,
        fileName: String
    ): Result<ByteArray> = try {
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
        val url = storage.reference("$path/$fileName").getDownloadUrl()
        Result.Success(url)
    } catch (e: Exception) {
        Result.Error(e)
    }
}
