package com.cil.shift.feature.habits.domain.model

import kotlinx.datetime.DayOfWeek

/**
 * Domain model representing a habit.
 *
 * @property id Unique identifier for the habit
 * @property name Display name of the habit
 * @property icon Icon identifier (e.g., "check", "fitness", "book")
 * @property color Hex color code (e.g., "#6C63FF")
 * @property frequency How often the habit should be performed
 * @property habitType Type of habit (simple checkbox, measurable, timer, etc.)
 * @property targetValue Target value for measurable/timer habits (e.g., 2000 for water, 120 for minutes)
 * @property targetUnit Unit for the target (e.g., "ml", "minutes", "pages")
 * @property reminderTime Optional reminder time in HH:mm format
 * @property notes Optional notes or description for the habit
 * @property sortOrder Position in the list for custom ordering (lower numbers appear first)
 * @property createdAt Timestamp when habit was created (epoch milliseconds)
 */
data class Habit(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val frequency: Frequency,
    val habitType: HabitType = HabitType.SIMPLE,
    val targetValue: Int? = null,
    val targetUnit: String? = null,
    val reminderTime: String?,
    val notes: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long
)

/**
 * Types of habits with different interaction patterns.
 */
enum class HabitType {
    SIMPLE,      // Simple checkbox (e.g., Read 20 mins, Workout)
    MEASURABLE,  // Trackable with numbers (e.g., Hydration: 1250ml / 2000ml)
    TIMER,       // Time-based with progress (e.g., Deep Work: 2h 15min left)
    SESSION      // Start/Stop session (e.g., Meditation: 15 mins goal)
}

/**
 * Represents how frequently a habit should be performed.
 */
sealed class Frequency {
    /**
     * Habit should be performed every day.
     */
    data object Daily : Frequency()

    /**
     * Habit should be performed on specific days of the week.
     *
     * @property days List of days when habit should be performed
     */
    data class Weekly(val days: List<DayOfWeek>) : Frequency()

    /**
     * Habit should be performed every N days.
     *
     * @property daysInterval Number of days between each occurrence
     */
    data class Custom(val daysInterval: Int) : Frequency()
}

/**
 * Extension to convert Frequency to a storable string format.
 * Format: "DAILY" | "WEEKLY:MON,WED,FRI" | "CUSTOM:3"
 */
fun Frequency.toStorageString(): String = when (this) {
    is Frequency.Daily -> "DAILY"
    is Frequency.Weekly -> "WEEKLY:${days.joinToString(",") { it.name }}"
    is Frequency.Custom -> "CUSTOM:$daysInterval"
}

/**
 * Extension to parse storage string back to Frequency.
 */
fun String.toFrequency(): Frequency {
    val parts = split(":")
    return when (parts[0]) {
        "DAILY" -> Frequency.Daily
        "WEEKLY" -> {
            val days = parts[1].split(",").map { DayOfWeek.valueOf(it) }
            Frequency.Weekly(days)
        }
        "CUSTOM" -> Frequency.Custom(parts[1].toInt())
        else -> Frequency.Daily // Default fallback
    }
}
