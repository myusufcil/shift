package com.cil.shift.core.common.localization

enum class Language(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    TURKISH("tr", "Turkish", "Türkçe"),
    SPANISH("es", "Spanish", "Español"); // Third language

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}
