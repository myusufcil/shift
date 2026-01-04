package com.cil.shift.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Configures Android system UI (status bar and navigation bar) for the app.
 * Sets dark theme with transparent system bars.
 */
@Composable
actual fun ConfigureSystemBars(
    statusBarColor: Color,
    navigationBarColor: Color,
    isLightStatusBar: Boolean,
    isLightNavigationBar: Boolean
) {
    val view = LocalView.current

    LaunchedEffect(statusBarColor, navigationBarColor, isLightStatusBar, isLightNavigationBar) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect

        // Set status bar color
        window.statusBarColor = statusBarColor.toArgb()

        // Set navigation bar color
        window.navigationBarColor = navigationBarColor.toArgb()

        // Configure window insets controller
        WindowCompat.getInsetsController(window, view).apply {
            // Light status bar (dark icons)
            isAppearanceLightStatusBars = isLightStatusBar

            // Light navigation bar (dark icons)
            isAppearanceLightNavigationBars = isLightNavigationBar
        }
    }
}
