package com.rapiddev.ah.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * متحكم تنقل بسيط — بديل Navigation Compose.
 * يحفظ الشاشة الحالية + مكدس للرجوع.
 */
class NavController(startScreen: Screen = Screen.Home) {
    var current by mutableStateOf(startScreen)
        private set
    private val stack = mutableListOf<Screen>()

    fun navigate(screen: Screen) {
        stack.add(current)
        current = screen
    }

    fun back() {
        current = if (stack.isNotEmpty()) stack.removeAt(stack.size - 1) else Screen.Home
    }

    fun resetTo(screen: Screen) {
        stack.clear()
        current = screen
    }

    fun canGoBack(): Boolean = stack.isNotEmpty()
}

@Composable
fun rememberNavController(startScreen: Screen = Screen.Home): NavController {
    return remember { NavController(startScreen) }
}