package com.cil.shift.core.designsystem.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
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
 * Animates a color change with smooth transition.
 */
@Composable
private fun animateColor(targetValue: Color): Color {
    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 400)
    ).value
}

/**
 * Creates an animated ColorScheme that smoothly transitions between colors.
 */
@Composable
private fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
    return targetColorScheme.copy(
        primary = animateColor(targetColorScheme.primary),
        onPrimary = animateColor(targetColorScheme.onPrimary),
        primaryContainer = animateColor(targetColorScheme.primaryContainer),
        onPrimaryContainer = animateColor(targetColorScheme.onPrimaryContainer),
        secondary = animateColor(targetColorScheme.secondary),
        onSecondary = animateColor(targetColorScheme.onSecondary),
        secondaryContainer = animateColor(targetColorScheme.secondaryContainer),
        onSecondaryContainer = animateColor(targetColorScheme.onSecondaryContainer),
        tertiary = animateColor(targetColorScheme.tertiary),
        onTertiary = animateColor(targetColorScheme.onTertiary),
        tertiaryContainer = animateColor(targetColorScheme.tertiaryContainer),
        onTertiaryContainer = animateColor(targetColorScheme.onTertiaryContainer),
        error = animateColor(targetColorScheme.error),
        onError = animateColor(targetColorScheme.onError),
        errorContainer = animateColor(targetColorScheme.errorContainer),
        onErrorContainer = animateColor(targetColorScheme.onErrorContainer),
        background = animateColor(targetColorScheme.background),
        onBackground = animateColor(targetColorScheme.onBackground),
        surface = animateColor(targetColorScheme.surface),
        onSurface = animateColor(targetColorScheme.onSurface),
        surfaceVariant = animateColor(targetColorScheme.surfaceVariant),
        onSurfaceVariant = animateColor(targetColorScheme.onSurfaceVariant),
        outline = animateColor(targetColorScheme.outline),
        outlineVariant = animateColor(targetColorScheme.outlineVariant),
        scrim = animateColor(targetColorScheme.scrim),
        inverseSurface = animateColor(targetColorScheme.inverseSurface),
        inverseOnSurface = animateColor(targetColorScheme.inverseOnSurface),
        inversePrimary = animateColor(targetColorScheme.inversePrimary)
    )
}

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
    val targetColorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Animate the color scheme for smooth transitions
    val animatedColorScheme = animateColorScheme(targetColorScheme)

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = ShiftTypography,
        shapes = ShiftShapes,
        content = content
    )
}
