package com.cil.shift.feature.statistics.presentation

import kotlinx.datetime.LocalDate

enum class ChartType {
    LINE,
    BAR,
    PIE
}

data class StatisticsState(
    val userName: String = "",
    val completionRate: Float = 0f,
    val totalHabits: Int = 0,
    val completedToday: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completedDates: Set<LocalDate> = emptySet(),
    val weeklyData: List<DayCompletion> = emptyList(),
    val monthlyData: List<DayCompletion> = emptyList(),
    val selectedWeekStart: LocalDate? = null,
    val selectedMonthStart: LocalDate? = null,
    val selectedStreakMonth: LocalDate? = null,
    val weeklyChartType: ChartType = ChartType.BAR,
    val monthlyChartType: ChartType = ChartType.LINE,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class DayCompletion(
    val dayName: String,
    val completionRate: Float
)
