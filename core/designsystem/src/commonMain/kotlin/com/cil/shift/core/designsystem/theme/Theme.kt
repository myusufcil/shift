package com.cil.shift.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Dark color scheme for Shift app.
 * This is the primary theme as shown in design screenshots.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = OnPrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = OnPrimary,

    background = BackgroundDark,
    onBackground = OnBackgroundDark,

    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Surface2Dark,
    onSurfaceVariant = OnSurfaceDark,

    error = Error,
    onError = OnError,

    outline = Color(0xFF6C6C6C),
    outlineVariant = Color(0xFF3C3C3C),
)

/**
 * Light color scheme for Shift app.
 * Currently not used but available for future theming options.
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = Color.White,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryVariant,
    onSecondaryContainer = Color.White,

    background = Color.White,
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color.Black,

    error = Error,
    onError = OnError,
)

/**
 * Shift app theme.
 * Applies Material3 theming with custom colors, typography, and shapes.
 *
 * @param darkTheme Whether to use dark theme. Defaults to true as per design.
 * @param content The composable content to theme
 */
@Composable
fun ShiftTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ShiftTypography,
        shapes = ShiftShapes,
        content = content
    )
}
