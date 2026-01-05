package com.cil.shift.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Platform-specific system UI configuration.
 * On Android: Configures status bar and navigation bar.
 * On iOS: Configures status bar.
 */
@Composable
expect fun ConfigureSystemBars(
    isDarkTheme: Boolean = true,
    statusBarColor: Color = if (isDarkTheme) DarkBackgroundColor else LightBackgroundColor,
    navigationBarColor: Color = if (isDarkTheme) DarkBackgroundColor else LightBackgroundColor,
    isLightStatusBar: Boolean = !isDarkTheme,
    isLightNavigationBar: Boolean = !isDarkTheme
)
