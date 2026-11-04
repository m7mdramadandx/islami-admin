package com.islami.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.islami.domain.entities.ContentKind
import com.islami.presentation.components.StarPattern
import com.islami.presentation.navigation.Navigator
import com.islami.presentation.navigation.Screen
import islami_kmp.shared.generated.resources.Res
import islami_kmp.shared.generated.resources.ic_app
import org.jetbrains.compose.resources.painterResource
import com.islami.presentation.theme.IslamiBrand
import org.koin.compose.koinInject

/** Keeps lines and cards a readable size on wide monitors. */
private val MaxContentWidth = 1100.dp

private data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val screen: Screen
)

private val features = listOf(
    Feature(
        "Azkar & Duas",
        "Edit the azkar and duas the app downloads. Every publish is backed up.",
        Icons.Default.Star,
        Screen.Content
    ),
    Feature(
        "Notifications",
        "Send a push notification to a topic or a single device.",
        Icons.Default.Notifications,
        Screen.Notifications
    ),
    Feature(
        "Feedback",
        "Read and clean up the messages users send from the app.",
        Icons.Default.Email,
        Screen.Feedback
    ),
    Feature(
        "App Releases",
        "Store and minimum versions, update prompts and release notes.",
        Icons.Default.Settings,
        Screen.Releases
    )
)

@Composable
fun HomeScreen(
    navigator: Navigator,
    email: String?,
    onLogout: () -> Unit,
    holder: HomeStateHolder = koinInject()
) {
    val state by holder.state.collectAsState()
    var confirmLogout by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Header(email = email, onLogout = { confirmLogout = true })

        Column(
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            LiveNow(state, onRefresh = { holder.refresh() }, onOpen = { navigator.navigateTo(Screen.Releases) })

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionTitle("Manage")
                BoxWithConstraints {
                    val columns = when {
                        maxWidth >= 900.dp -> 2
                        else -> 1
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        features.chunked(columns).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                row.forEach { feature ->
                                    FeatureCard(feature, Modifier.weight(1f)) { navigator.navigateTo(feature.screen) }
                                }
                                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Sign out?") },
            text = { Text("You'll need your email and password to sign in again.") },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; onLogout() }) { Text("Sign out") }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun Header(email: String?, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(IslamiBrand.Forest)) {
        StarPattern(color = IslamiBrand.Gold.copy(alpha = 0.07f), spacing = 80.dp, modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .widthIn(max = MaxContentWidth)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.ic_app),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Islami Admin",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = IslamiBrand.OnForest,
                    modifier = Modifier.weight(1f)
                )
                if (email != null) {
                    Text(
                        email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = IslamiBrand.OnForest.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                }
                TextButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.textButtonColors(contentColor = IslamiBrand.OnForest)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sign out")
                }
            }
            Spacer(Modifier.height(32.dp))
            Text("السلام عليكم", style = MaterialTheme.typography.titleLarge, color = IslamiBrand.Gold)
            Spacer(Modifier.height(4.dp))
            Text(
                "What would you like to manage today?",
                style = MaterialTheme.typography.headlineSmall,
                color = IslamiBrand.OnForest
            )
        }
    }
}

@Composable
private fun LiveNow(state: HomeState, onRefresh: () -> Unit, onOpen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Live in the app", modifier = Modifier.weight(1f))
            if (state.loadFailed) {
                Text(
                    "Couldn't load",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
        val live = state.live
        val placeholder = if (state.isLoading) "…" else "—"
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = 4
        ) {
            val tile = Modifier.weight(1f).widthIn(min = 150.dp)
            StatTile("Store version", live?.config?.storeVersion?.ifEmpty { null } ?: placeholder, tile, onOpen)
            StatTile("Minimum version", live?.config?.minVersion?.ifEmpty { null } ?: placeholder, tile, onOpen)
            ContentKind.entries.forEach { kind ->
                StatTile(
                    "${kind.title} file",
                    live?.contentVersions?.get(kind)?.let { "v$it" } ?: placeholder,
                    tile,
                    onOpen
                )
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FeatureCard(feature: Feature, modifier: Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = colors.surface)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(feature.icon, contentDescription = null, tint = colors.primary)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(feature.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(feature.description, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = modifier)
}
