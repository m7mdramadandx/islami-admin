package com.islami.data.content

import com.islami.domain.entities.ContentCategory
import com.islami.domain.entities.ContentDocument
import com.islami.domain.entities.ContentItem
import com.islami.domain.entities.ContentKind
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Reads and writes the `{"data": [category...]}` files the mobile app parses with Gson.
 *
 * Works on raw JSON rather than typed models so that key order, absent-vs-null fields and keys the
 * editor doesn't know about all survive a round trip: an unedited file re-encodes identically.
 */
object ContentCodec {
    private const val DATA_KEY = "data"
    private const val ID_KEY = "id"

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        // The published files use two-space indentation; matching it keeps diffs readable.
        prettyPrintIndent = "  "
    }

    fun decode(kind: ContentKind, text: String): ContentDocument {
        val root = Json.parseToJsonElement(text).jsonObject
        var nextKey = 1L

        val categories = root[DATA_KEY]?.jsonArray.orEmpty().map { element ->
            val obj = element.jsonObject
            val (fields, extras) = obj.split(exclude = setOf(ID_KEY, kind.itemsKey))
            ContentCategory(
                id = obj[ID_KEY]?.jsonPrimitive?.intOrNull ?: 0,
                key = nextKey++,
                fields = fields,
                extras = extras,
                items = obj[kind.itemsKey]?.jsonArray.orEmpty().map { item ->
                    val (itemFields, itemExtras) = item.jsonObject.split(exclude = emptySet())
                    ContentItem(key = nextKey++, fields = itemFields, extras = itemExtras)
                }
            )
        }
        return ContentDocument(categories = categories, extras = root - DATA_KEY)
    }

    fun encode(kind: ContentKind, document: ContentDocument): String {
        val root = buildJsonObject {
            put(DATA_KEY, JsonArray(document.categories.map { it.toJson(kind) }))
            document.extras.forEach { (k, v) -> put(k, v) }
        }
        return json.encodeToString(JsonElement.serializer(), root)
    }

    private fun ContentCategory.toJson(kind: ContentKind) = buildJsonObject {
        put(ID_KEY, JsonPrimitive(id))
        fields.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
        extras.forEach { (k, v) -> put(k, v) }
        put(kind.itemsKey, JsonArray(items.map { it.toJson() }))
    }

    private fun ContentItem.toJson() = buildJsonObject {
        fields.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
        extras.forEach { (k, v) -> put(k, v) }
    }

    /** Strings and nulls become editable fields; numbers, booleans and nested values pass through. */
    private fun JsonObject.split(exclude: Set<String>): Pair<Map<String, String?>, Map<String, JsonElement>> {
        val fields = linkedMapOf<String, String?>()
        val extras = linkedMapOf<String, JsonElement>()
        for ((k, v) in this) {
            if (k in exclude) continue
            when {
                v is JsonNull -> fields[k] = null
                v is JsonPrimitive && v.isString -> fields[k] = v.content
                else -> extras[k] = v
            }
        }
        return fields to extras
    }
}
