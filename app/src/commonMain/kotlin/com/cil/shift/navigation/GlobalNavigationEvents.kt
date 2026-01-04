package com.cil.shift.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object GlobalNavigationEvents {
    var shouldNavigateToCreateHabit by mutableStateOf(false)
        private set

    var isFullScreenActive by mutableStateOf(false)

    fun navigateToCreateHabit() {
        shouldNavigateToCreateHabit = true
    }

    fun resetNavigateToCreateHabit() {
        shouldNavigateToCreateHabit = false
    }
}
