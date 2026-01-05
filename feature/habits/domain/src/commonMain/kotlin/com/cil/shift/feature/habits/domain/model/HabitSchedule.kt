package com.cil.shift.feature.habits.domain.model

/**
 * Represents a scheduled habit event on the calendar
 */
data class HabitSchedule(
    val id: String,
    val habitId: String,
    val habitName: String,
    val habitIcon: String,
    val habitColor: String,
    val date: String, // YYYY-MM-DD format
    val startTime: String, // HH:mm format
    val endTime: String, // HH:mm format
    val hasReminder: Boolean = true,
    val repeatType: RepeatType = RepeatType.NEVER,
    val notes: String? = null,
    val createdAt: Long
)

/**
 * Repeat options for scheduled events
 */
enum class RepeatType {
    NEVER,
    DAILY,
    WEEKLY,
    MONTHLY
}
