package com.islami.domain.images

/** Image formats the app's image loader and every admin platform can decode. */
enum class ImageType(val extension: String, val contentType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp")
}

/** Users download every image on their phones; the existing ones are 17-390 KB. */
const val MAX_IMAGE_BYTES = 1_000_000

/** Detects the format from the file's first bytes rather than trusting its name. */
fun detectImageType(bytes: ByteArray): ImageType? {
    fun at(index: Int) = bytes.getOrNull(index)?.toInt()?.and(0xFF)
    fun ascii(from: Int, text: String) = text.indices.all { at(from + it) == text[it].code }
    return when {
        at(0) == 0xFF && at(1) == 0xD8 && at(2) == 0xFF -> ImageType.JPEG
        at(0) == 0x89 && ascii(1, "PNG") -> ImageType.PNG
        ascii(0, "RIFF") && ascii(8, "WEBP") -> ImageType.WEBP
        else -> null
    }
}

/** Why [bytes] can't be uploaded, or null if it can. */
fun imageUploadProblem(bytes: ByteArray): String? = when {
    bytes.isEmpty() -> "The file is empty"
    detectImageType(bytes) == null -> "Only JPEG, PNG or WebP images can be used"
    bytes.size > MAX_IMAGE_BYTES ->
        "The image is ${formatMegabytes(bytes.size)}; the limit is ${formatMegabytes(MAX_IMAGE_BYTES)}. " +
            "Compress or resize it first, since every user downloads it"
    else -> null
}

private fun formatMegabytes(bytes: Int): String {
    val tenths = (bytes + 50_000) / 100_000
    return "${tenths / 10}.${tenths % 10} MB"
}

/** `islamic_pictures/abc.jpg` from a Storage download URL, to match a stored URL to its picker entry. */
fun storagePathOf(url: String): String? =
    url.substringAfter("/o/", "").substringBefore('?').takeIf { it.isNotEmpty() }?.replace("%2F", "/")
