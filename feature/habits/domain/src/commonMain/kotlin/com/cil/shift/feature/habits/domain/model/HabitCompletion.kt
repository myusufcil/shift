package com.cil.shift.feature.habits.domain.model

/**
 * Domain model representing a completion record for a habit on a specific date.
 *
 * @property habitId ID of the habit that was completed
 * @property date Date of completion in YYYY-MM-DD format
 * @property isCompleted Whether the habit is completed for this date
 * @property currentValue Current progress value for measurable/timer habits
 * @property note Optional note or comment for this completion
 */
data class HabitCompletion(
    val habitId: String,
    val date: String,
    val isCompleted: Boolean = false,
    val currentValue: Int = 0,
    val note: String? = null
)
