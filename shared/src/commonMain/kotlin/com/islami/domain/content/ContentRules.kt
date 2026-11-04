package com.islami.domain.content

import com.islami.domain.entities.ContentCategory
import com.islami.domain.entities.ContentDocument
import com.islami.domain.entities.ContentItem
import com.islami.domain.entities.ContentKind
import com.islami.domain.entities.text
import com.islami.domain.entities.titleOf

data class ContentIssue(
    /** Errors block publishing; warnings are shown but allowed. */
    val isError: Boolean,
    val categoryKey: Long,
    val itemKey: Long? = null,
    val message: String
)

/**
 * What the mobile app needs to render a file without crashing or showing blanks: it reads the
 * azkar category name and zekr as non-null, and shows duas in the reader's language only.
 */
fun ContentDocument.validate(kind: ContentKind): List<ContentIssue> = buildList {
    val idCounts = categories.groupingBy { it.id }.eachCount()

    categories.forEachIndexed { index, category ->
        val name = kind.titleOf(category).ifEmpty { "Category ${index + 1}" }

        kind.categoryFields.filter { it.required && category.text(it.key).isBlank() }.forEach {
            add(ContentIssue(true, category.key, message = "$name: ${it.label.lowercase()} is empty"))
        }
        if (category.items.isEmpty()) {
            add(ContentIssue(false, category.key, message = "$name has no ${kind.itemNoun}s and will show up empty"))
        }
        if ((idCounts[category.id] ?: 0) > 1) {
            add(
                ContentIssue(
                    false, category.key,
                    message = "$name shares id ${category.id} with another category; notifications to that id open the first one"
                )
            )
        }

        category.items.forEachIndexed { itemIndex, item ->
            kind.itemFields.filter { it.required && item.text(it.key).isBlank() }.forEach {
                add(
                    ContentIssue(
                        true, category.key, item.key,
                        "$name, ${kind.itemNoun} ${itemIndex + 1}: ${it.label.lowercase()} is empty"
                    )
                )
            }
        }
    }
}

data class ContentChanges(
    val categoriesAdded: Int = 0,
    val categoriesRemoved: Int = 0,
    val categoriesRenamed: Int = 0,
    val itemsAdded: Int = 0,
    val itemsRemoved: Int = 0,
    val itemsEdited: Int = 0,
    val reordered: Boolean = false,
    /** Categories whose content differs from the published file, for highlighting in the list. */
    val changedCategoryKeys: Set<Long> = emptySet()
) {
    val hasChanges: Boolean
        get() = categoriesAdded + categoriesRemoved + categoriesRenamed + itemsAdded + itemsRemoved + itemsEdited > 0 ||
            reordered

    fun summary(kind: ContentKind): List<String> = buildList {
        fun count(n: Int, noun: String, verb: String) {
            if (n > 0) add("$n $noun${if (n == 1) "" else "s"} $verb")
        }
        count(categoriesAdded, "category", "added")
        count(categoriesRemoved, "category", "deleted")
        count(categoriesRenamed, "category", "renamed")
        count(itemsAdded, kind.itemNoun, "added")
        count(itemsEdited, kind.itemNoun, "edited")
        count(itemsRemoved, kind.itemNoun, "deleted")
        if (reordered) add("Order changed")
    }.map { it.replaceFirstChar(Char::uppercase) }
}

/** Compares by editor key, so a renamed category or edited zekr counts as an edit, not a swap. */
fun ContentDocument.changesFrom(original: ContentDocument): ContentChanges {
    val before = original.categories.associateBy { it.key }
    val after = categories.associateBy { it.key }
    val itemsBefore = original.categories.flatMap { it.items }.associateBy { it.key }
    val itemsAfter = categories.flatMap { it.items }.associateBy { it.key }

    val keptCategories = categories.map { it.key }.filter { it in before }
    val keptItems = categories.flatMap { c -> c.items.map { it.key to c.key } }.filter { it.first in itemsBefore }
    val itemsMoved = keptItems != original.categories
        .flatMap { c -> c.items.map { it.key to c.key } }
        .filter { it.first in itemsAfter }

    return ContentChanges(
        categoriesAdded = after.keys.count { it !in before },
        categoriesRemoved = before.keys.count { it !in after },
        categoriesRenamed = keptCategories.count { k ->
            val old = before.getValue(k)
            val new = after.getValue(k)
            old.fields != new.fields || old.id != new.id
        },
        itemsAdded = itemsAfter.keys.count { it !in itemsBefore },
        itemsRemoved = itemsBefore.keys.count { it !in itemsAfter },
        itemsEdited = itemsAfter.count { (k, item) -> itemsBefore[k]?.let { it.fields != item.fields } == true },
        reordered = keptCategories != original.categories.map { it.key }.filter { it in after } || itemsMoved,
        changedCategoryKeys = categories.filter { it != before[it.key] }.map { it.key }.toSet()
    )
}

/**
 * Gives a freshly decoded document (a backup, or a draft based on an older file) the editor keys of
 * [reference] wherever the content matches, so the change summary shows real edits rather than
 * "everything deleted, everything added".
 */
fun ContentDocument.rekeyAgainst(reference: ContentDocument): ContentDocument {
    val unclaimed = reference.categories.toMutableList()
    var next = maxOf(nextKey(), reference.nextKey())
    val usedItemKeys = mutableSetOf<Long>()

    fun matchItems(items: List<ContentItem>, candidates: List<ContentItem>): List<ContentItem> {
        val pool = candidates.toMutableList()
        return items.map { item ->
            val match = pool.firstOrNull { it.fields == item.fields && it.key !in usedItemKeys }
            if (match != null) {
                pool.remove(match)
                usedItemKeys += match.key
                item.copy(key = match.key)
            } else item.copy(key = next++)
        }
    }

    return copy(categories = categories.map { category ->
        val match = unclaimed.firstOrNull { it.id == category.id }
            ?: unclaimed.firstOrNull { it.fields == category.fields }
        if (match != null) unclaimed.remove(match)
        val key = match?.key ?: next++
        category.copy(key = key, items = matchItems(category.items, match?.items.orEmpty()))
    })
}

/**
 * Edits a field. Clearing it restores the published form ([published] absent, null or ""), so
 * typing and erasing leaves the file byte-identical instead of adding `"key": ""`.
 */
fun Map<String, String?>.withField(key: String, value: String, published: Map<String, String?>?): Map<String, String?> {
    if (value.isNotEmpty() || published?.get(key)?.isNotEmpty() == true) return this + (key to value)
    return if (published != null && key in published) this + (key to published[key]) else this - key
}

fun ContentCategory.matches(kind: ContentKind, query: String): Boolean =
    query.isBlank() || kind.titleOf(this).contains(query, ignoreCase = true) ||
        items.any { it.matches(query) }

fun ContentItem.matches(query: String): Boolean =
    query.isBlank() || fields.values.any { it?.contains(query, ignoreCase = true) == true }
