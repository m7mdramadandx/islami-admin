package com.islami.presentation.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.islami.core.state.UiEvent
import com.islami.presentation.navigation.Navigator
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navigator: Navigator,
    stateHolder: NotificationStateHolder = koinInject()
) {
    val state by stateHolder.state.collectAsState()
    val event by stateHolder.event.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(event) {
        when (event) {
            is UiEvent.ShowMessage -> snackbarHostState.showSnackbar((event as UiEvent.ShowMessage).message)
            is UiEvent.ShowError -> snackbarHostState.showSnackbar((event as UiEvent.ShowError).error)
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Send Notification") },
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { stateHolder.onTitleChanged(it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.body,
                onValueChange = { stateHolder.onBodyChanged(it) },
                label = { Text("Message Body") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { stateHolder.sendNotification() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Send Notification")
                }
            }
        }
    }
}
