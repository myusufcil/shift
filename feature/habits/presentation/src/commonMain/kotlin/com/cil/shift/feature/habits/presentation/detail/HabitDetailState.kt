package com.cil.shift.feature.habits.presentation.detail

import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.model.HabitCompletion

data class HabitDetailState(
    val habit: Habit? = null,
    val completions: List<HabitCompletion> = emptyList(),
    val todayCompletion: HabitCompletion? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completionRate: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleted: Boolean = false,
    val isEditingName: Boolean = false,
    val editedName: String = "",
    val isEditingNote: Boolean = false,
    val editedNote: String = "",
    val selectedDateString: String? = null
)
