package com.islami.presentation.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.islami.domain.entities.Story
import com.islami.domain.entities.StoryBlock
import com.islami.domain.entities.StoryBlockType
import com.islami.domain.stories.analyzeImportedHtml
import com.islami.domain.stories.htmlToStoryBlocks
import com.islami.domain.stories.isEditorFormat
import com.islami.domain.stories.storyPlainText
import com.islami.presentation.components.ImageLibraryDialog
import com.islami.presentation.components.RemoteImage
import com.islami.presentation.navigation.Navigator
import com.islami.presentation.stories.StoriesState.PendingSwitch
import org.koin.compose.koinInject

/** Above this width the story list sits beside the editor instead of replacing it. */
private val TwoPaneBreakpoint = 840.dp

/** Above this width the preview gets its own column instead of sitting under the editor. */
private val ThreePaneBreakpoint = 1200.dp

private val ContentTextStyle = TextStyle(textDirection = TextDirection.Content)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    navigator: Navigator,
    holder: StoriesStateHolder = koinInject()
) {
    val state by holder.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmLeave by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        val undo = state.undoBlock != null
        val result = snackbarHostState.showSnackbar(
            message,
            actionLabel = if (undo) "Undo" else null,
            withDismissAction = !undo,
            duration = if (undo) SnackbarDuration.Long else SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) holder.undoDeleteBlock()
        holder.onMessageShown()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stories") },
                navigationIcon = {
                    IconButton(onClick = { if (state.hasChanges) confirmLeave = true else navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = holder::newStory, enabled = !state.isSaving) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("New story")
                    }
                    IconButton(onClick = { holder.load() }, enabled = !state.isLoading && !state.hasChanges) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.stories.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.loadError != null && state.stories.isEmpty() -> LoadError(state.loadError.orEmpty()) { holder.load() }
                else -> Body(state, holder)
            }
        }
    }

    Dialogs(state, holder)

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Leave without saving?") },
            text = { Text("Your unsaved changes to this story will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmLeave = false; navigator.pop() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Leave") }
            },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Stay") } }
        )
    }
}

@Composable
private fun LoadError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Text("Couldn't load root/ar/stories", style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
private fun Body(state: StoriesState, holder: StoriesStateHolder) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val draft = state.draft
        when {
            maxWidth >= ThreePaneBreakpoint -> Row(Modifier.fillMaxSize()) {
                StoryList(state, holder, Modifier.width(320.dp).fillMaxHeight())
                VerticalDivider()
                if (draft == null) EmptyEditor(Modifier.weight(1f))
                else {
                    Editor(state, holder, draft, showPreview = false, onBack = null, modifier = Modifier.weight(1f))
                    VerticalDivider()
                    Column(Modifier.width(380.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(20.dp)) {
                        Preview(draft)
                    }
                }
            }
            maxWidth >= TwoPaneBreakpoint -> Row(Modifier.fillMaxSize()) {
                StoryList(state, holder, Modifier.width(300.dp).fillMaxHeight())
                VerticalDivider()
                if (draft == null) EmptyEditor(Modifier.weight(1f))
                else Editor(state, holder, draft, showPreview = true, onBack = null, modifier = Modifier.weight(1f))
            }
            draft != null -> Editor(state, holder, draft, showPreview = true, onBack = holder::close, modifier = Modifier.fillMaxSize())
            else -> StoryList(state, holder, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun EmptyEditor(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select a story to edit, or add a new one", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// region Story list

@Composable
private fun StoryList(state: StoriesState, holder: StoriesStateHolder, modifier: Modifier) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(8.dp)) {
        item {
            Text(
                "${state.stories.size} stories, in the app's order (alphabetical by original title)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
            )
        }
        items(state.stories, key = { it.id }) { story ->
            NavigationDrawerItem(
                selected = story.id == state.selectedId,
                onClick = { holder.open(story.id) },
                icon = {
                    if (story.image.isNotBlank()) {
                        RemoteImage(
                            url = story.image, contentDescription = null, maxWidth = 120,
                            modifier = Modifier.size(44.dp).clip(MaterialTheme.shapes.small)
                        )
                    } else {
                        Box(Modifier.size(44.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceContainerHigh))
                    }
                },
                label = {
                    Column {
                        Text(
                            story.title.trim(),
                            style = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle),
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            story.brief,
                            style = MaterialTheme.typography.bodySmall.merge(ContentTextStyle),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                badge = {
                    if (!isEditorFormat(story.body)) {
                        Text("Imported", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

// endregion

// region Editor

@Composable
private fun Editor(
    state: StoriesState,
    holder: StoriesStateHolder,
    draft: StoryDraft,
    showPreview: Boolean,
    onBack: (() -> Unit)?,
    modifier: Modifier
) {
    Column(modifier) {
        SaveBar(state, holder, draft, onBack)
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "fields") { DetailsFields(state, holder, draft) }
            item(key = "bodyHeader") {
                Text("Story text", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
            }
            val blocks = draft.blocks
            if (blocks == null) {
                item(key = "imported") { ImportedBody(draft.base?.body.orEmpty(), holder) }
            } else {
                blockItems(blocks, holder)
                item(key = "addBlocks") { AddBlockRow(holder) }
            }
            if (showPreview) {
                item(key = "preview") {
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    Preview(draft, Modifier.widthIn(max = 420.dp))
                }
            }
        }
    }
}

@Composable
private fun SaveBar(state: StoriesState, holder: StoriesStateHolder, draft: StoryDraft, onBack: (() -> Unit)?) {
    var confirmDiscard by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "All stories") }
            }
            Column(Modifier.weight(1f).widthIn(min = 180.dp)) {
                Text(
                    if (draft.isNew) "New story" else draft.base?.title?.trim().orEmpty(),
                    style = MaterialTheme.typography.titleSmall.merge(ContentTextStyle),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (state.hasChanges) "Unsaved changes" else "No unsaved changes",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.hasChanges) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.errorCount > 0) {
                AssistChip(
                    onClick = holder::requestSave,
                    label = { Text("${state.errorCount} to fix") },
                    leadingIcon = {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(AssistChipDefaults.IconSize))
                    }
                )
            }
            if (!draft.isNew) {
                IconButton(onClick = holder::requestDelete, enabled = !state.isSaving) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete story", tint = MaterialTheme.colorScheme.error)
                }
            }
            TextButton(onClick = { confirmDiscard = true }, enabled = state.hasChanges && !state.isSaving) { Text("Discard") }
            Button(onClick = holder::requestSave, enabled = state.hasChanges && !state.isSaving) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Saving…")
                } else Text(if (draft.isNew) "Add story" else "Save")
            }
        }
    }
    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard unsaved changes?") },
            text = { Text(if (draft.isNew) "The new story won't be added." else "The story goes back to what's live in the app.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmDiscard = false; if (draft.isNew) holder.confirmDiscardNew() else holder.discardChanges() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Keep editing") } }
        )
    }
}

@Composable
private fun DetailsFields(state: StoriesState, holder: StoriesStateHolder, draft: StoryDraft) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.issues.forEach { issue ->
            Text(
                "• ${issue.message}",
                style = MaterialTheme.typography.bodySmall,
                color = if (issue.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
            )
        }
        OutlinedTextField(
            value = draft.title,
            onValueChange = holder::onTitleChanged,
            label = { Text("Title") },
            supportingText = {
                Text(
                    if (draft.isNew) "Also becomes the story's id, which sets its place in the app's alphabetical list"
                    else "Renaming doesn't move the story: the app orders by the title it was created with"
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = draft.brief,
            onValueChange = holder::onBriefChanged,
            label = { Text("Brief") },
            supportingText = { Text("The line under the title in the stories list") },
            maxLines = 3,
            textStyle = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle),
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (draft.image.isNotBlank()) {
                RemoteImage(
                    url = draft.image, contentDescription = "Cover image", maxWidth = 320,
                    modifier = Modifier.size(width = 120.dp, height = 90.dp).clip(MaterialTheme.shapes.small)
                )
            } else {
                Box(
                    Modifier.size(width = 120.dp, height = 90.dp).clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) { Text("No cover", style = MaterialTheme.typography.labelSmall) }
            }
            Spacer(Modifier.width(16.dp))
            OutlinedButton(onClick = holder::openPicker) { Text("Change cover") }
        }
    }
}

@Composable
private fun ImportedBody(body: String, holder: StoriesStateHolder) {
    val report = remember(body) { analyzeImportedHtml(body) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Copied from a website", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(
                buildList {
                    if (report.scripts > 0) add("${report.scripts} script${if (report.scripts == 1) "" else "s"} the app runs")
                    if (report.links > 0) add("${report.links} link${if (report.links == 1) "" else "s"} to other sites")
                    if (report.coloredStyles > 0) add("${report.coloredStyles} fixed color${if (report.coloredStyles == 1) "" else "s"} that can break dark mode")
                    if (report.embeds > 0) add("${report.embeds} embedded video${if (report.embeds == 1) "" else "s"} or image${if (report.embeds == 1) "" else "s"}")
                }.ifEmpty { listOf("website formatting") }.joinToString(", ", prefix = "Its text still carries ", postfix = "."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                "Converting keeps the words, headings, quotes and bold text and removes everything else, " +
                    "including reporter bylines. The preview shows the result. Nothing changes in the app until you save.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Button(onClick = holder::convertBody) { Text("Convert to editable text") }
        }
    }
}

private fun LazyListScope.blockItems(blocks: List<EditableBlock>, holder: StoriesStateHolder) {
    items(blocks, key = { it.key }) { block ->
        val index = blocks.indexOf(block)
        BlockCard(block, canMoveUp = index > 0, canMoveDown = index < blocks.lastIndex, holder = holder)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockCard(block: EditableBlock, canMoveUp: Boolean, canMoveDown: Boolean, holder: StoriesStateHolder) {
    var menuOpen by remember { mutableStateOf(false) }
    OutlinedCard {
        Column(Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SingleChoiceSegmentedButtonRow(Modifier.weight(1f, fill = false)) {
                    BLOCK_TYPES.forEachIndexed { i, (type, label) ->
                        SegmentedButton(
                            selected = block.type == type,
                            onClick = { holder.onBlockTypeChanged(block.key, type) },
                            shape = SegmentedButtonDefaults.itemShape(i, BLOCK_TYPES.size),
                            icon = {},
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { holder.moveBlock(block.key, -1) }, enabled = canMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = { holder.moveBlock(block.key, 1) }, enabled = canMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        BLOCK_TYPES.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text("Insert $label below") },
                                onClick = { menuOpen = false; holder.addBlock(type, afterKey = block.key) }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuOpen = false; holder.deleteBlock(block.key) }
                        )
                    }
                }
            }
            val heading = block.type == StoryBlockType.HEADING
            OutlinedTextField(
                value = block.text,
                onValueChange = { holder.onBlockTextChanged(block.key, it) },
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp, top = 4.dp),
                textStyle = (if (heading) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge).merge(ContentTextStyle),
                singleLine = heading,
                minLines = if (heading) 1 else 2,
                maxLines = if (heading) 1 else 14,
                placeholder = { Text(if (heading) "Heading" else "Text. Wrap words in **double stars** for bold") }
            )
        }
    }
}

@Composable
private fun AddBlockRow(holder: StoriesStateHolder) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BLOCK_TYPES.forEach { (type, label) ->
            OutlinedButton(onClick = { holder.addBlock(type) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(label)
            }
        }
    }
}

private val BLOCK_TYPES = listOf(
    StoryBlockType.PARAGRAPH to "Paragraph",
    StoryBlockType.HEADING to "Heading",
    StoryBlockType.QUOTE to "Quote"
)

// endregion

// region Preview

/** How the story looks in the app: its row in the list, then its page. */
@Composable
private fun Preview(draft: StoryDraft, modifier: Modifier = Modifier) {
    val base = draft.base
    // An unconverted body previews as it will look after conversion.
    val blocks = draft.blocks?.map(EditableBlock::toBlock)
        ?: remember(base?.body) { htmlToStoryBlocks(base?.body.orEmpty()) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("In the stories list", style = MaterialTheme.typography.labelLarge)
        OutlinedCard {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (draft.image.isNotBlank()) {
                    RemoteImage(draft.image, null, maxWidth = 240, modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.small))
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(draft.title.trim().ifEmpty { "Title" }, style = MaterialTheme.typography.titleMedium.merge(ContentTextStyle))
                    Text(
                        draft.brief.trim().ifEmpty { "Brief" },
                        style = MaterialTheme.typography.bodyMedium.merge(ContentTextStyle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Text(
            if (draft.blocks == null) "Story page, after conversion" else "Story page",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            "${storyPlainText(blocks).split(Regex("\\s+")).count { it.isNotBlank() }} words",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(color = MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.medium, tonalElevation = 1.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Long stories: preview the start, which is enough to judge the look.
                blocks.take(PREVIEW_BLOCKS).forEach { PreviewBlock(it) }
                if (blocks.size > PREVIEW_BLOCKS) {
                    Text("…and ${blocks.size - PREVIEW_BLOCKS} more blocks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PreviewBlock(block: StoryBlock) {
    when (block.type) {
        StoryBlockType.HEADING -> Text(
            block.text, style = MaterialTheme.typography.titleLarge.merge(ContentTextStyle), fontWeight = FontWeight.Bold
        )
        StoryBlockType.PARAGRAPH -> Text(boldMarkup(block.text), style = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle))
        StoryBlockType.QUOTE -> Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline))
            Spacer(Modifier.width(12.dp))
            Text(boldMarkup(block.text), style = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle))
        }
    }
}

/** Renders `**bold**` markup the way the saved HTML will. */
private fun boldMarkup(text: String) = buildAnnotatedString {
    text.split("**").forEachIndexed { index, part ->
        if (index % 2 == 1) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) } else append(part)
    }
}

private const val PREVIEW_BLOCKS = 40

// endregion

// region Dialogs

@Composable
private fun Dialogs(state: StoriesState, holder: StoriesStateHolder) {
    val draft = state.draft

    if (state.pickerOpen) {
        ImageLibraryDialog(
            title = "Choose a cover image",
            hint = "Images in Storage stories/. JPEG, PNG or WebP up to 1 MB.",
            images = state.images,
            selectedUrl = draft?.image.orEmpty(),
            aspectRatio = 1f,
            isUploading = state.isUploading,
            onPick = { holder.chooseImage(it.url) },
            onImagePicked = holder::onImagePicked,
            onDismiss = holder::dismissPicker
        )
    }

    state.pendingSwitch?.let { target ->
        AlertDialog(
            onDismissRequest = holder::cancelSwitch,
            title = { Text("Discard unsaved changes?") },
            text = {
                Text(
                    if (draft?.isNew == true) "The new story hasn't been added yet."
                    else "Your edits to \"${draft?.base?.title?.trim()}\" haven't been saved."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = holder::confirmSwitch,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(if (target == PendingSwitch.Close) "Discard" else "Discard and continue") }
            },
            dismissButton = { TextButton(onClick = holder::cancelSwitch) { Text("Keep editing") } }
        )
    }

    if (state.confirmSave && draft != null) {
        AlertDialog(
            onDismissRequest = holder::dismissDialogs,
            title = { Text(if (draft.isNew) "Add this story?" else "Save this story?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    saveSummary(draft).forEach { Text("• $it") }
                    Text(
                        if (draft.isNew) "It appears in the app right away, in alphabetical position."
                        else "The app shows it right away. The current version is backed up to Storage first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    state.issues.filter { !it.isError }.forEach {
                        Text("• ${it.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            },
            confirmButton = { Button(onClick = { holder.confirmSave() }) { Text(if (draft.isNew) "Add story" else "Save") } },
            dismissButton = { TextButton(onClick = holder::dismissDialogs) { Text("Cancel") } }
        )
    }

    if (state.confirmDelete && draft?.base != null) {
        AlertDialog(
            onDismissRequest = holder::dismissDialogs,
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Delete \"${draft.base.title.trim()}\"?") },
            text = { Text("It disappears from the app right away. A backup is kept in Storage stories/backups/.") },
            confirmButton = {
                TextButton(
                    onClick = holder::confirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = holder::dismissDialogs) { Text("Cancel") } }
        )
    }

    if (state.conflict) {
        AlertDialog(
            onDismissRequest = holder::dismissDialogs,
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Changed since you opened it") },
            text = {
                Text(
                    "Someone else saved or deleted this story after you opened it. Saving now replaces their " +
                        "version. Reload to see it first; this discards your unsaved edits."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { holder.confirmSave(overwrite = true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Save anyway") }
            },
            dismissButton = {
                TextButton(onClick = { holder.dismissDialogs(); holder.reloadDiscarding() }) { Text("Reload") }
            }
        )
    }
}

private fun saveSummary(draft: StoryDraft): List<String> {
    val base = draft.base ?: return listOf("New story: \"${draft.title.trim()}\"")
    val story = draft.toStory()
    return buildList {
        if (story.title != base.title) add("Title")
        if (story.brief != base.brief) add("Brief")
        if (story.image != base.image) add("Cover image")
        if (story.body != base.body) add(if (isEditorFormat(base.body)) "Story text" else "Story text, converted from the website version")
    }
}

// endregion
