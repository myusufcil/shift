package com.cil.shift.feature.habits.data.mapper

import com.cil.shift.feature.habits.data.database.Habit as HabitEntity
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.model.HabitType
import com.cil.shift.feature.habits.domain.model.toFrequency
import com.cil.shift.feature.habits.domain.model.toStorageString

/**
 * Maps database Habit entity to domain Habit model.
 */
fun HabitEntity.toDomain(): Habit {
    val frequencyString = if (frequency_data != null) {
        "$frequency_type:$frequency_data"
    } else {
        frequency_type
    }

    return Habit(
        id = id,
        name = name,
        icon = icon,
        color = color,
        frequency = frequencyString.toFrequency(),
        habitType = HabitType.valueOf(habit_type ?: "SIMPLE"),
        targetValue = target_value?.toInt(),
        targetUnit = target_unit,
        reminderTime = reminder_time,
        notes = notes,
        sortOrder = sort_order?.toInt() ?: 0,
        createdAt = created_at
    )
}

/**
 * Maps domain Habit model to database insert parameters.
 */
fun Habit.toEntity(): HabitInsertParams {
    val frequencyString = frequency.toStorageString()
    val parts = frequencyString.split(":", limit = 2)
    val frequencyType = parts[0]
    val frequencyData = parts.getOrNull(1)

    return HabitInsertParams(
        id = id,
        name = name,
        icon = icon,
        color = color,
        frequencyType = frequencyType,
        frequencyData = frequencyData,
        habitType = habitType.name,
        targetValue = targetValue,
        targetUnit = targetUnit,
        reminderTime = reminderTime,
        notes = notes,
        sortOrder = sortOrder.toLong(),
        createdAt = createdAt,
        isArchived = 0L // Always 0 for new/updated habits
    )
}

/**
 * Data class to hold habit insert parameters.
 * This avoids passing many individual parameters to SQL queries.
 */
data class HabitInsertParams(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val frequencyType: String,
    val frequencyData: String?,
    val habitType: String,
    val targetValue: Int?,
    val targetUnit: String?,
    val reminderTime: String?,
    val notes: String?,
    val sortOrder: Long,
    val createdAt: Long,
    val isArchived: Long
)
