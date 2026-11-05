package com.islami

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.islami.presentation.auth.LoginScreen
import com.islami.presentation.auth.SessionState
import com.islami.presentation.auth.SessionStateHolder
import com.islami.presentation.home.HomeScreen
import com.islami.presentation.feedback.FeedbackScreen
import com.islami.presentation.notifications.NotificationScreen
import com.islami.presentation.content.ContentScreen
import com.islami.presentation.release.AppReleaseScreen
import com.islami.presentation.ramadan.RamadanScreen
import com.islami.presentation.stories.StoriesScreen
import com.islami.presentation.navigation.Screen
import com.islami.presentation.navigation.rememberNavigator
import com.islami.presentation.theme.IslamiTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val session: SessionStateHolder = koinInject()
    val sessionState by session.state.collectAsState()

    IslamiTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (sessionState is SessionState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Surface
            }

            val isSignedIn = sessionState is SessionState.SignedIn
            // A fresh back stack whenever the admin signs in or out.
            key(isSignedIn) {
                val navigator = rememberNavigator(if (isSignedIn) Screen.Home else Screen.Login)

                when (navigator.currentScreen) {
                    is Screen.Login -> LoginScreen()
                    is Screen.Home -> HomeScreen(
                        navigator,
                        email = (sessionState as? SessionState.SignedIn)?.user?.email,
                        onLogout = session::logout
                    )
                    is Screen.Feedback -> FeedbackScreen(navigator)
                    is Screen.Notifications -> NotificationScreen(navigator)
                    is Screen.Content -> ContentScreen(navigator)
                    is Screen.Releases -> AppReleaseScreen(navigator)
                    is Screen.Ramadan -> RamadanScreen(navigator)
                    is Screen.Stories -> StoriesScreen(navigator)
                }
            }
        }
    }
}
