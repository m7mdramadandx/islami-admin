package com.islami.presentation.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.islami.domain.entities.FeedbackMessage
import com.islami.presentation.navigation.Navigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    navigator: Navigator,
    stateHolder: FeedbackStateHolder = koinInject()
) {
    val state by stateHolder.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val visible = remember(state.messages, state.query, state.sort) {
        state.messages.filterAndSort(state.query, state.sort)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            stateHolder.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("User Feedback") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Centered and capped so messages stay readable on wide (web/tablet) screens.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 840.dp)
        ) {
            FeedbackToolbar(
                query = state.query,
                onQueryChange = stateHolder::onQueryChange,
                sort = state.sort,
                onSortChange = stateHolder::onSortChange,
                shown = visible.size,
                total = state.messages.size
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    state.error != null && state.messages.isEmpty() -> CenteredText(
                        text = state.error!!,
                        isError = true,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    state.messages.isEmpty() -> CenteredText("No feedback yet", modifier = Modifier.align(Alignment.Center))
                    visible.isEmpty() -> CenteredText(
                        "No feedback matches \"${state.query.trim()}\"",
                        modifier = Modifier.align(Alignment.Center)
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visible, key = { it.id }) { message ->
                            FeedbackItem(
                                message = message,
                                isDeleting = message.id in state.deletingIds,
                                onDelete = { stateHolder.requestDelete(message) }
                            )
                        }
                    }
                }
            }
        }
    }

    state.pendingDelete?.let { message ->
        AlertDialog(
            onDismissRequest = stateHolder::cancelDelete,
            title = { Text("Delete feedback?") },
            text = {
                Column {
                    Text("This permanently removes the message from Firestore.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message.msg.ifBlank { "(empty message)" },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = stateHolder::confirmDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = stateHolder::cancelDelete) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedbackToolbar(
    query: String,
    onQueryChange: (String) -> Unit,
    sort: FeedbackSort,
    onSortChange: (FeedbackSort) -> Unit,
    shown: Int,
    total: Int
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search message, email, phone, device, version") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            FeedbackSort.entries.forEach { option ->
                FilterChip(
                    selected = option == sort,
                    onClick = { onSortChange(option) },
                    label = { Text(option.label) }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (shown == total) "$total messages" else "$shown of $total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

private const val COLLAPSED_LINES = 4

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedbackItem(
    message: FeedbackMessage,
    isDeleting: Boolean,
    onDelete: () -> Unit
) {
    var expanded by rememberSaveable(message.id) { mutableStateOf(false) }
    var overflows by remember(message.id) { mutableStateOf(false) }
    val sender = message.email.takeIf { it.isNotBlank() }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 16.dp)) {
            // Header: who sent it and when.
            Row(verticalAlignment = Alignment.CenterVertically) {
                SenderAvatar(sender)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sender ?: "Anonymous",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (message.date.isNotBlank()) {
                        Text(
                            text = message.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                FeedbackActions(isDeleting = isDeleting, onDelete = onDelete)
            }

            // Body: the message itself, collapsed when long.
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Spacer(modifier = Modifier.height(12.dp))
                SelectionContainer {
                    Text(
                        text = message.msg.ifBlank { "(empty message)" },
                        modifier = Modifier.fillMaxWidth(),
                        // Content direction so Arabic messages align right.
                        style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Content),
                        color = if (message.msg.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow }
                    )
                }
                if (overflows || expanded) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                    ) {
                        Text(if (expanded) "Show less" else "Show more")
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Footer: device context as compact tags.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (message.appVersion.isNotBlank()) {
                        MetaTag(Icons.Default.Info, "v${message.appVersion}")
                    }
                    if (message.deviceName.isNotBlank()) {
                        MetaTag(Icons.Default.Build, message.deviceName)
                    }
                    if (message.phone.isNotBlank()) {
                        MetaTag(Icons.Default.Phone, message.phone)
                    }
                }
            }
        }
    }
}

@Composable
private fun SenderAvatar(sender: String?) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = sender?.firstOrNull()?.uppercase() ?: "?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun FeedbackActions(isDeleting: Boolean, onDelete: () -> Unit) {
    if (isDeleting) {
        CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(24.dp), strokeWidth = 2.dp)
        return
    }
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Feedback actions")
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = {
                    menuOpen = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun MetaTag(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CenteredText(text: String, modifier: Modifier = Modifier, isError: Boolean = false) {
    Text(
        text = text,
        modifier = modifier.padding(24.dp),
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
}
