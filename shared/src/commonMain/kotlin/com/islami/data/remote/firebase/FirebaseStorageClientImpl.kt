package com.islami.data.remote.firebase

import com.islami.core.error.Result
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.FirebaseStorageMetadata
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlin.coroutines.cancellation.CancellationException

/** Storage's upload API takes a platform buffer (ByteArray, Uint8Array or NSData). */
expect fun ByteArray.toStorageData(): Data

class FirebaseStorageClientImpl(
    private val storage: FirebaseStorage,
    /**
     * A bare client on purpose: the shared API client sends `Content-Type: application/json` on
     * every request, which makes browsers preflight the download and the bucket CORS rejects it.
     */
    private val http: HttpClient = HttpClient()
) : FirebaseStorageClient {

    override suspend fun upload(path: String, data: ByteArray, contentType: String?): Result<Unit> = attempt {
        storage.reference(path).putData(data.toStorageData(), FirebaseStorageMetadata(contentType = contentType))
    }

    // gitlive has no common getBytes(), so fetch through the (token-bearing) download URL.
    override suspend fun download(path: String): Result<ByteArray> = attempt {
        val response = http.get(storage.reference(path).getDownloadUrl())
        if (!response.status.isSuccess()) throw Exception("Download of $path failed: HTTP ${response.status.value}")
        response.body<ByteArray>()
    }

    override suspend fun md5Hash(path: String): Result<String?> = attempt {
        storage.reference(path).getMetadata()?.md5Hash
    }

    override suspend fun list(folder: String): Result<List<String>> = attempt {
        storage.reference(folder).listAll().items.map { it.path }
    }

    override suspend fun downloadUrl(path: String): Result<String> = attempt {
        storage.reference(path).getDownloadUrl()
    }

    private inline fun <T> attempt(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(e)
    }
}
