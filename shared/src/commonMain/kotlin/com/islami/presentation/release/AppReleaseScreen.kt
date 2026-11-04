package com.islami.presentation.release

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.islami.domain.entities.AppReleaseConfig
import com.islami.domain.entities.AppReleaseHistoryEntry
import com.islami.domain.entities.ContentKind
import com.islami.domain.release.ReleaseField
import com.islami.domain.release.UpdatePrompt
import com.islami.domain.release.isValidVersion
import com.islami.domain.release.normalized
import com.islami.domain.release.promptFor
import com.islami.presentation.navigation.Navigator
import org.koin.compose.koinInject
import kotlin.time.Instant

/** Above this width the preview sits beside the form instead of below it. */
private val WideLayoutBreakpoint = 1000.dp

private val ContentTextStyle = TextStyle(textDirection = TextDirection.Content)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppReleaseScreen(
    navigator: Navigator,
    holder: AppReleaseStateHolder = koinInject()
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
                title = { Text("App releases") },
                navigationIcon = {
                    IconButton(onClick = { if (state.hasChanges) confirmLeave = true else navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { holder.openHistory() }, enabled = state.published != null) {
                        Icon(Icons.Default.DateRange, contentDescription = "History")
                    }
                    IconButton(onClick = { holder.load() }, enabled = !state.isLoading && !state.isSaving) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.published == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.published == null -> LoadError(state.loadError.orEmpty()) { holder.load() }
                else -> {
                    SaveBar(state, holder)
                    HorizontalDivider()
                    ReleaseBody(state, holder, Modifier.weight(1f))
                }
            }
        }
    }

    SaveDialogs(state, holder)
    HistoryDialog(state, holder)
    BumpDialog(state, holder)

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Leave without saving?") },
            text = { Text("Your unsaved changes to the release config will be lost.") },
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
        Text("Couldn't load root/version", style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
private fun SaveBar(state: AppReleaseState, holder: AppReleaseStateHolder) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).widthIn(min = 200.dp)) {
                Text(
                    text = if (state.hasChanges) state.changeSummary.joinToString(" · ") else "No unsaved changes",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (state.hasChanges) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Live in Firestore: root/version",
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
            TextButton(onClick = holder::discardChanges, enabled = state.hasChanges && !state.isSaving) { Text("Discard") }
            Button(onClick = holder::requestSave, enabled = state.hasChanges && !state.isSaving) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Saving…")
                } else Text("Save")
            }
        }
    }
}

@Composable
private fun ReleaseBody(state: AppReleaseState, holder: AppReleaseStateHolder, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= WideLayoutBreakpoint) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    VersionsCard(state, holder)
                    NotesCard(state, holder, sideBySide = true)
                }
                Column(
                    modifier = Modifier.width(400.dp).fillMaxHeight().verticalScroll(rememberScrollState())
                        .padding(start = 12.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AudienceCard(state, holder)
                    WhatsNewPreview(state.form)
                    ContentVersionsCard(state, holder)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VersionsCard(state, holder)
                AudienceCard(state, holder)
                NotesCard(state, holder, sideBySide = false)
                WhatsNewPreview(state.form)
                ContentVersionsCard(state, holder)
            }
        }
    }
}

// region Form

@Composable
private fun VersionsCard(state: AppReleaseState, holder: AppReleaseStateHolder) {
    SectionCard("Versions") {
        VersionField(
            label = "Store version",
            value = state.form.storeVersion,
            hint = "Latest version in Google Play. Older apps get an update prompt they can dismiss, at most once a day.",
            issues = state.issuesFor(ReleaseField.STORE_VERSION).map { it.isError to it.message },
            onValueChange = holder::onStoreVersionChanged
        )
        VersionField(
            label = "Minimum version",
            value = state.form.minVersion,
            hint = "Apps below this are blocked by an update dialog with no Cancel button.",
            issues = state.issuesFor(ReleaseField.MIN_VERSION).map { it.isError to it.message },
            onValueChange = holder::onMinVersionChanged
        )
        if (state.raisesMinVersion && state.issuesFor(ReleaseField.MIN_VERSION).none { it.isError }) {
            IssueRow(
                isError = true,
                message = "Raising the minimum from ${state.published?.config?.minVersion} blocks every user below " +
                    "${state.form.minVersion.trim()} until they update. Only do this once that version is live in the store."
            )
        }
    }
}

@Composable
private fun VersionField(
    label: String,
    value: String,
    hint: String,
    issues: List<Pair<Boolean, String>>,
    onValueChange: (String) -> Unit
) {
    val error = issues.firstOrNull { it.first }?.second
    val warning = issues.firstOrNull { !it.first }?.second
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = {
            Text(
                error ?: warning ?: hint,
                color = when {
                    error != null -> MaterialTheme.colorScheme.error
                    warning != null -> MaterialTheme.colorScheme.tertiary
                    else -> Color.Unspecified
                }
            )
        }
    )
}

@Composable
private fun NotesCard(state: AppReleaseState, holder: AppReleaseStateHolder, sideBySide: Boolean) {
    SectionCard("Release notes", subtitle = "Shown on the app's \"App versions\" screen. Blank lines are dropped on save.") {
        val columns: @Composable (Modifier) -> Unit = { columnModifier ->
            NotesColumn("Arabic", ReleaseField.NOTES_AR, state.form.releaseNotesAr, state, holder, columnModifier)
            NotesColumn("English", ReleaseField.NOTES_EN, state.form.releaseNotesEn, state, holder, columnModifier)
        }
        if (sideBySide) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) { columns(Modifier.weight(1f)) }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { columns(Modifier.fillMaxWidth()) }
        }
        state.issues.filter { it.field == ReleaseField.NOTES_AR || it.field == ReleaseField.NOTES_EN }.forEach {
            IssueRow(isError = it.isError, message = it.message)
        }
    }
}

@Composable
private fun NotesColumn(
    title: String,
    field: ReleaseField,
    notes: List<String>,
    state: AppReleaseState,
    holder: AppReleaseStateHolder,
    modifier: Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        if (notes.isEmpty()) {
            Text(
                "No notes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        notes.forEachIndexed { index, note ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { holder.onNoteChanged(field, index, it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Line ${index + 1}") },
                    textStyle = MaterialTheme.typography.bodyMedium.merge(ContentTextStyle),
                    maxLines = 4,
                    enabled = !state.isSaving
                )
                Column {
                    IconButton(onClick = { holder.moveNote(field, index, -1) }, enabled = index > 0, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                    }
                    IconButton(onClick = { holder.moveNote(field, index, 1) }, enabled = index < notes.lastIndex, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                    }
                }
                IconButton(onClick = { holder.removeNote(field, index) }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove line")
                }
            }
        }
        TextButton(onClick = { holder.addNote(field) }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add line")
        }
    }
}

// endregion

// region Previews

@Composable
private fun AudienceCard(state: AppReleaseState, holder: AppReleaseStateHolder) {
    val form = state.form.normalized()
    val valid = isValidVersion(form.minVersion) && isValidVersion(form.storeVersion) && state.errorCount == 0
    SectionCard("Who sees what", subtitle = "How apps react on launch once this is saved") {
        if (!valid) {
            Text("Fix the versions to see who is affected.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            AudienceRow(UpdatePrompt.FORCE, "Below ${form.minVersion}", "Blocked until they update")
            if (form.minVersion != form.storeVersion) {
                AudienceRow(UpdatePrompt.SOFT, "${form.minVersion} up to ${form.storeVersion}", "Update prompt, can dismiss for the day")
            }
            AudienceRow(UpdatePrompt.NONE, "${form.storeVersion} and newer", "No prompt")

            HorizontalDivider()
            OutlinedTextField(
                value = state.testVersion,
                onValueChange = holder::onTestVersionChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Check an app version") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = {
                    if (isValidVersion(state.testVersion)) {
                        val prompt = form.promptFor(state.testVersion)
                        Text("Version ${state.testVersion}: ${prompt.describe()}", color = prompt.color())
                    } else {
                        Text("e.g. 4.3.0")
                    }
                }
            )
        }
    }
}

@Composable
private fun AudienceRow(prompt: UpdatePrompt, versions: String, effect: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(prompt.color()))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(versions, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(effect, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun UpdatePrompt.describe() = when (this) {
    UpdatePrompt.FORCE -> "blocked by the force-update dialog"
    UpdatePrompt.SOFT -> "sees a dismissible update prompt"
    UpdatePrompt.NONE -> "no prompt"
}

@Composable
private fun UpdatePrompt.color(): Color = when (this) {
    UpdatePrompt.FORCE -> MaterialTheme.colorScheme.error
    UpdatePrompt.SOFT -> MaterialTheme.colorScheme.tertiary
    UpdatePrompt.NONE -> MaterialTheme.colorScheme.primary
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhatsNewPreview(form: AppReleaseConfig) {
    var arabic by remember { mutableStateOf(true) }
    val notes = (if (arabic) form.releaseNotesAr else form.releaseNotesEn).map { it.trim() }.filter { it.isNotEmpty() }

    SectionCard("Preview", subtitle = "The app's \"What's new\" card") {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(selected = arabic, onClick = { arabic = true }, shape = SegmentedButtonDefaults.itemShape(0, 2)) {
                Text("Arabic")
            }
            SegmentedButton(selected = !arabic, onClick = { arabic = false }, shape = SegmentedButtonDefaults.itemShape(1, 2)) {
                Text("English")
            }
        }
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (arabic) "ما الجديد" else "What's new", style = MaterialTheme.typography.titleSmall)
                }
                if (notes.isEmpty()) {
                    Text(
                        "The app hides this card when there are no notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                notes.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium.merge(ContentTextStyle)) }
            }
        }
    }
}

@Composable
private fun ContentVersionsCard(state: AppReleaseState, holder: AppReleaseStateHolder) {
    SectionCard(
        "Content versions",
        subtitle = "Publishing in Azkar & Duas bumps these. Bump by hand only if an update didn't reach users."
    ) {
        ContentKind.entries.forEach { kind ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(kind.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${kind.versionField} = ${state.published?.contentVersions?.get(kind) ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = { holder.requestBump(kind) }) { Text("Force re-download") }
            }
        }
    }
}

// endregion

// region Dialogs

@Composable
private fun SaveDialogs(state: AppReleaseState, holder: AppReleaseStateHolder) {
    if (state.confirmSave) {
        val min = state.form.minVersion.trim()
        var typed by remember { mutableStateOf("") }
        val needsTyping = state.raisesMinVersion
        AlertDialog(
            onDismissRequest = holder::dismissSave,
            title = { Text("Save release config?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.changeSummary.forEach { Text("• $it") }
                    Text(
                        "Apps read this every time they open, so it takes effect right away.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    state.issues.filter { !it.isError }.forEach { IssueRow(isError = false, message = it.message) }
                    if (needsTyping) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Every user below $min will be blocked until they update. Type $min to confirm.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = typed,
                            onValueChange = { typed = it.trim() },
                            singleLine = true,
                            placeholder = { Text(min) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { holder.confirmSave() }, enabled = !needsTyping || typed == min) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = holder::dismissSave) { Text("Cancel") } }
        )
    }

    if (state.conflict) {
        AlertDialog(
            onDismissRequest = holder::dismissSave,
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Changed since you opened it") },
            text = {
                Text(
                    "Someone saved the release config after you loaded it. Saving now replaces their change. " +
                        "Reload to see it first; this discards your unsaved edits."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { holder.confirmSave(overwrite = true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Save anyway") }
            },
            dismissButton = { TextButton(onClick = { holder.dismissSave(); holder.load() }) { Text("Reload") } }
        )
    }
}

@Composable
private fun HistoryDialog(state: AppReleaseState, holder: AppReleaseStateHolder) {
    val history = state.history ?: return
    AlertDialog(
        onDismissRequest = holder::dismissHistory,
        title = { Text("Release config history") },
        text = {
            when {
                state.isLoadingHistory -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
                history.isEmpty() -> Text("No saves yet. Every save from this screen is recorded here.")
                else -> LazyColumn(modifier = Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(history, key = { it.id }) { HistoryRow(it, holder) }
                }
            }
        },
        confirmButton = { TextButton(onClick = holder::dismissHistory) { Text("Close") } }
    )
}

@Composable
private fun HistoryRow(entry: AppReleaseHistoryEntry, holder: AppReleaseStateHolder) {
    OutlinedCard {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${formatTime(entry.savedAt)}${entry.savedBy?.let { " · $it" }.orEmpty()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Store ${entry.before.storeVersion} to ${entry.after.storeVersion}", style = MaterialTheme.typography.bodyMedium)
            Text("Minimum ${entry.before.minVersion} to ${entry.after.minVersion}", style = MaterialTheme.typography.bodyMedium)
            if (entry.before.releaseNotesAr != entry.after.releaseNotesAr || entry.before.releaseNotesEn != entry.after.releaseNotesEn) {
                Text("Release notes changed", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                TextButton(onClick = { holder.restore(entry, before = true) }) { Text("Load values before") }
                TextButton(onClick = { holder.restore(entry, before = false) }) { Text("Load values after") }
            }
        }
    }
}

@Composable
private fun BumpDialog(state: AppReleaseState, holder: AppReleaseStateHolder) {
    val kind = state.confirmBump ?: return
    AlertDialog(
        onDismissRequest = holder::dismissBump,
        title = { Text("Force ${kind.title.lowercase()} re-download?") },
        text = {
            Text(
                "Every app downloads ${kind.storagePath} again the next time it opens. The file itself doesn't change. " +
                    "Use this if a publish didn't reach users."
            )
        },
        confirmButton = { Button(onClick = holder::confirmBump) { Text("Bump version") } },
        dismissButton = { TextButton(onClick = holder::dismissBump) { Text("Cancel") } }
    )
}

// endregion

@Composable
private fun SectionCard(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            content()
        }
    }
}

@Composable
private fun IssueRow(isError: Boolean, message: String) {
    val color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = color, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

private fun formatTime(millis: Long): String =
    Instant.fromEpochMilliseconds(millis).toString().take(16).replace('T', ' ') + " UTC"
