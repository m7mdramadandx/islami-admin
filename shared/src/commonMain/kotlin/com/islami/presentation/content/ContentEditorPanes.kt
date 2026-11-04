package com.islami.presentation.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.islami.domain.content.matches
import com.islami.domain.entities.ContentCategory
import com.islami.domain.entities.ContentField
import com.islami.domain.entities.ContentItem
import com.islami.domain.entities.ContentKind
import com.islami.domain.entities.text
import com.islami.domain.entities.titleOf

/** Above this width the category list sits beside the selected category instead of replacing it. */
private val TwoPaneBreakpoint = 840.dp

/** Arabic text should align by its own script, not the (LTR) app layout. */
private val ContentTextStyle = TextStyle(textDirection = TextDirection.Content)

@Composable
fun ContentEditor(state: ContentEditorState, holder: ContentEditorStateHolder, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= TwoPaneBreakpoint) {
            Row(modifier = Modifier.fillMaxSize()) {
                CategoryList(
                    state = state,
                    holder = holder,
                    onOpen = holder::selectCategory,
                    modifier = Modifier.width(340.dp).fillMaxHeight()
                )
                VerticalDivider()
                CategoryDetailOrEmpty(state, holder, onBack = null, modifier = Modifier.weight(1f))
            }
        } else {
            // On narrow screens the list and the detail take turns; the selection survives either way.
            var showDetail by remember(state.kind) { mutableStateOf(false) }
            if (showDetail && state.selectedCategory != null) {
                CategoryDetailOrEmpty(state, holder, onBack = { showDetail = false })
            } else {
                CategoryList(
                    state = state,
                    holder = holder,
                    onOpen = { holder.selectCategory(it); showDetail = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// region Category list

@Composable
private fun CategoryList(
    state: ContentEditorState,
    holder: ContentEditorStateHolder,
    onOpen: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val kind = state.kind
    val categories = state.draft?.categories.orEmpty()
    val visible = categories.filter { it.matches(kind, state.query) }
    val errorCategories = remember(state.issues) { state.issues.filter { it.isError }.map { it.categoryKey }.toSet() }

    Column(modifier = modifier) {
        SearchField(
            query = state.query,
            onQueryChanged = holder::onQueryChanged,
            placeholder = "Search ${kind.title.lowercase()}",
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
        Text(
            text = if (state.query.isBlank()) {
                "${categories.size} categories · ${categories.sumOf { it.items.size }} ${kind.itemNoun}s"
            } else {
                "${visible.size} of ${categories.size} categories match"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(visible, key = { it.key }) { category ->
                val matches = if (state.query.isBlank()) null else category.items.count { it.matches(state.query) }
                CategoryRow(
                    kind = kind,
                    category = category,
                    selected = category.key == state.selectedCategoryKey,
                    changed = category.key in state.changes.changedCategoryKeys,
                    hasErrors = category.key in errorCategories,
                    matchCount = matches,
                    onClick = { onOpen(category.key) }
                )
            }
        }
        HorizontalDivider()
        TextButton(
            onClick = holder::addCategory,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add category")
        }
    }
}

@Composable
private fun CategoryRow(
    kind: ContentKind,
    category: ContentCategory,
    selected: Boolean,
    changed: Boolean,
    hasErrors: Boolean,
    matchCount: Int?,
    onClick: () -> Unit
) {
    val title = kind.titleOf(category)
    NavigationDrawerItem(
        selected = selected,
        onClick = onClick,
        label = {
            Column {
                Text(
                    text = title.ifEmpty { "Untitled category" },
                    style = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle),
                    fontStyle = if (title.isEmpty()) FontStyle.Italic else FontStyle.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append("${category.items.size} ${kind.itemNoun}s · id ${category.id}")
                        if (matchCount != null && matchCount > 0) append(" · $matchCount match")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        badge = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (hasErrors) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Has problems",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (changed) ChangedDot()
            }
        },
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun ChangedDot() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiary)
    )
}

@Composable
private fun SearchField(query: String, onQueryChanged: (String) -> Unit, placeholder: String, modifier: Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        textStyle = ContentTextStyle
    )
}

// endregion

// region Category detail

@Composable
private fun CategoryDetailOrEmpty(
    state: ContentEditorState,
    holder: ContentEditorStateHolder,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val category = state.selectedCategory
    if (category == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Select a category to edit its ${state.kind.itemNoun}s",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        CategoryDetail(state, holder, category, onBack, modifier)
    }
}

@Composable
private fun CategoryDetail(
    state: ContentEditorState,
    holder: ContentEditorStateHolder,
    category: ContentCategory,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val kind = state.kind
    val categories = state.draft?.categories.orEmpty()
    val index = categories.indexOfFirst { it.key == category.key }
    val publishedItems = remember(state.published) {
        state.published?.document?.categories.orEmpty().flatMap { it.items }.associateBy { it.key }
    }
    val categoryIssues = state.issues.filter { it.categoryKey == category.key && it.itemKey == null }
    val itemsWithErrors = state.issues.filter { it.categoryKey == category.key && it.isError }.mapNotNull { it.itemKey }.toSet()
    val visibleItems = category.items.filter { it.matches(state.query) }
    val listState = rememberLazyListState()
    var confirmDelete by remember(category.key) { mutableStateOf(false) }
    var movingItem by remember(category.key) { mutableStateOf<ContentItem?>(null) }

    // Bring a newly added item into view so the admin can start typing straight away.
    var lastCount by remember(category.key) { mutableStateOf(category.items.size) }
    LaunchedEffect(category.key, category.items.size) {
        if (category.items.size > lastCount && state.query.isBlank()) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
        lastCount = category.items.size
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            CategoryHeader(
                kind = kind,
                category = category,
                issues = categoryIssues.map { it.message },
                hasDuplicateId = categories.count { it.id == category.id } > 1,
                canMoveUp = index > 0,
                canMoveDown = index in 0 until categories.lastIndex,
                onBack = onBack,
                onFieldChanged = { field, value -> holder.onCategoryFieldChanged(category.key, field, value) },
                onMove = { holder.moveCategory(category.key, it) },
                onAssignNewId = { holder.assignNewCategoryId(category.key) },
                onDelete = { confirmDelete = true }
            )
        }

        if (state.query.isNotBlank()) {
            item(key = "filter") {
                Text(
                    "Showing ${visibleItems.size} of ${category.items.size} ${kind.itemNoun}s matching \"${state.query}\"",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(visibleItems, key = { it.key }) { item ->
            val position = category.items.indexOf(item)
            val published = publishedItems[item.key]
            ItemCard(
                kind = kind,
                item = item,
                position = position,
                status = when {
                    published == null -> "New"
                    published.fields != item.fields -> "Edited"
                    else -> null
                },
                showErrors = item.key in itemsWithErrors,
                canMoveUp = position > 0,
                canMoveDown = position < category.items.lastIndex,
                canMoveToCategory = categories.size > 1,
                onFieldChanged = { field, value -> holder.onItemFieldChanged(category.key, item.key, field, value) },
                onMove = { holder.moveItem(category.key, item.key, it) },
                onDuplicate = { holder.duplicateItem(category.key, item.key) },
                onMoveToCategory = { movingItem = item },
                onDelete = { holder.deleteItem(category.key, item.key) }
            )
        }

        item(key = "add") {
            OutlinedButton(onClick = { holder.addItem(category.key) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add ${kind.itemNoun}")
            }
        }
    }

    if (confirmDelete) {
        val name = kind.titleOf(category).ifEmpty { "this category" }
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Delete \"$name\"?") },
            text = {
                Text(
                    "Its ${category.items.size} ${kind.itemNoun}s are removed too. Notifications that open " +
                        "category id ${category.id} will stop working once you publish."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { confirmDelete = false; holder.deleteCategory(category.key) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }

    movingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { movingItem = null },
            title = { Text("Move ${kind.itemNoun} to…") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(categories.filter { it.key != category.key }, key = { it.key }) { target ->
                        ListItem(
                            headlineContent = {
                                Text(kind.titleOf(target).ifEmpty { "Untitled category" }, style = ContentTextStyle)
                            },
                            supportingContent = { Text("${target.items.size} ${kind.itemNoun}s") },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clip(MaterialTheme.shapes.small).clickable {
                                movingItem = null
                                holder.moveItemToCategory(category.key, item.key, target.key)
                            }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { movingItem = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CategoryHeader(
    kind: ContentKind,
    category: ContentCategory,
    issues: List<String>,
    hasDuplicateId: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onBack: (() -> Unit)?,
    onFieldChanged: (String, String) -> Unit,
    onMove: (Int) -> Unit,
    onAssignNewId: () -> Unit,
    onDelete: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "All categories")
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Category", style = MaterialTheme.typography.titleMedium)
                    Text(
                        // The app opens categories by this id, e.g. from a notification's extra route id.
                        "App id ${category.id} · use it as a notification's extra route id",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onMove(-1) }, enabled = canMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move category up")
                }
                IconButton(onClick = { onMove(1) }, enabled = canMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move category down")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete category", tint = MaterialTheme.colorScheme.error)
                }
            }

            kind.categoryFields.forEach { field ->
                ContentTextField(field, category.text(field.key), showError = true, singleLine = true) {
                    onFieldChanged(field.key, it)
                }
            }

            issues.forEach { message ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
            if (hasDuplicateId) {
                TextButton(onClick = onAssignNewId) { Text("Give this category a new id") }
            }
        }
    }
}

// endregion

// region Items

@Composable
private fun ItemCard(
    kind: ContentKind,
    item: ContentItem,
    position: Int,
    status: String?,
    showErrors: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canMoveToCategory: Boolean,
    onFieldChanged: (String, String) -> Unit,
    onMove: (Int) -> Unit,
    onDuplicate: () -> Unit,
    onMoveToCategory: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    OutlinedCard {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${kind.itemNoun.replaceFirstChar(Char::uppercase)} ${position + 1}",
                    style = MaterialTheme.typography.labelLarge
                )
                if (status != null) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            status,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onMove(-1) }, enabled = canMoveUp) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = { onMove(1) }, enabled = canMoveDown) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down")
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More actions")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Duplicate") }, onClick = { menuOpen = false; onDuplicate() })
                        if (canMoveToCategory) {
                            DropdownMenuItem(
                                text = { Text("Move to another category…") },
                                onClick = { menuOpen = false; onMoveToCategory() }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                kind.itemFields.forEach { field ->
                    ContentTextField(field, item.text(field.key), showError = showErrors) { onFieldChanged(field.key, it) }
                }
            }
        }
    }
}

@Composable
private fun ContentTextField(
    field: ContentField,
    value: String,
    showError: Boolean,
    singleLine: Boolean = !field.multiline,
    onValueChange: (String) -> Unit
) {
    val isError = showError && field.required && value.isBlank()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(if (field.required) field.label else "${field.label} (optional)") },
        supportingText = when {
            isError -> ({ Text("Required") })
            field.hint != null -> ({ Text(field.hint) })
            else -> null
        },
        isError = isError,
        singleLine = singleLine,
        minLines = if (field.multiline) 2 else 1,
        maxLines = if (field.multiline) 12 else 1,
        textStyle = MaterialTheme.typography.bodyLarge.merge(ContentTextStyle)
    )
}

// endregion
