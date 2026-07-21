package com.islami.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.islami.presentation.navigation.Navigator
import com.islami.presentation.navigation.Screen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigator: Navigator,
    stateHolder: HomeStateHolder = koinInject()
) {
    val state by stateHolder.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Islami Admin Dashboard") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        StatCard("Total Users", state.stats?.totalUsers?.toString() ?: "0", Icons.Default.People)
                    }
                    item {
                        StatCard("Feedback", state.stats?.totalFeedback?.toString() ?: "0", Icons.Default.Feedback) {
                            navigator.navigateTo(Screen.Feedback)
                        }
                    }
                    item {
                        StatCard("Quran Surahs", state.stats?.quranSurahs?.toString() ?: "0", Icons.Default.Book) {
                            navigator.navigateTo(Screen.Quran)
                        }
                    }
                    item {
                        StatCard("Hadith", state.stats?.totalHadith?.toString() ?: "0", Icons.Default.Book) {
                            navigator.navigateTo(Screen.Hadith)
                        }
                    }
                    item {
                        StatCard("Notifications", "Send", Icons.Default.Notifications) {
                            navigator.navigateTo(Screen.Notifications)
                        }
                    }
                    item {
                        StatCard("Azkar", "Manage", Icons.Default.Star) {
                            navigator.navigateTo(Screen.Azkar)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth().height(120.dp),
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}
