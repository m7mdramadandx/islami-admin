package com.islami.presentation.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.islami.presentation.navigation.Navigator
import org.koin.compose.koinInject

/** Above this width the live preview sits beside the form instead of above it. */
private val WideLayoutBreakpoint = 1000.dp

/** Arabic copy should align by its own script, not the (LTR) app layout. */
private val ContentDirection = TextDirection.Content

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navigator: Navigator,
    stateHolder: NotificationStateHolder = koinInject()
) {
    val state by stateHolder.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            stateHolder.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val wide = maxWidth >= WideLayoutBreakpoint
            if (wide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 24.dp, end = 12.dp, bottom = 24.dp)
                    ) {
                        TemplatesSection(state, stateHolder)
                        Spacer(modifier = Modifier.height(24.dp))
                        ComposeForm(state, stateHolder)
                    }
                    PreviewPanel(
                        state = state,
                        modifier = Modifier
                            .width(400.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 12.dp, end = 24.dp, top = 48.dp, bottom = 24.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    TemplatesSection(state, stateHolder)
                    Spacer(modifier = Modifier.height(16.dp))
                    PreviewPanel(state)
                    Spacer(modifier = Modifier.height(16.dp))
                    ComposeForm(state, stateHolder)
                }
            }
        }
    }

    if (state.confirmBroadcast) {
        AlertDialog(
            onDismissRequest = stateHolder::dismissBroadcast,
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            title = { Text("Broadcast to \"${state.topic.trim()}\"?") },
            text = {
                Column {
                    Text("Every device subscribed to this topic will receive the notification. This can't be undone.")
                    Spacer(modifier = Modifier.height(12.dp))
                    NotificationCard(state)
                }
            },
            confirmButton = {
                Button(onClick = stateHolder::confirmBroadcast) { Text("Send") }
            },
            dismissButton = {
                TextButton(onClick = stateHolder::dismissBroadcast) { Text("Cancel") }
            }
        )
    }
}

// region Templates

@Composable
private fun TemplatesSection(state: NotificationState, stateHolder: NotificationStateHolder) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quick Templates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = stateHolder::applyRandomTemplate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Random")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(notificationTemplates, key = { it.key }) { template ->
                TemplateCard(
                    template = template,
                    selected = template.key == state.selectedTemplate,
                    onClick = { stateHolder.applyTemplate(template) }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(template: NotificationTemplate, selected: Boolean, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.width(200.dp).height(136.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(template.emoji, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = template.title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall.copy(textDirection = ContentDirection),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = template.bodies.first(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(textDirection = ContentDirection),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// endregion

// region Form

@Composable
private fun ComposeForm(state: NotificationState, stateHolder: NotificationStateHolder) {
    val enabled = !state.isLoading
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormSection("Content")
            LabeledField(
                label = "Notification Title",
                value = state.title,
                onValueChange = stateHolder::onTitleChanged,
                icon = Icons.Default.Edit,
                placeholder = "أذكار اليوم",
                error = state.fieldErrors[NotificationField.TITLE],
                enabled = enabled,
                counter = state.title.length.takeIf { it > 0 }?.let { "$it / $TITLE_SOFT_LIMIT" },
                textDirection = ContentDirection
            )
            LabeledField(
                label = "Message Content",
                value = state.body,
                onValueChange = stateHolder::onBodyChanged,
                icon = Icons.AutoMirrored.Filled.List,
                placeholder = "اكتب نص الإشعار هنا…",
                error = state.fieldErrors[NotificationField.BODY],
                enabled = enabled,
                singleLine = false,
                counter = state.body.length.takeIf { it > 0 }?.let { "$it / $BODY_SOFT_LIMIT" },
                textDirection = ContentDirection
            )
            LabeledField(
                label = "Image URL (Optional)",
                value = state.imageUrl,
                onValueChange = stateHolder::onImageUrlChanged,
                icon = Icons.Default.Share,
                placeholder = "https://…",
                error = state.fieldErrors[NotificationField.IMAGE_URL],
                enabled = enabled,
                keyboardType = KeyboardType.Uri
            )

            FormSection("Target")
            FieldPair(
                first = {
                    LabeledField(
                        label = "Topic / Channel",
                        value = state.topic,
                        onValueChange = stateHolder::onTopicChanged,
                        glyph = "#",
                        placeholder = DEFAULT_TOPIC,
                        error = state.fieldErrors[NotificationField.TOPIC],
                        // A token overrides the topic, so make that visible instead of silently ignoring it.
                        enabled = enabled && !state.sendsToDevice
                    )
                },
                second = {
                    LabeledField(
                        label = "Direct FCM Token (Optional)",
                        value = state.fcmToken,
                        onValueChange = stateHolder::onFcmTokenChanged,
                        icon = Icons.Default.Lock,
                        placeholder = "fcm token…",
                        enabled = enabled
                    )
                }
            )
            Helper(
                if (state.sendsToDevice) "Sending to one device — the topic is ignored while a token is set."
                else "Target priority: FCM Token > Topic broadcast."
            )

            FormSection("Navigation")
            RouteDropdown(
                selected = state.route,
                onSelected = stateHolder::onRouteSelected,
                enabled = enabled
            )
            Helper("Choosing a route fills the title and message above; you can still edit them.")
            FieldPair(
                first = {
                    LabeledField(
                        label = "Extra Route ID (Optional)",
                        value = state.extraRouteId,
                        onValueChange = stateHolder::onExtraRouteIdChanged,
                        glyph = "{ }",
                        placeholder = "0",
                        error = state.fieldErrors[NotificationField.EXTRA_ROUTE_ID],
                        enabled = enabled,
                        keyboardType = KeyboardType.Number
                    )
                },
                second = {
                    LabeledField(
                        label = "Min Version (Optional)",
                        value = state.minVersion,
                        onValueChange = stateHolder::onMinVersionChanged,
                        icon = Icons.Default.Info,
                        placeholder = "1.0.0",
                        error = state.fieldErrors[NotificationField.MIN_VERSION],
                        enabled = enabled,
                        keyboardType = KeyboardType.Decimal
                    )
                }
            )
            Helper("These values are sent in the FCM data payload for deep-link/navigation handling.")

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = stateHolder::clear, enabled = enabled && !state.isEmpty) {
                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear")
                }
                Button(
                    onClick = stateHolder::requestSend,
                    enabled = enabled,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sending…")
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (state.sendsToDevice) "Send to device" else "Send to topic")
                    }
                }
            }
        }
    }
}

/** Android truncates beyond roughly these lengths in the collapsed tray view. */
private const val TITLE_SOFT_LIMIT = 65
private const val BODY_SOFT_LIMIT = 240

@Composable
private fun FormSection(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun Helper(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.offset(y = (-8).dp)
    )
}

/** Two fields side by side, stacking when the form is too narrow for both. */
@Composable
private fun FieldPair(first: @Composable () -> Unit, second: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= 560.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) { first() }
                Box(modifier = Modifier.weight(1f)) { second() }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                first()
                second()
            }
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    errorContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent
)

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    icon: ImageVector? = null,
    glyph: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    counter: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    textDirection: TextDirection = TextDirection.Unspecified
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 4,
            maxLines = if (singleLine) 1 else 8,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors(),
            textStyle = LocalTextStyle.current.copy(textDirection = textDirection),
            placeholder = { Text(placeholder, style = LocalTextStyle.current.copy(textDirection = textDirection)) },
            leadingIcon = when {
                icon != null -> ({ Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) })
                glyph != null -> ({ Text(glyph, fontWeight = FontWeight.Bold) })
                else -> null
            },
            trailingIcon = if (value.isNotEmpty() && enabled) {
                {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear $label", modifier = Modifier.size(18.dp))
                    }
                }
            } else null,
            isError = error != null,
            supportingText = when {
                error != null -> ({ Text(error) })
                counter != null -> ({ Text(counter, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) })
                else -> null
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteDropdown(
    selected: NotificationRoute?,
    onSelected: (NotificationRoute?) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            text = "Route ID (Optional)",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = it }
        ) {
            TextField(
                value = selected?.displayName ?: "Not set (opens dashboard)",
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors(),
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Not set (opens dashboard)") },
                    onClick = {
                        onSelected(null)
                        expanded = false
                    }
                )
                NotificationRoute.entries.forEach { route ->
                    DropdownMenuItem(
                        text = { Text(route.displayName) },
                        trailingIcon = route.defaultTitle?.let {
                            { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        },
                        onClick = {
                            onSelected(route)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// endregion

// region Preview

@Composable
private fun PreviewPanel(state: NotificationState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Live Preview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        NotificationCard(state)
        Spacer(modifier = Modifier.height(16.dp))
        PayloadSummary(state)
    }
}

/** Approximates how the notification renders in the Android tray. */
@Composable
private fun NotificationCard(state: NotificationState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🕌", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ISLAMI APP",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5f,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "now",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.title.ifBlank { "Notification title" },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall.copy(textDirection = ContentDirection),
                fontWeight = FontWeight.SemiBold,
                color = if (state.title.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.body.ifBlank { "Message content appears here" },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium.copy(textDirection = ContentDirection),
                color = if (state.body.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
            if (state.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Image: ${state.imageUrl.trim()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** What will actually be sent, so the admin can sanity-check the target and deep link. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PayloadSummary(state: NotificationState) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip(if (state.sendsToDevice) "Device token" else "Topic: ${state.topic.trim().ifBlank { "—" }}")
        SummaryChip("Opens: ${state.route?.label ?: "Dashboard"}")
        state.extraRouteId.takeIf { it.isNotBlank() && it != "0" }?.let { SummaryChip("Extra ID: $it") }
        state.minVersion.takeIf { it.isNotBlank() }?.let { SummaryChip("Min v${it.trim()}") }
    }
}

@Composable
private fun SummaryChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

// endregion
