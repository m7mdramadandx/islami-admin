package com.islami.domain.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A JSON file in Firebase Storage that the mobile app downloads (see `AzkarRemoteDataSourceImpl`
 * and `DuasRemoteDataSourceImpl` in the islami app). The app caches the file and fetches it again
 * only when [versionField] in `root/version` grows, so every publish must bump that number.
 */
enum class ContentKind(
    val title: String,
    val itemNoun: String,
    val storagePath: String,
    val versionField: String,
    /** Key of the item array inside each category object. */
    val itemsKey: String,
    val categoryFields: List<ContentField>,
    val itemFields: List<ContentField>
) {
    AZKAR(
        title = "Azkar",
        itemNoun = "zekr",
        storagePath = "json/azkar.json",
        versionField = "azkarVersion",
        itemsKey = "AzkarList",
        categoryFields = listOf(
            ContentField(
                key = "Category",
                label = "Category name",
                hint = "The app picks the icon from words in the name, e.g. الصباح, النوم, السفر"
            )
        ),
        itemFields = listOf(
            ContentField(key = "zekr", label = "Zekr", multiline = true),
            ContentField(
                key = "category",
                label = "Section heading",
                required = false,
                hint = "Optional sub-heading, e.g. دعاء السجود"
            ),
            ContentField(
                key = "description",
                label = "Virtue or note",
                required = false,
                multiline = true
            )
        )
    ),
    DUAS(
        title = "Duas",
        itemNoun = "dua",
        storagePath = "json/duas.json",
        versionField = "duasVersion",
        itemsKey = "duas",
        categoryFields = listOf(
            ContentField(key = "categoryAr", label = "Arabic name"),
            ContentField(key = "categoryEn", label = "English name")
        ),
        itemFields = listOf(
            // The app shows only the reader's language, with no fallback, so both are required.
            ContentField(key = "duaAr", label = "Arabic", multiline = true),
            ContentField(key = "duaEn", label = "English", multiline = true)
        )
    );

    val backupFolder: String get() = "json/backups/${name.lowercase()}"
}

data class ContentField(
    /** The JSON key, exactly as the mobile app's Gson models name it. */
    val key: String,
    val label: String,
    val required: Boolean = true,
    val multiline: Boolean = false,
    val hint: String? = null
)

/**
 * Editable form of a content file. Text fields keep their JSON keys and anything the editor does
 * not understand rides along in `extras`, so publishing never drops data the app might read.
 */
@Serializable
data class ContentDocument(
    val categories: List<ContentCategory>,
    val extras: Map<String, JsonElement> = emptyMap()
) {
    fun category(key: Long): ContentCategory? = categories.firstOrNull { it.key == key }

    /** Editor keys are unique across categories and items. */
    fun nextKey(): Long =
        (categories.maxOfOrNull { c -> maxOf(c.key, c.items.maxOfOrNull { it.key } ?: 0) } ?: 0) + 1

    /** Category ids are deep-link targets (notification `extraRouteId`), so new ones never reuse an id. */
    fun nextCategoryId(): Int = (categories.maxOfOrNull { it.id } ?: 0) + 1
}

@Serializable
data class ContentCategory(
    /** The app's id: notifications open a category by it, so a published id must not change. */
    val id: Int,
    /** Editor-only identity that survives renames and reordering. Not written to the file. */
    val key: Long,
    /** String or null JSON values; an absent key stays absent in the file. */
    val fields: Map<String, String?> = emptyMap(),
    val items: List<ContentItem> = emptyList(),
    val extras: Map<String, JsonElement> = emptyMap()
)

@Serializable
data class ContentItem(
    val key: Long,
    val fields: Map<String, String?> = emptyMap(),
    val extras: Map<String, JsonElement> = emptyMap()
)

fun ContentCategory.text(field: String): String = fields[field].orEmpty()
fun ContentItem.text(field: String): String = fields[field].orEmpty()

fun ContentKind.titleOf(category: ContentCategory): String =
    categoryFields.map { category.text(it.key).trim() }.filter { it.isNotEmpty() }.joinToString(" · ")

/** The published file as last read from Storage. */
data class ContentSnapshot(
    val document: ContentDocument,
    /** Exact bytes as text, kept so a backup is the file itself rather than a re-encoding. */
    val rawJson: String,
    /** Storage md5 at load time, used to detect someone else publishing in the meantime. */
    val md5Hash: String?,
    /** `root/version` value the app compares against its cached copy. */
    val version: Int
)

data class ContentBackup(
    val path: String,
    /** Display label derived from the file name, e.g. "2026-11-04 09:30:12 UTC". */
    val label: String
)

/** The live file changed after it was loaded; publishing now would overwrite those changes. */
class ContentConflictException : Exception("The live file was changed by someone else since you opened it")

/**
 * The new file is live but `root/version` could not be bumped, so apps keep their cached copy
 * until the bump is retried.
 */
class ContentVersionBumpException(
    val uploaded: ContentSnapshot,
    cause: Throwable
) : Exception("The file was uploaded but the app version could not be bumped: ${cause.message}", cause)
