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
    statusBarColor: Color = Color(0xFF0A1628),
    navigationBarColor: Color = Color(0xFF0A1628),
    isLightStatusBar: Boolean = false,
    isLightNavigationBar: Boolean = false
)
