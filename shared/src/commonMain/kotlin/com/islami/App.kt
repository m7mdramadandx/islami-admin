package com.islami

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.islami.presentation.auth.LoginScreen
import com.islami.presentation.home.HomeScreen
import com.islami.presentation.quran.QuranScreen
import com.islami.presentation.hadith.HadithScreen
import com.islami.presentation.feedback.FeedbackScreen
import com.islami.presentation.notifications.NotificationScreen
import com.islami.presentation.azkar.AzkarScreen
import com.islami.presentation.navigation.Screen
import com.islami.presentation.navigation.rememberNavigator
import com.islami.presentation.theme.IslamiTheme

@Composable
fun App() {
    val navigator = rememberNavigator(Screen.Login)

    IslamiTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (navigator.currentScreen) {
                is Screen.Login -> LoginScreen(navigator)
                is Screen.Home -> HomeScreen(navigator)
                is Screen.Quran -> QuranScreen(navigator)
                is Screen.Hadith -> HadithScreen(navigator)
                is Screen.Feedback -> FeedbackScreen(navigator)
                is Screen.Notifications -> NotificationScreen(navigator)
                is Screen.Azkar -> AzkarScreen(navigator)
                // Add other screens here as they are implemented
                else -> {}
            }
        }
    }
}
