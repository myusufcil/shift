package com.cil.shift.core.common.localization

enum class Language(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    TURKISH("tr", "Turkish", "Türkçe"),
    SPANISH("es", "Spanish", "Español"),
    FRENCH("fr", "French", "Français"),
    GERMAN("de", "German", "Deutsch"),
    PORTUGUESE("pt", "Portuguese", "Português"),
    ARABIC("ar", "Arabic", "العربية"),
    RUSSIAN("ru", "Russian", "Русский"),
    HINDI("hi", "Hindi", "हिन्दी"),
    JAPANESE("ja", "Japanese", "日本語"),
    CHINESE("zh", "Chinese", "中文"),
    ITALIAN("it", "Italian", "Italiano");

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
