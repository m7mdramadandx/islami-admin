package com.islami.content

import com.islami.domain.content.changesFrom
import com.islami.domain.content.rekeyAgainst
import com.islami.domain.content.validate
import com.islami.domain.content.withField
import com.islami.domain.entities.ContentCategory
import com.islami.domain.entities.ContentDocument
import com.islami.domain.entities.ContentItem
import com.islami.domain.entities.ContentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentRulesTest {

    private fun zekr(key: Long, text: String) = ContentItem(key, mapOf("zekr" to text))

    private val published = ContentDocument(
        listOf(
            ContentCategory(1, key = 1, fields = mapOf("Category" to "الصباح"), items = listOf(zekr(2, "a"), zekr(3, "b"))),
            ContentCategory(2, key = 4, fields = mapOf("Category" to "المساء"), items = listOf(zekr(5, "c")))
        )
    )

    @Test
    fun publishedDocumentHasNoChanges() {
        assertFalse(published.changesFrom(published).hasChanges)
    }

    @Test
    fun countsEditsByKeyNotPosition() {
        val edited = published.copy(
            categories = published.categories.map {
                if (it.key == 1L) it.copy(items = listOf(zekr(2, "a!"), zekr(3, "b"), zekr(9, "new"))) else it
            }
        )
        val changes = edited.changesFrom(published)
        assertEquals(1, changes.itemsEdited)
        assertEquals(1, changes.itemsAdded)
        assertFalse(changes.reordered)
        assertEquals(setOf(1L), changes.changedCategoryKeys)
    }

    @Test
    fun detectsReorderAndMoveBetweenCategories() {
        val reordered = published.copy(categories = published.categories.reversed())
        assertTrue(reordered.changesFrom(published).reordered)

        val moved = published.copy(
            categories = listOf(
                published.categories[0].copy(items = listOf(zekr(2, "a"))),
                published.categories[1].copy(items = listOf(zekr(5, "c"), zekr(3, "b")))
            )
        )
        val changes = moved.changesFrom(published)
        assertTrue(changes.reordered)
        assertEquals(0, changes.itemsEdited + changes.itemsAdded + changes.itemsRemoved)
    }

    @Test
    fun blankRequiredTextBlocksPublishingButDuplicateIdOnlyWarns() {
        val doc = ContentDocument(
            listOf(
                ContentCategory(24, key = 1, fields = mapOf("Category" to " "), items = listOf(zekr(2, ""))),
                ContentCategory(24, key = 3, fields = mapOf("Category" to "x"), items = listOf(zekr(4, "y")))
            )
        )
        val issues = doc.validate(ContentKind.AZKAR)
        assertEquals(2, issues.count { it.isError })
        assertEquals(2, issues.count { !it.isError && "id 24" in it.message })
    }

    @Test
    fun duasNeedBothLanguages() {
        val doc = ContentDocument(
            listOf(
                ContentCategory(
                    1, key = 1,
                    fields = mapOf("categoryAr" to "غضب", "categoryEn" to "Angry"),
                    items = listOf(ContentItem(2, mapOf("duaAr" to "دعاء")))
                )
            )
        )
        assertEquals(1, doc.validate(ContentKind.DUAS).count { it.isError })
    }

    @Test
    fun clearingAFieldRestoresItsPublishedForm() {
        val publishedFields = mapOf<String, String?>("zekr" to "a", "category" to "")
        val typed = publishedFields.withField("description", "x", publishedFields)
        assertEquals(publishedFields, typed.withField("description", "", publishedFields))
        assertEquals("", publishedFields.withField("category", "", publishedFields)["category"])
        assertEquals(mapOf<String, String?>(), mapOf<String, String?>().withField("zekr", "", null))
    }

    @Test
    fun rekeyMatchesBackupContentToCurrentKeys() {
        // A backup decodes with fresh keys; matching gives back the published ones where content agrees.
        val backup = ContentDocument(
            listOf(
                ContentCategory(1, key = 10, fields = mapOf("Category" to "الصباح"), items = listOf(zekr(11, "a"), zekr(12, "old"))),
                ContentCategory(2, key = 13, fields = mapOf("Category" to "المساء"), items = listOf(zekr(14, "c")))
            )
        )
        val changes = backup.rekeyAgainst(published).changesFrom(published)
        assertEquals(0, changes.categoriesAdded + changes.categoriesRemoved)
        assertEquals(1, changes.itemsAdded)
        assertEquals(1, changes.itemsRemoved)
    }
}
