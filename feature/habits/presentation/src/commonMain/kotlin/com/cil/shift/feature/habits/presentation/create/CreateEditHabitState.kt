package com.cil.shift.feature.habits.presentation.create

import com.cil.shift.feature.habits.domain.model.Frequency
import com.cil.shift.feature.habits.domain.model.HabitType
import kotlinx.datetime.DayOfWeek

enum class TimeOfDay(val displayName: String, val emoji: String) {
    ANYTIME("Anytime", "🌍"),
    MORNING("Morning", "🌅"),
    AFTERNOON("Afternoon", "☀️"),
    EVENING("Evening", "🌙")
}

data class CreateEditHabitState(
    val habitId: String? = null,
    val name: String = "",
    val selectedIcon: String = "water",
    val selectedColor: String = "#4E7CFF",
    val frequency: Frequency = Frequency.Daily,
    val habitType: HabitType = HabitType.SIMPLE,
    val targetValue: Int? = null,
    val targetUnit: String? = null,
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val hasReminder: Boolean = false,
    val reminderTime: String? = null,
    val reminderTimes: List<String> = emptyList(), // Multiple reminders
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val isNegative: Boolean = false,      // For NEGATIVE/reduce type habits
    val quitStartDate: Long? = null       // For QUIT type habits (null = today)
) {
    val isValid: Boolean
        get() = name.isNotBlank()
}

val availableIcons = listOf(
    "check", "work", "briefcase", "fitness", "workout",
    "book", "read", "meditation", "mindfulness",
    "water", "hydration", "sleep", "nutrition", "food",
    "study", "music", "art"
)

val availableColors = listOf(
    "#FF6B6B", // Red
    "#FF8C42", // Orange
    "#FFD93D", // Yellow
    "#6BCF7F", // Green
    "#4ECDC4", // Teal
    "#45B7D1", // Light Blue
    "#6C63FF", // Purple
    "#FF6BC9", // Pink
    "#A78BFA", // Lavender
    "#F472B6", // Hot Pink
    "#34D399", // Emerald
    "#60A5FA"  // Sky Blue
)

val daysOfWeek = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY
)
