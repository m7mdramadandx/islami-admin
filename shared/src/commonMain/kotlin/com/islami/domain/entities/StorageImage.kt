package com.islami.domain.entities

/** An image in Firebase Storage that the mobile app shows by its download URL. */
data class StorageImage(
    /** Storage path, e.g. `islamic_pictures/0c73e1c5....jpg`. */
    val path: String,
    val url: String
)

/** Storage folders the dashboard picks images from and uploads to. */
enum class ImageFolder(val path: String) {
    /** Backgrounds behind each Ramadan dua. */
    RAMADAN_BACKGROUNDS("islamic_pictures"),

    /** Cover images on the stories list. */
    STORY_IMAGES("stories")
}
