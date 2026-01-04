package com.cil.shift.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * iOS implementation of system UI configuration.
 * Currently a no-op as iOS system bars are handled differently.
 */
@Composable
actual fun ConfigureSystemBars(
    statusBarColor: Color,
    navigationBarColor: Color,
    isLightStatusBar: Boolean,
    isLightNavigationBar: Boolean
) {
    // iOS system bars are configured through Info.plist and UIViewController
    // No-op for now
}
