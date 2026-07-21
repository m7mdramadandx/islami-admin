package com.islami.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

sealed class Screen {
    data object Login : Screen()
    data object Home : Screen()
    data object Quran : Screen()
    data object Hadith : Screen()
    data object Feedback : Screen()
    data object Notifications : Screen()
    data object Azkar : Screen()
}

class Navigator(initialScreen: Screen = Screen.Login) {
    private val _currentScreen = mutableStateOf(initialScreen)
    val currentScreen: Screen get() = _currentScreen.value

    private val backStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        backStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun pop() {
        if (backStack.isNotEmpty()) {
            _currentScreen.value = backStack.removeAt(backStack.size - 1)
        }
    }
}

@Composable
fun rememberNavigator(initialScreen: Screen = Screen.Login): Navigator {
    return remember { Navigator(initialScreen) }
}
