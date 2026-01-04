package com.cil.shift.core.common

/**
 * Application-wide constants.
 */
object Constants {
    // Date & Time formats
    const val DATE_FORMAT_ISO = "yyyy-MM-dd"
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val TIME_FORMAT_24H = "HH:mm"
    const val DATETIME_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss"

    // Database
    const val DATABASE_NAME = "shift.db"
    const val DATABASE_VERSION = 1

    // Preferences keys
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_USER_NAME = "user_name"
    const val PREF_THEME_MODE = "theme_mode"

    // Default values
    const val DEFAULT_HABIT_COLOR = "#6C63FF"
    const val DEFAULT_HABIT_ICON = "check"

    // Habit colors
    val HABIT_COLORS = listOf(
        "#6C63FF", // Blue
        "#00BCD4", // Cyan
        "#E91E63", // Pink
        "#4CAF50", // Green
        "#FF9800", // Orange
        "#9C27B0", // Purple
        "#009688", // Teal
        "#F44336", // Red
    )

    // Habit icons (icon identifiers)
    val HABIT_ICONS = listOf(
        "check",
        "fitness",
        "book",
        "water",
        "meditation",
        "sleep",
        "run",
        "food",
    )
}
