package com.islami.domain.ramadan

/** Image formats the app's image loader and every admin platform can decode. */
enum class BackgroundImageType(val extension: String, val contentType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp")
}

/** Users download each background on their phones; the existing ones are 17-390 KB. */
const val MAX_BACKGROUND_BYTES = 1_000_000

/** Detects the format from the file's first bytes rather than trusting its name. */
fun detectBackgroundType(bytes: ByteArray): BackgroundImageType? {
    fun at(index: Int) = bytes.getOrNull(index)?.toInt()?.and(0xFF)
    fun ascii(from: Int, text: String) = text.indices.all { at(from + it) == text[it].code }
    return when {
        at(0) == 0xFF && at(1) == 0xD8 && at(2) == 0xFF -> BackgroundImageType.JPEG
        at(0) == 0x89 && ascii(1, "PNG") -> BackgroundImageType.PNG
        ascii(0, "RIFF") && ascii(8, "WEBP") -> BackgroundImageType.WEBP
        else -> null
    }
}

/** Why [bytes] can't be uploaded, or null if it can. */
fun backgroundUploadProblem(bytes: ByteArray): String? = when {
    bytes.isEmpty() -> "The file is empty"
    detectBackgroundType(bytes) == null -> "Only JPEG, PNG or WebP images can be used"
    bytes.size > MAX_BACKGROUND_BYTES ->
        "The image is ${formatMegabytes(bytes.size)}; the limit is ${formatMegabytes(MAX_BACKGROUND_BYTES)}. " +
            "Compress or resize it first, since every user downloads it"
    else -> null
}

private fun formatMegabytes(bytes: Int): String {
    val tenths = (bytes + 50_000) / 100_000
    return "${tenths / 10}.${tenths % 10} MB"
}
