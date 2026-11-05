package com.islami.presentation.ramadan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.islami.domain.entities.RamadanDay
import com.islami.presentation.components.RemoteImage
import com.islami.presentation.components.ImageLibraryDialog
import com.islami.presentation.navigation.Navigator
import org.koin.compose.koinInject

/** Above this width the day list sits beside the editor instead of replacing it. */
private val TwoPaneBreakpoint = 840.dp

/** Above this width (of the detail pane) the preview sits beside the fields instead of below. */
private val PreviewBesideBreakpoint = 760.dp

private val ContentTextStyle = TextStyle(textDirection = TextDirection.Content)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RamadanScreen(
    navigator: Navigator,
    holder: RamadanStateHolder = koinInject()
) {
    val state by holder.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmLeave by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it, withDismissAction = true)
            holder.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ramadan duas") },
                navigationIcon = {
                    IconButton(onClick = { if (state.hasChanges) confirmLeave = true else navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { holder.load() }, enabled = !state.isLoading && !state.isSaving && !state.hasChanges) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.draft.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.draft.isEmpty() -> LoadError(state.loadError.orEmpty()) { holder.load() }
                else -> {
                    SaveBar(state, holder)
                    HorizontalDivider()
                    Editor(state, holder, Modifier.weight(1f))
                }
            }
        }
    }

    if (state.pickerOpen) BackgroundPicker(state, holder)
    SaveDialogs(state, holder)

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Leave without saving?") },
            text = { Text("Your unsaved changes to the Ramadan duas will be lost.") },
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
        Text("Couldn't load root/ar/ramadan", style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
private fun SaveBar(state: RamadanState, holder: RamadanStateHolder) {
    var confirmDiscard by remember { mutableStateOf(false) }
    val changed = state.changedIds.size
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).widthIn(min = 200.dp)) {
                Text(
                    if (changed == 0) "No unsaved changes" else "$changed day${if (changed == 1) "" else "s"} changed",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (changed == 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Shown on the app's Ramadan duas screen, one per day. Saved changes appear right away.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.errorCount > 0) {
                AssistChip(
                    onClick = holder::requestSave,
                    label = { Text("${state.errorCount} to fix") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
            }
            TextButton(onClick = { confirmDiscard = true }, enabled = state.hasChanges && !state.isSaving) { Text("Discard") }
            Button(onClick = holder::requestSave, enabled = state.hasChanges && !state.isSaving) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Saving…")
                } else Text("Save")
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard unsaved changes?") },
            text = { Text("The $changed edited day${if (changed == 1) "" else "s"} go back to what's live in the app.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmDiscard = false; holder.discardChanges() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = false }) { Text("Keep editing") } }
        )
    }
}

@Composable
private fun Editor(state: RamadanState, holder: RamadanStateHolder, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= TwoPaneBreakpoint) {
            Row(Modifier.fillMaxSize()) {
                DayList(state, onSelect = holder::select, modifier = Modifier.width(300.dp).fillMaxHeight())
                VerticalDivider()
                DayDetail(state, holder, onBack = null, modifier = Modifier.weight(1f))
            }
        } else {
            var showDetail by remember { mutableStateOf(false) }
            if (showDetail && state.selected != null) {
                DayDetail(state, holder, onBack = { showDetail = false })
            } else {
                DayList(state, onSelect = { holder.select(it); showDetail = true }, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

// region Day list

@Composable
private fun DayList(state: RamadanState, onSelect: (String) -> Unit, modifier: Modifier) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(8.dp)) {
        items(state.draft, key = { it.id }) { day ->
            val hasError = state.issuesFor(day.id).any { it.isError }
            NavigationDrawerItem(
                selected = day.id == state.selectedId,
                onClick = { onSelect(day.id) },
                icon = {
                    if (day.backgroundUrl.isNotBlank()) {
                        RemoteImage(
                            url = day.backgroundUrl,
                            contentDescription = null,
                            maxWidth = 96,
                            modifier = Modifier.size(width = 32.dp, height = 44.dp).clip(MaterialTheme.shapes.extraSmall)
                        )
                    } else {
                        Box(
                            Modifier.size(width = 32.dp, height = 44.dp).clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                    }
                },
                label = {
                    Column {
                        Text(
                            "Day ${day.dayNumber} · ${day.title}",
                            style = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            day.dua.lineSequence().firstOrNull()?.ifBlank { null } ?: "No dua",
                            style = MaterialTheme.typography.bodySmall.merge(ContentTextStyle),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                badge = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (hasError) {
                            Icon(Icons.Default.Warning, contentDescription = "Has problems", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                        if (day.id in state.changedIds) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                        }
                    }
                },
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

// endregion

// region Day detail

@Composable
private fun DayDetail(state: RamadanState, holder: RamadanStateHolder, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    val day = state.selected ?: return
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val besides = maxWidth >= PreviewBesideBreakpoint
        val fields: @Composable (Modifier) -> Unit = { fieldModifier -> DayFields(state, holder, day, onBack, fieldModifier) }
        if (besides) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(24.dp)) {
                    fields(Modifier.fillMaxWidth())
                }
                Column(
                    Modifier.width(340.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(top = 24.dp, end = 24.dp, bottom = 24.dp)
                ) {
                    DayPreview(day)
                }
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                fields(Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                DayPreview(day, Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun DayFields(
    state: RamadanState,
    holder: RamadanStateHolder,
    day: RamadanDay,
    onBack: (() -> Unit)?,
    modifier: Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "All days") }
            }
            Column {
                Text("Day ${day.dayNumber} of Ramadan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    "Title in the app: ${day.title}",
                    style = MaterialTheme.typography.bodyMedium.merge(ContentTextStyle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (day.id in state.changedIds) {
                    Text("Unsaved changes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        val duaMissing = day.dua.isBlank()
        OutlinedTextField(
            value = day.dua,
            onValueChange = holder::onDuaChanged,
            label = { Text("Dua") },
            isError = duaMissing,
            supportingText = { Text(if (duaMissing) "Required: the app would show an empty screen" else "${day.dua.length} characters") },
            minLines = 6,
            maxLines = 16,
            textStyle = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Background", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (day.backgroundUrl.isNotBlank()) {
                RemoteImage(
                    url = day.backgroundUrl,
                    contentDescription = "Current background",
                    maxWidth = 240,
                    modifier = Modifier.size(width = 72.dp, height = 128.dp).clip(MaterialTheme.shapes.small)
                )
            } else {
                Box(
                    Modifier.size(width = 72.dp, height = 128.dp).clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) { Text("None", style = MaterialTheme.typography.labelSmall) }
            }
            Spacer(Modifier.width(16.dp))
            OutlinedButton(onClick = holder::openPicker) { Text("Change image") }
        }

        state.issuesFor(day.id).filter { !it.isError }.forEach {
            Text("• ${it.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

/** Roughly how the day looks in the app: the dua over its background, title on top. */
@Composable
private fun DayPreview(day: RamadanDay, modifier: Modifier = Modifier) {
    Column(modifier = modifier.widthIn(max = 300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(MaterialTheme.shapes.large)
                .border(BorderStroke(6.dp, Color(0xFF1B1B1B)), MaterialTheme.shapes.large)
        ) {
            if (day.backgroundUrl.isNotBlank()) {
                RemoteImage(url = day.backgroundUrl, contentDescription = null, maxWidth = 720, modifier = Modifier.matchParentSize())
            } else {
                Box(Modifier.matchParentSize().background(MaterialTheme.colorScheme.secondaryContainer))
            }
            // Darken the image so white text stays readable on any background.
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.6f)))
                )
            )
            Column(Modifier.matchParentSize().padding(20.dp)) {
                Text(
                    day.title,
                    style = MaterialTheme.typography.titleMedium.merge(ContentTextStyle),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        day.dua,
                        style = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Approximate preview; the app's layout may differ slightly.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// endregion

// region Dialogs

@Composable
private fun BackgroundPicker(state: RamadanState, holder: RamadanStateHolder) {
    ImageLibraryDialog(
        title = "Choose a background",
        hint = "Images in Storage islamic_pictures/. JPEG, PNG or WebP up to 1 MB; portrait (9:16) fits the app best.",
        images = state.backgrounds?.takeUnless { state.isLoadingBackgrounds && it.isEmpty() },
        selectedUrl = state.selected?.backgroundUrl.orEmpty(),
        aspectRatio = 9f / 16f,
        isUploading = state.isUploading,
        onPick = { holder.chooseBackground(it.url) },
        onImagePicked = holder::onImagePicked,
        onDismiss = holder::dismissPicker
    )
}

@Composable
private fun SaveDialogs(state: RamadanState, holder: RamadanStateHolder) {
    if (state.confirmSave) {
        val before = state.published.associateBy { it.id }
        val lines = state.draft.filter { it.id in state.changedIds }.map { day ->
            val old = before[day.id]
            val parts = buildList {
                if (old?.dua != day.dua) add("dua")
                if (old?.backgroundUrl != day.backgroundUrl) add("background")
            }
            "Day ${day.dayNumber}: ${parts.joinToString(", ")}"
        }
        val warnings = state.issues.filter { !it.isError && it.dayId in state.changedIds }
        AlertDialog(
            onDismissRequest = holder::dismissSave,
            title = { Text("Save Ramadan duas?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    lines.take(10).forEach { Text("• $it") }
                    if (lines.size > 10) Text("…and ${lines.size - 10} more days")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "The app reads these live, so users see the change the next time they open the duas screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    warnings.forEach { Text("• ${it.message}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary) }
                }
            },
            confirmButton = { Button(onClick = { holder.confirmSave() }) { Text("Save") } },
            dismissButton = { TextButton(onClick = holder::dismissSave) { Text("Cancel") } }
        )
    }

    if (state.conflictDays.isNotEmpty()) {
        val days = state.conflictDays.joinToString()
        AlertDialog(
            onDismissRequest = holder::dismissSave,
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Changed since you opened it") },
            text = {
                Text(
                    "Someone else saved day $days after you loaded the duas. Saving now replaces their change. " +
                        "Reload to see it first; this discards your unsaved edits."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { holder.confirmSave(overwrite = true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Save anyway") }
            },
            dismissButton = { TextButton(onClick = { holder.dismissSave(); holder.discardChanges(); holder.load() }) { Text("Reload") } }
        )
    }
}

// endregion
