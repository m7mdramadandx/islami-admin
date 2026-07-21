package com.islami.presentation.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.islami.core.state.UiEvent
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
    val event by stateHolder.event.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(event) {
        when (event) {
            is UiEvent.ShowMessage -> {
                snackbarHostState.showSnackbar((event as UiEvent.ShowMessage).message)
            }
            is UiEvent.ShowError -> {
                snackbarHostState.showSnackbar((event as UiEvent.ShowError).error)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("User Feedback") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (state.isLoading && state.messages.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null && state.messages.isEmpty()) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.messages) { message ->
                        FeedbackItem(message) { response ->
                            stateHolder.respondToFeedback(message.id, response)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackItem(
    message: FeedbackMessage,
    onRespond: (String) -> Unit
) {
    var responseText by remember { mutableStateOf("") }
    var showRespondField by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = message.email, style = MaterialTheme.typography.titleSmall)
                Text(text = message.date, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message.msg, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "App Version: ${message.appVersion} | Device: ${message.deviceName}", style = MaterialTheme.typography.labelSmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (showRespondField) {
                OutlinedTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    label = { Text("Response") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showRespondField = false }) {
                        Text("Cancel")
                    }
                    Button(onClick = { 
                        onRespond(responseText)
                        showRespondField = false
                        responseText = ""
                    }) {
                        Text("Send")
                    }
                }
            } else {
                Button(onClick = { showRespondField = true }) {
                    Text("Respond")
                }
            }
        }
    }
}
