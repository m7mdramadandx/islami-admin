package com.islami.domain.stories

import com.islami.domain.entities.StoryBlock
import com.islami.domain.entities.StoryBlockType

/** Starts every body the dashboard writes; anything else is imported website HTML. */
const val STORY_FORMAT_MARKER = "<!-- islami-admin:story-v1 -->"

fun isEditorFormat(body: String): Boolean = body.trimStart().startsWith(STORY_FORMAT_MARKER)

/** Clean HTML for the app's WebView, which supplies the font and theme colors. */
fun storyBlocksToHtml(blocks: List<StoryBlock>): String = buildString {
    append(STORY_FORMAT_MARKER).append('\n')
    // Without it the app's wide-viewport WebView zooms out and the text is tiny.
    append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
    append("<div dir=\"rtl\" style=\"line-height:1.8\">\n")
    blocks.filter { it.text.isNotBlank() }.forEach { block ->
        when (block.type) {
            StoryBlockType.HEADING -> append("<h2>").append(escape(block.text.trim().replace("**", ""))).append("</h2>")
            StoryBlockType.PARAGRAPH -> append("<p>").append(inline(block.text.trim())).append("</p>")
            StoryBlockType.QUOTE -> append("<blockquote>").append(inline(block.text.trim())).append("</blockquote>")
        }
        append('\n')
    }
    append("</div>")
}

/** What converting an imported body removes, so the admin knows before replacing it. */
data class ImportReport(val scripts: Int, val links: Int, val coloredStyles: Int, val embeds: Int)

fun analyzeImportedHtml(html: String): ImportReport = ImportReport(
    scripts = Regex("<script\\b", RegexOption.IGNORE_CASE).findAll(html).count(),
    links = Regex("<a\\s[^>]*href", RegexOption.IGNORE_CASE).findAll(html).count(),
    coloredStyles = Regex("color\\s*[:=]", RegexOption.IGNORE_CASE).findAll(html).count(),
    embeds = Regex("<(iframe|img|video)\\b", RegexOption.IGNORE_CASE).findAll(html).count()
)

/** Reverses [storyBlocksToHtml]; for imported HTML keeps text and structure, drops the rest. */
fun htmlToStoryBlocks(html: String): List<StoryBlock> {
    val imported = !isEditorFormat(html)
    val cleaned = html
        .replace(Regex("<!--[\\s\\S]*?-->"), "")
        .replace(Regex("<![^>]*>"), "") // <!DOCTYPE ...> and similar declarations
        .replace(DROPPED_ELEMENTS, "")

    val blocks = mutableListOf<StoryBlock>()
    val text = StringBuilder()
    var headingDepth = 0
    var quoteDepth = 0

    fun flush() {
        val raw = decodeEntities(text.toString())
        text.clear()
        val type = when {
            headingDepth > 0 -> StoryBlockType.HEADING
            quoteDepth > 0 -> StoryBlockType.QUOTE
            else -> StoryBlockType.PARAGRAPH
        }
        cleanBlock(type, raw, imported)?.let(blocks::add)
    }

    var last = 0
    for (tag in TAG.findAll(cleaned)) {
        text.append(cleaned, last, tag.range.first)
        last = tag.range.last + 1
        val closing = tag.groupValues[1] == "/"
        when (val name = tag.groupValues[2].lowercase()) {
            in HEADINGS -> { flush(); headingDepth = (headingDepth + if (closing) -1 else 1).coerceAtLeast(0) }
            "blockquote" -> { flush(); quoteDepth = (quoteDepth + if (closing) -1 else 1).coerceAtLeast(0) }
            // In dashboard bodies a <br> is a line break the admin typed; on websites it separates lines.
            "br" -> if (imported) flush() else text.append('\n')
            "strong", "b" -> if (headingDepth == 0) text.append("**")
            in BLOCK_TAGS -> if (name != "div" || imported) flush()
            else -> Unit // inline tags (a, span, font, em, ...) keep their text only
        }
    }
    text.append(cleaned, last, cleaned.length)
    flush()
    return blocks
}

/** Plain text of a body, e.g. to count words. */
fun storyPlainText(blocks: List<StoryBlock>): String = blocks.joinToString("\n") { it.text.replace("**", "") }

private fun cleanBlock(type: StoryBlockType, raw: String, imported: Boolean): StoryBlock? {
    var text = raw
        .replace(' ', ' ')
        .lines().joinToString("\n") { it.replace(Regex("[ \\t\\r]+"), " ").trim() }
        .replace(Regex("\n{2,}"), "\n")
        .trim()
    // Merge touching bold runs ("**a** **b**" -> "**a b**") and drop unmatched markers.
    text = text.replace(Regex("\\*\\*(\\s*)\\*\\*"), "$1").trim()
    if (Regex("\\*\\*").findAll(text).count() % 2 != 0) text = text.replace("**", "")
    if (!text.any { it.isLetterOrDigit() }) return null

    if (!imported) return StoryBlock(type, if (type == StoryBlockType.HEADING) text.replace("**", "") else text)

    // A news-site byline such as "كتب- إيهاب زكريا:".
    if (type == StoryBlockType.PARAGRAPH && BYLINE.matches(text)) return null
    // Websites often fake headings with a short, fully bold paragraph.
    val wholeBold = text.startsWith("**") && text.endsWith("**") && text.length > 4 &&
        text.substring(2, text.length - 2).let { !it.contains("**") }
    if (type == StoryBlockType.PARAGRAPH && wholeBold && text.split(' ').size <= 12) {
        return StoryBlock(StoryBlockType.HEADING, text.removeSurrounding("**").trim())
    }
    return StoryBlock(type, if (type == StoryBlockType.HEADING) text.replace("**", "") else text)
}

private fun inline(text: String): String {
    val escaped = escape(text)
    var open = false
    return buildString {
        escaped.split("**").forEachIndexed { index, part ->
            if (index > 0) { append(if (open) "</strong>" else "<strong>"); open = !open }
            append(part)
        }
        if (open) append("</strong>")
    }.replace("\n", "<br>")
}

private fun escape(text: String) = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

private fun decodeEntities(text: String): String = ENTITY.replace(text) { match ->
    val name = match.groupValues[1]
    when {
        name.startsWith("#x") || name.startsWith("#X") -> name.drop(2).toIntOrNull(16)?.let(::codePointToString)
        name.startsWith("#") -> name.drop(1).toIntOrNull()?.let(::codePointToString)
        else -> NAMED_ENTITIES[name]
    } ?: match.value
}

private fun codePointToString(code: Int): String? = when {
    code in 0..0xFFFF -> code.toChar().toString()
    code <= 0x10FFFF -> {
        val v = code - 0x10000
        charArrayOf(((v shr 10) + 0xD800).toChar(), ((v and 0x3FF) + 0xDC00).toChar()).concatToString()
    }
    else -> null
}

private val TAG = Regex("<(/?)([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>")
private val ENTITY = Regex("&(#[xX][0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);")
private val DROPPED_ELEMENTS = Regex(
    // [\s\S] rather than DOT_MATCHES_ALL, which Kotlin/JS doesn't support.
    "<(script|style|head|title|iframe|noscript|svg|sup|video|object)\\b[\\s\\S]*?</\\1\\s*>",
    RegexOption.IGNORE_CASE
)
private val HEADINGS = setOf("h1", "h2", "h3", "h4", "h5", "h6")
private val BLOCK_TAGS = setOf(
    "p", "div", "li", "ul", "ol", "tr", "table", "section", "article", "header", "footer",
    "figure", "figcaption", "hr", "html", "body", "main", "aside", "nav", "dd", "dt"
)
private val BYLINE = Regex("^كتب\\s*[-–ـ:]+\\s*\\S.{0,40}$")
private val NAMED_ENTITIES = mapOf(
    "nbsp" to " ", "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'",
    "laquo" to "«", "raquo" to "»", "hellip" to "…", "mdash" to "—", "ndash" to "–",
    "rlm" to "‏", "lrm" to "‎", "zwnj" to "‌", "zwj" to "‍"
)
