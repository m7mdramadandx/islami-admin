package com.islami.presentation.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.islami.core.time.currentTimeMillis
import com.islami.domain.entities.ContentKind
import com.islami.presentation.navigation.Navigator
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(navigator: Navigator) {
    // Both editors stay alive while the screen is open, so switching tabs keeps unsaved edits.
    val editors = ContentKind.entries.associateWith { kind ->
        key(kind) { koinInject<ContentEditorStateHolder>(parameters = { parametersOf(kind) }) }
    }
    var selectedKind by remember { mutableStateOf(ContentKind.AZKAR) }
    val holder = editors.getValue(selectedKind)
    val state by holder.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(holder, state.message) {
        val message = state.message ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = if (state.undo != null) "Undo" else null,
            withDismissAction = state.undo == null,
            duration = if (state.undo != null) SnackbarDuration.Long else SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) holder.undo()
        holder.onMessageShown()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Azkar & Duas") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = holder::openBackups, enabled = state.published != null) {
                            Icon(Icons.Default.DateRange, contentDescription = "Backups")
                        }
                        IconButton(onClick = { holder.load() }, enabled = !state.isLoading && !state.isPublishing) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload from Storage")
                        }
                    }
                )
                PrimaryTabRow(selectedTabIndex = selectedKind.ordinal) {
                    ContentKind.entries.forEach { kind ->
                        val kindState by editors.getValue(kind).state.collectAsState()
                        Tab(
                            selected = kind == selectedKind,
                            onClick = { selectedKind = kind },
                            text = {
                                BadgedBox(badge = { if (kindState.changes.hasChanges) Badge() }) {
                                    Text(kind.title)
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.draft == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.draft == null -> LoadError(state, holder)
                else -> {
                    PublishBar(state, holder)
                    Banners(state, holder)
                    HorizontalDivider()
                    ContentEditor(state, holder, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    PublishDialogs(state, holder)
    BackupsDialog(state, holder)
}

@Composable
private fun LoadError(state: ContentEditorState, holder: ContentEditorStateHolder) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Text("Couldn't load ${state.kind.storagePath}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            state.loadError.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { holder.load() }) { Text("Try again") }
    }
}

// region Publish bar and banners

@Composable
private fun PublishBar(state: ContentEditorState, holder: ContentEditorStateHolder) {
    var confirmDiscard by remember { mutableStateOf(false) }
    val summary = state.changes.summary(state.kind)

    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).widthIn(min = 200.dp)) {
                Text(
                    text = if (summary.isEmpty()) "No unpublished changes" else summary.joinToString(" · "),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (summary.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Live: ${state.kind.storagePath} · version ${state.published?.version ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.errorCount > 0) {
                AssistChip(
                    onClick = holder::requestPublish,
                    label = { Text("${state.errorCount} to fix") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
            }
            TextButton(onClick = { confirmDiscard = true }, enabled = state.changes.hasChanges && !state.isPublishing) {
                Text("Discard")
            }
            Button(onClick = holder::requestPublish, enabled = state.canPublish) {
                if (state.isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Publishing…")
                } else {
                    Text("Publish")
                }
            }
        }
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard unpublished changes?") },
            text = { Text("This reverts ${state.kind.title.lowercase()} to the live file: ${summary.joinToString(", ").lowercase()}.") },
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
private fun Banners(state: ContentEditorState, holder: ContentEditorStateHolder) {
    if (state.versionBumpPending) {
        Banner(
            text = "The new file is uploaded, but apps keep their cached copy until the version number is bumped.",
            isError = true,
            actions = {
                TextButton(onClick = { holder.retryVersionBump() }, enabled = !state.isPublishing) { Text("Retry") }
            }
        )
    }
    state.restoredDraftSavedAt?.let { savedAt ->
        Banner(
            text = buildString {
                append("Restored unpublished changes from ${timeAgo(savedAt)}.")
                if (state.restoredDraftIsStale) {
                    append(" The live file was published again since then; publishing will replace those newer changes.")
                }
            },
            isError = state.restoredDraftIsStale,
            actions = {
                TextButton(onClick = holder::discardChanges) { Text("Discard") }
                TextButton(onClick = holder::dismissRestoredDraftNotice) { Text("Keep") }
            }
        )
    }
}

@Composable
private fun Banner(text: String, isError: Boolean, actions: @Composable RowScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(color = if (isError) colors.errorContainer else colors.secondaryContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isError) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = null,
                tint = if (isError) colors.onErrorContainer else colors.onSecondaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) colors.onErrorContainer else colors.onSecondaryContainer
            )
            actions()
        }
    }
}

private fun timeAgo(millis: Long): String {
    val minutes = (currentTimeMillis() - millis) / 60_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
        minutes < 60 * 24 -> "${minutes / 60} hour${if (minutes / 60 == 1L) "" else "s"} ago"
        else -> "${minutes / (60 * 24)} day${if (minutes / (60 * 24) == 1L) "" else "s"} ago"
    }
}

// endregion

// region Dialogs

@Composable
private fun PublishDialogs(state: ContentEditorState, holder: ContentEditorStateHolder) {
    val kind = state.kind
    if (state.confirmPublish) {
        val version = state.published?.version ?: 0
        val warnings = state.issues.filter { !it.isError }
        AlertDialog(
            onDismissRequest = holder::dismissPublish,
            title = { Text("Publish ${kind.title.lowercase()}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.changes.summary(kind).forEach { Text("• $it") }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Apps download the new file the next time they open (version $version to ${version + 1}). " +
                            "The current live file is backed up first, so you can restore it from Backups.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (warnings.isNotEmpty()) {
                        Text(
                            "${warnings.size} warning${if (warnings.size == 1) "" else "s"}:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        warnings.take(4).forEach {
                            Text("• ${it.message}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (warnings.size > 4) Text("…and ${warnings.size - 4} more", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = { Button(onClick = { holder.confirmPublish() }) { Text("Publish") } },
            dismissButton = { TextButton(onClick = holder::dismissPublish) { Text("Cancel") } }
        )
    }

    if (state.conflict) {
        AlertDialog(
            onDismissRequest = holder::dismissPublish,
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("${kind.title} changed since you opened it") },
            text = {
                Text(
                    "Someone published ${kind.storagePath} after you loaded it. Publishing now replaces their " +
                        "changes with yours. Reload to see their version first; your edits stay as a draft."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { holder.confirmPublish(overwrite = true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Publish anyway") }
            },
            dismissButton = {
                TextButton(onClick = { holder.dismissPublish(); holder.load() }) { Text("Reload") }
            }
        )
    }
}

@Composable
private fun BackupsDialog(state: ContentEditorState, holder: ContentEditorStateHolder) {
    val backups = state.backups ?: return
    AlertDialog(
        onDismissRequest = holder::dismissBackups,
        title = { Text("${state.kind.title} backups") },
        text = {
            Column {
                Text(
                    "A copy of the live file is saved every time you publish. Loading one puts it in the editor " +
                        "as unpublished changes. Nothing goes live until you publish.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                when {
                    state.isLoadingBackups -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    backups.isEmpty() -> Text("No backups yet.", style = MaterialTheme.typography.bodyMedium)
                    else -> LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(backups, key = { it.path }) { backup ->
                            ListItem(
                                headlineContent = { Text(backup.label) },
                                trailingContent = {
                                    TextButton(onClick = { holder.restoreBackup(backup) }) { Text("Load") }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = holder::dismissBackups) { Text("Close") } }
    )
}

// endregion
