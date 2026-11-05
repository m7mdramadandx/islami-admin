package com.islami.stories

import com.islami.domain.entities.StoryBlock
import com.islami.domain.entities.StoryBlockType.HEADING
import com.islami.domain.entities.StoryBlockType.PARAGRAPH
import com.islami.domain.entities.StoryBlockType.QUOTE
import com.islami.domain.stories.analyzeImportedHtml
import com.islami.domain.stories.htmlToStoryBlocks
import com.islami.domain.stories.isEditorFormat
import com.islami.domain.stories.storyBlocksToHtml
import com.islami.domain.stories.validateStory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryHtmlTest {

    private val imported = """
        <!DOCTYPE html> <html lang="ar" dir="rtl"> <head> <meta charset="UTF-8"/> <title>Stories</title></head>
        <body><div class="ArticleDetails details">
        <p>كتب- إيهاب زكريا:</p>
        <p><strong>ثروة أيوب عليه السلام</strong></p>
        <p>كان أيوب <span style="color:#ff0000">رجلاً</span> كثير المال<sup><a href="#1">[1]</a></sup>، يمتلك <a href="https://example.com">الأنعام</a>&nbsp;والعبيد.<br>وعاش على هذه النعم.</p>
        <script type="text/javascript" id="art_mpu_mobile"> if (checkScenario("MPU") == "mobile") { x() } </script>
        <h3>بلاء <b>أيوب</b></h3>
        <blockquote>قال تعالى: (وَوَهَبْنَا لَهُ إِسْحَاقَ)</blockquote>
        <iframe src="https://www.youtube.com/embed/x"></iframe>
        <p>هو &quot;نبي&quot; &#1589;&#x628;ر <strong>عظيم</strong> <strong>جداً</strong></p>
        </div></body></html>
    """.trimIndent()

    @Test
    fun importedHtmlKeepsTextAndStructureOnly() {
        assertEquals(
            listOf(
                StoryBlock(HEADING, "ثروة أيوب عليه السلام"),
                StoryBlock(PARAGRAPH, "كان أيوب رجلاً كثير المال، يمتلك الأنعام والعبيد."),
                StoryBlock(PARAGRAPH, "وعاش على هذه النعم."),
                StoryBlock(HEADING, "بلاء أيوب"),
                StoryBlock(QUOTE, "قال تعالى: (وَوَهَبْنَا لَهُ إِسْحَاقَ)"),
                StoryBlock(PARAGRAPH, "هو \"نبي\" صبر **عظيم جداً**")
            ),
            htmlToStoryBlocks(imported)
        )
    }

    @Test
    fun reportsWhatConversionRemoves() {
        val report = analyzeImportedHtml(imported)
        assertEquals(1, report.scripts)
        assertEquals(2, report.links)
        assertEquals(1, report.coloredStyles)
        assertEquals(1, report.embeds)
    }

    @Test
    fun editorBodiesRoundTripExactly() {
        val blocks = listOf(
            StoryBlock(HEADING, "العنوان"),
            StoryBlock(PARAGRAPH, "سطر أول **مهم**\nسطر ثانٍ"),
            StoryBlock(QUOTE, "آية <كريمة> & شرح"),
            // A fully bold short paragraph stays a paragraph: heading guesses are for imported HTML only.
            StoryBlock(PARAGRAPH, "**كله عريض**")
        )
        val html = storyBlocksToHtml(blocks)
        assertTrue(isEditorFormat(html))
        assertFalse(isEditorFormat(imported))
        assertTrue("&lt;كريمة&gt; &amp;" in html)
        assertTrue("<strong>مهم</strong><br>سطر" in html)
        // Without it the app's wide-viewport WebView zooms the page out and the text is tiny.
        assertTrue("<meta name=\"viewport\" content=\"width=device-width" in html)
        assertEquals(blocks, htmlToStoryBlocks(html))
    }

    @Test
    fun blankBlocksAreDroppedOnSave() {
        val html = storyBlocksToHtml(listOf(StoryBlock(PARAGRAPH, "نص"), StoryBlock(HEADING, "  ")))
        assertEquals(listOf(StoryBlock(PARAGRAPH, "نص")), htmlToStoryBlocks(html))
    }

    @Test
    fun newStoriesNeedAUniqueSafeTitle() {
        val ids = listOf(" إدريس", "آدم")
        fun errors(title: String) = validateStory(title, "brief", "img", listOf(StoryBlock(PARAGRAPH, "x")), ids, isNew = true)
            .filter { it.isError }.map { it.message }
        assertTrue(errors("نوح").isEmpty())
        assertTrue(errors("إدريس").single().contains("already exists"))
        assertTrue(errors("a/b").single().contains("/"))
        assertTrue(errors("  ").single().contains("title is empty"))
    }
}
