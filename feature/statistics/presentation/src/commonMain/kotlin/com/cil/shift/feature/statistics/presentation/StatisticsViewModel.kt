package com.cil.shift.feature.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class StatisticsViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        val today = com.cil.shift.core.common.currentDate()
        _state.update { it.copy(
            selectedWeekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY),
            selectedMonthStart = LocalDate(today.year, today.month, 1)
        )}
        loadStatistics()
    }

    fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.PreviousWeek -> {
                val currentWeek = _state.value.selectedWeekStart ?: com.cil.shift.core.common.currentDate()
                _state.update { it.copy(
                    selectedWeekStart = currentWeek.minus(7, DateTimeUnit.DAY)
                )}
                loadStatistics()
            }
            is StatisticsEvent.NextWeek -> {
                val currentWeek = _state.value.selectedWeekStart ?: com.cil.shift.core.common.currentDate()
                val nextWeek = currentWeek.plus(7, DateTimeUnit.DAY)
                val today = com.cil.shift.core.common.currentDate()
                val todayWeekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
                // Only allow navigation if next week is before current week
                if (nextWeek < todayWeekStart) {
                    _state.update { it.copy(
                        selectedWeekStart = nextWeek
                    )}
                    loadStatistics()
                }
            }
            is StatisticsEvent.PreviousMonth -> {
                val currentMonth = _state.value.selectedMonthStart ?: com.cil.shift.core.common.currentDate()
                val prevMonth = currentMonth.minus(1, DateTimeUnit.MONTH)
                _state.update { it.copy(
                    selectedMonthStart = LocalDate(prevMonth.year, prevMonth.month, 1)
                )}
                loadStatistics()
            }
            is StatisticsEvent.NextMonth -> {
                val currentMonth = _state.value.selectedMonthStart ?: com.cil.shift.core.common.currentDate()
                val nextMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
                val today = com.cil.shift.core.common.currentDate()
                val todayMonthStart = LocalDate(today.year, today.month, 1)
                // Only allow navigation if next month is before current month
                if (LocalDate(nextMonth.year, nextMonth.month, 1) < todayMonthStart) {
                    _state.update { it.copy(
                        selectedMonthStart = LocalDate(nextMonth.year, nextMonth.month, 1)
                    )}
                    loadStatistics()
                }
            }
            is StatisticsEvent.ChangeWeeklyChartType -> {
                _state.update { it.copy(weeklyChartType = event.chartType) }
            }
            is StatisticsEvent.ChangeMonthlyChartType -> {
                _state.update { it.copy(monthlyChartType = event.chartType) }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            habitRepository.getHabits().collect { habits ->
                val today = com.cil.shift.core.common.currentDate().toString()

                // Calculate today's completion
                var completedToday = 0
                habits.forEach { habit ->
                    val completion = habitRepository.getCompletion(habit.id, today)
                    if (completion?.isCompleted == true) {
                        completedToday++
                    }
                }

                val totalHabits = habits.size
                val completionRate = if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f

                // Calculate weekly data (last 7 days)
                val weeklyData = calculateWeeklyData(habits)

                // Calculate monthly data (last 30 days)
                val monthlyData = calculateMonthlyData(habits)

                // Calculate current streak (simplified)
                val currentStreak = calculateCurrentStreak(habits)

                _state.update { currentState ->
                    currentState.copy(
                        completionRate = completionRate,
                        totalHabits = totalHabits,
                        completedToday = completedToday,
                        currentStreak = currentStreak,
                        weeklyData = weeklyData,
                        monthlyData = monthlyData,
                        weeklyChartType = currentState.weeklyChartType,
                        monthlyChartType = currentState.monthlyChartType,
                        selectedWeekStart = currentState.selectedWeekStart,
                        selectedMonthStart = currentState.selectedMonthStart,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun calculateWeeklyData(habits: List<com.cil.shift.feature.habits.domain.model.Habit>): List<DayCompletion> {
        val weekStart = _state.value.selectedWeekStart ?: com.cil.shift.core.common.currentDate()
        val weekData = mutableListOf<DayCompletion>()

        for (i in 0 until 7) {
            val date = weekStart.plus(i, DateTimeUnit.DAY)
            val dateString = date.toString()
            val dayName = date.dayOfWeek.name.take(3)

            var completedCount = 0
            habits.forEach { habit ->
                val completion = habitRepository.getCompletion(habit.id, dateString)
                if (completion?.isCompleted == true) {
                    completedCount++
                }
            }

            val rate = if (habits.isNotEmpty()) completedCount.toFloat() / habits.size else 0f
            weekData.add(DayCompletion(dayName, rate))
        }

        return weekData
    }

    private suspend fun calculateMonthlyData(habits: List<com.cil.shift.feature.habits.domain.model.Habit>): List<DayCompletion> {
        val monthStart = _state.value.selectedMonthStart ?: com.cil.shift.core.common.currentDate()
        val monthData = mutableListOf<DayCompletion>()

        // Get number of days in the selected month
        val daysInMonth = monthStart.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth

        for (day in 1..daysInMonth) {
            val date = LocalDate(monthStart.year, monthStart.month, day)
            val dateString = date.toString()
            // Use day number (1-28/30/31 depending on month)
            val dayLabel = "$day"

            var completedCount = 0
            habits.forEach { habit ->
                val completion = habitRepository.getCompletion(habit.id, dateString)
                if (completion?.isCompleted == true) {
                    completedCount++
                }
            }

            val rate = if (habits.isNotEmpty()) completedCount.toFloat() / habits.size else 0f
            monthData.add(DayCompletion(dayLabel, rate))
        }

        return monthData
    }

    private suspend fun calculateCurrentStreak(habits: List<com.cil.shift.feature.habits.domain.model.Habit>): Int {
        if (habits.isEmpty()) return 0

        val today = com.cil.shift.core.common.currentDate()
        var streak = 0
        var currentDate = today

        // Check consecutive days where at least one habit was completed
        while (true) {
            val dateString = currentDate.toString()
            var anyCompleted = false

            habits.forEach { habit ->
                val completion = habitRepository.getCompletion(habit.id, dateString)
                if (completion?.isCompleted == true) {
                    anyCompleted = true
                    return@forEach
                }
            }

            if (anyCompleted) {
                streak++
                currentDate = currentDate.minus(1, DateTimeUnit.DAY)
            } else {
                break
            }

            // Limit to prevent infinite loop
            if (streak > 365) break
        }

        return streak
    }
}

sealed interface StatisticsEvent {
    data object PreviousWeek : StatisticsEvent
    data object NextWeek : StatisticsEvent
    data object PreviousMonth : StatisticsEvent
    data object NextMonth : StatisticsEvent
    data class ChangeWeeklyChartType(val chartType: ChartType) : StatisticsEvent
    data class ChangeMonthlyChartType(val chartType: ChartType) : StatisticsEvent
}
