package com.cil.shift.core.common.font

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Font size preference levels.
 * Each level applies a scaling factor to the base typography.
 */
enum class FontSize(val displayName: String, val scale: Float) {
    SMALL("Small", 0.85f),
    NORMAL("Normal", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra Large", 1.3f);

    companion object {
        fun fromString(value: String?): FontSize {
            return entries.find { it.name == value } ?: NORMAL
        }
    }
}

/**
 * Manages the app's font size preference.
 * Provides a reactive flow for font size changes.
 */
class FontSizeManager(
    private val fontSizePreferences: FontSizePreferences
) {
    private val _currentFontSize = MutableStateFlow(fontSizePreferences.getFontSize())
    val currentFontSize: StateFlow<FontSize> = _currentFontSize.asStateFlow()

    /**
     * Set the font size preference.
     */
    fun setFontSize(fontSize: FontSize) {
        fontSizePreferences.setFontSize(fontSize)
        _currentFontSize.value = fontSize
    }

    /**
     * Get the current font scale factor.
     */
    fun getCurrentScale(): Float = _currentFontSize.value.scale
}

/**
 * Interface for font size persistence.
 */
interface FontSizePreferences {
    fun getFontSize(): FontSize
    fun setFontSize(fontSize: FontSize)
}
