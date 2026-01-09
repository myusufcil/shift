package com.cil.shift.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cil.shift.core.designsystem.components.CoachMarkController

object GlobalNavigationEvents {
    var shouldNavigateToCreateHabit by mutableStateOf(false)
        private set

    var isFullScreenActive by mutableStateOf(false)

    // Global coach mark controller
    var coachMarkController: CoachMarkController? by mutableStateOf(null)

    // Track current active tab to prevent coach marks on wrong tab
    var currentTabIndex by mutableStateOf(0)

    fun navigateToCreateHabit() {
        shouldNavigateToCreateHabit = true
    }

    fun resetNavigateToCreateHabit() {
        shouldNavigateToCreateHabit = false
    }

    fun setCurrentTab(index: Int) {
        currentTabIndex = index
    }
}
