package com.islami.data.remote

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

/**
 * Loads remote images for previews and pickers. Each image is shrunk to the requested width right
 * after decoding, so a grid of full-size backgrounds doesn't hold hundreds of MB of bitmaps.
 */
class RemoteImageLoader(
    // A bare client: the Storage bucket's CORS allows simple GETs only.
    private val http: HttpClient = HttpClient()
) {
    private val cache = mutableMapOf<String, ImageBitmap>()
    private val lock = Mutex()

    /** Null if the image can't be downloaded or decoded. */
    suspend fun load(url: String, maxWidth: Int): ImageBitmap? {
        val key = "$maxWidth|$url"
        lock.withLock { cache[key] }?.let { return it }
        return try {
            val response = http.get(url)
            if (!response.status.isSuccess()) return null
            val bytes = response.body<ByteArray>()
            val image = withContext(Dispatchers.Default) { bytes.decodeToImageBitmap().scaledToWidth(maxWidth) }
            lock.withLock { cache[key] = image }
            image
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private fun ImageBitmap.scaledToWidth(maxWidth: Int): ImageBitmap {
        if (width <= maxWidth) return this
        val target = IntSize(maxWidth, (height * maxWidth / width.toFloat()).roundToInt().coerceAtLeast(1))
        val scaled = ImageBitmap(target.width, target.height)
        Canvas(scaled).drawImageRect(this, dstSize = target, paint = Paint().apply { filterQuality = FilterQuality.Medium })
        return scaled
    }
}
