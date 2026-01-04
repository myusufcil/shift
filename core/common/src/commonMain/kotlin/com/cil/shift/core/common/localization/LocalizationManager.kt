package com.cil.shift.core.common.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalizationManager(
    private val preferences: LanguagePreferences
) {
    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    init {
        // Load saved language preference
        val savedLanguageCode = preferences.getLanguageCode()
        _currentLanguage.value = Language.fromCode(savedLanguageCode ?: Language.ENGLISH.code)
    }

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
        preferences.setLanguageCode(language.code)
    }

    fun getString(resource: StringResource): String {
        return resource.get(_currentLanguage.value)
    }
}

// CompositionLocal for accessing LocalizationManager in Composables
val LocalLocalization = compositionLocalOf<LocalizationManager> {
    error("LocalizationManager not provided")
}

@Composable
fun ProvideLocalization(
    localizationManager: LocalizationManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLocalization provides localizationManager) {
        content()
    }
}

// Convenience extension function to get strings in Composables
@Composable
fun StringResource.localized(): String {
    val localizationManager = LocalLocalization.current
    return localizationManager.getString(this)
}

// Interface for platform-specific language preferences storage
interface LanguagePreferences {
    fun getLanguageCode(): String?
    fun setLanguageCode(code: String)
}
