package com.cil.shift.feature.habits.presentation.home

import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.model.HabitCompletion
import com.cil.shift.feature.habits.domain.model.HabitSchedule
import kotlinx.datetime.LocalDate

enum class WeeklyChartType {
    LINE,
    BAR,
    PIE
}

data class HomeState(
    val habits: List<HabitWithCompletion> = emptyList(),
    val scheduledEvents: List<HabitSchedule> = emptyList(),
    val userName: String = "Alex",
    val currentDate: String = "",
    val selectedDate: LocalDate? = null,
    val selectedDateCompletions: Map<String, HabitCompletion> = emptyMap(),
    val showDayProgress: Boolean = false,
    val showAllHabits: Boolean = false,
    val weeklyChartData: List<Pair<String, Float>> = emptyList(),
    val weeklyChartType: WeeklyChartType = WeeklyChartType.LINE,
    val selectedDayIndex: Int? = null, // Today's index in weekly chart (0=Monday, 6=Sunday)
    val runningTimers: Set<String> = emptySet(), // Set of habit IDs with running timers
    val timerStartTimes: Map<String, Long> = emptyMap(), // habitId -> timestamp when timer started
    val isLoading: Boolean = true,
    val error: String? = null,
    val confettiShownForDate: String? = null, // Track which date confetti was shown to prevent repeating
    // Honey system
    val lastHoneyEarned: Int = 0, // Amount of honey earned (triggers popup when > 0)
    val lastHoneyReason: String = "" // Reason for earning honey
)

data class HabitWithCompletion(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentValue: Int = 0, // For measurable/timer habits
    val currentStreak: Int = 0 // Consecutive days completed
) {
    val isCompleted: Boolean get() = isCompletedToday
}
