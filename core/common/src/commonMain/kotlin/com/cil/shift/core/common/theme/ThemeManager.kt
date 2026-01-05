package com.cil.shift.core.common.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme(val displayName: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System")
}

class ThemeManager(
    private val preferences: ThemePreferences
) {
    private val _currentTheme = MutableStateFlow(AppTheme.DARK)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    init {
        // Load saved theme preference
        val savedTheme = preferences.getTheme()
        _currentTheme.value = savedTheme?.let {
            try { AppTheme.valueOf(it) } catch (e: Exception) { AppTheme.DARK }
        } ?: AppTheme.DARK
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        preferences.setTheme(theme.name)
    }

    fun isDarkTheme(isSystemInDarkTheme: Boolean): Boolean {
        return when (_currentTheme.value) {
            AppTheme.DARK -> true
            AppTheme.LIGHT -> false
            AppTheme.SYSTEM -> isSystemInDarkTheme
        }
    }
}

// CompositionLocal for accessing ThemeManager in Composables
val LocalThemeManager = compositionLocalOf<ThemeManager> {
    error("ThemeManager not provided")
}

@Composable
fun ProvideTheme(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        content()
    }
}

// Interface for platform-specific theme preferences storage
interface ThemePreferences {
    fun getTheme(): String?
    fun setTheme(theme: String)
}
