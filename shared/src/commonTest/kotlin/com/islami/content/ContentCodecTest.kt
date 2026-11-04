package com.islami.content

import com.islami.data.content.ContentCodec
import com.islami.domain.entities.ContentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContentCodecTest {

    private val azkar = """
        {
          "data": [
            {
              "id": 1,
              "Category": "أذكار الصباح",
              "AzkarList": [
                {
                  "description": "من قالها حين يصبح",
                  "zekr": "أَعُوذُ بِاللهِ\nمِنْ الشَّيْطَانِ",
                  "category": ""
                },
                {
                  "zekr": "سبحان الله",
                  "category": null,
                  "count": 3
                }
              ]
            },
            {
              "id": 24,
              "Category": "ادعية الدراسة",
              "AzkarList": []
            }
          ]
        }
    """.trimIndent()

    @Test
    fun unchangedFileReEncodesIdentically() {
        assertEquals(azkar, ContentCodec.encode(ContentKind.AZKAR, ContentCodec.decode(ContentKind.AZKAR, azkar)))
    }

    @Test
    fun keepsAbsentNullAndEmptyFieldsDistinct() {
        val items = ContentCodec.decode(ContentKind.AZKAR, azkar).categories[0].items
        assertEquals("", items[0].fields["category"])
        assertTrue("category" in items[1].fields)
        assertNull(items[1].fields["category"])
        assertFalse("description" in items[1].fields)
    }

    @Test
    fun passesThroughValuesTheEditorDoesNotEdit() {
        val item = ContentCodec.decode(ContentKind.AZKAR, azkar).categories[0].items[1]
        assertEquals("3", item.extras["count"].toString())
        assertFalse("count" in item.fields)
    }

    @Test
    fun editorKeysAreUniqueAcrossCategoriesAndItems() {
        val doc = ContentCodec.decode(ContentKind.AZKAR, azkar)
        val keys = doc.categories.map { it.key } + doc.categories.flatMap { c -> c.items.map { it.key } }
        assertEquals(keys.size, keys.toSet().size)
        assertEquals(25, doc.nextCategoryId())
    }

    @Test
    fun duasRoundTrip() {
        val duas = """
            {
              "data": [
                {
                  "id": 1,
                  "categoryAr": "غضب",
                  "categoryEn": "Angry",
                  "color": "",
                  "duas": [
                    {
                      "duaAr": "أَعُوْذُ بِاللهِ",
                      "duaEn": " I seek protection in Allah"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
        assertEquals(duas, ContentCodec.encode(ContentKind.DUAS, ContentCodec.decode(ContentKind.DUAS, duas)))
    }
}
