package com.cil.shift.feature.habits.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cil.shift.feature.habits.domain.usecase.GetHabitsUseCase
import com.cil.shift.feature.habits.domain.usecase.ToggleHabitCompletionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class HomeViewModel(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val habitRepository: com.cil.shift.feature.habits.domain.repository.HabitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadHabits()
        updateCurrentDate()
        loadWeeklyChartData()
    }

    fun refresh() {
        // Preserve selected date when refreshing
        val currentSelectedDate = _state.value.selectedDate
        if (currentSelectedDate != null) {
            val dateString = "${currentSelectedDate.year}-${currentSelectedDate.monthNumber.toString().padStart(2, '0')}-${currentSelectedDate.dayOfMonth.toString().padStart(2, '0')}"
            loadHabits(dateString)
        } else {
            loadHabits()
        }
        loadWeeklyChartData()
    }

    private fun loadHabits(date: String? = null) {
        viewModelScope.launch {
            val targetDate = date ?: getTodayDateString()
            val targetLocalDate = parseDate(targetDate)

            getHabitsUseCase()
                .catch { e ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )}
                }
                .collect { habits ->
                    // Filter habits based on their frequency schedule
                    val scheduledHabits = habits.filter { habit ->
                        shouldShowHabitOnDate(habit, targetLocalDate)
                    }

                    // For each habit, get completion and combine
                    val habitsWithCompletion = scheduledHabits.map { habit ->
                        val completion = habitRepository.getCompletion(habit.id, targetDate)
                        HabitWithCompletion(
                            habit = habit,
                            isCompletedToday = completion?.isCompleted ?: false,
                            currentValue = completion?.currentValue ?: 0
                        )
                    }

                    _state.update { currentState ->
                        currentState.copy(
                            habits = habitsWithCompletion,
                            isLoading = false,
                            error = null,
                            // Update selectedDate if we're loading a specific date
                            selectedDate = if (date != null) targetLocalDate else currentState.selectedDate
                        )
                    }
                }
        }
    }

    private fun shouldShowHabitOnDate(habit: com.cil.shift.feature.habits.domain.model.Habit, date: LocalDate): Boolean {
        return when (val frequency = habit.frequency) {
            is com.cil.shift.feature.habits.domain.model.Frequency.Daily -> true
            is com.cil.shift.feature.habits.domain.model.Frequency.Weekly -> {
                frequency.days.contains(date.dayOfWeek)
            }
            is com.cil.shift.feature.habits.domain.model.Frequency.Custom -> {
                // For custom frequency, check if the date is a valid occurrence
                val habitCreatedDate = LocalDate.fromEpochDays((habit.createdAt / (24 * 60 * 60 * 1000)).toInt())
                val daysSinceCreation = habitCreatedDate.daysUntil(date)
                daysSinceCreation >= 0 && daysSinceCreation % frequency.daysInterval == 0
            }
        }
    }

    private fun parseDate(dateString: String): LocalDate {
        val parts = dateString.split("-")
        return LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }

    private fun updateCurrentDate() {
        val today = com.cil.shift.core.common.currentDateTime()
        val formattedDate = "${today.dayOfWeek.name.lowercase().capitalize()}, ${today.dayOfMonth} ${today.month.name.lowercase().capitalize()}"
        _state.update { it.copy(currentDate = formattedDate) }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.ToggleHabit -> {
                viewModelScope.launch {
                    // Use selected date if available, otherwise use today
                    val targetDate = _state.value.selectedDate?.toString() ?: getTodayDateString()
                    toggleHabitCompletionUseCase(event.habitId, targetDate)

                    // Update UI optimistically
                    _state.update { currentState ->
                        val updatedHabits = currentState.habits.map { habitWithCompletion ->
                            if (habitWithCompletion.habit.id == event.habitId) {
                                habitWithCompletion.copy(
                                    isCompletedToday = !habitWithCompletion.isCompletedToday
                                )
                            } else {
                                habitWithCompletion
                            }
                        }
                        currentState.copy(habits = updatedHabits)
                    }

                    // Refresh weekly chart data
                    loadWeeklyChartData()
                }
            }
            is HomeEvent.IncrementHabit -> {
                viewModelScope.launch {
                    // Use selected date if available, otherwise use today
                    val targetDate = _state.value.selectedDate?.toString() ?: getTodayDateString()
                    val habitWithCompletion = _state.value.habits.find { it.habit.id == event.habitId }
                    if (habitWithCompletion != null) {
                        val newValue = habitWithCompletion.currentValue + event.amount
                        val targetValue = habitWithCompletion.habit.targetValue ?: Int.MAX_VALUE
                        val finalValue = newValue.coerceAtMost(targetValue)

                        // Save to database
                        habitRepository.updateCurrentValue(event.habitId, targetDate, finalValue)

                        // Update UI optimistically
                        _state.update { currentState ->
                            val updatedHabits = currentState.habits.map { habit ->
                                if (habit.habit.id == event.habitId) {
                                    habit.copy(currentValue = finalValue)
                                } else {
                                    habit
                                }
                            }
                            currentState.copy(habits = updatedHabits)
                        }

                        // Refresh weekly chart data
                        loadWeeklyChartData()
                    }
                }
            }
            is HomeEvent.DecrementHabit -> {
                viewModelScope.launch {
                    // Use selected date if available, otherwise use today
                    val targetDate = _state.value.selectedDate?.toString() ?: getTodayDateString()
                    val habitWithCompletion = _state.value.habits.find { it.habit.id == event.habitId }
                    if (habitWithCompletion != null) {
                        val newValue = (habitWithCompletion.currentValue - event.amount).coerceAtLeast(0)

                        // Save to database
                        habitRepository.updateCurrentValue(event.habitId, targetDate, newValue)

                        // Update UI optimistically
                        _state.update { currentState ->
                            val updatedHabits = currentState.habits.map { habit ->
                                if (habit.habit.id == event.habitId) {
                                    habit.copy(currentValue = newValue)
                                } else {
                                    habit
                                }
                            }
                            currentState.copy(habits = updatedHabits)
                        }

                        // Refresh weekly chart data
                        loadWeeklyChartData()
                    }
                }
            }
            is HomeEvent.ToggleShowAll -> {
                _state.update { it.copy(showAllHabits = !it.showAllHabits) }
            }
            is HomeEvent.DaySelected -> {
                // Load habits for the selected date
                val dateString = "${event.date.year}-${event.date.monthNumber.toString().padStart(2, '0')}-${event.date.dayOfMonth.toString().padStart(2, '0')}"
                loadHabits(dateString)
                _state.update {
                    it.copy(selectedDate = event.date)
                }
            }
            is HomeEvent.CloseDayProgress -> {
                // When closing, reload today's habits
                loadHabits()
                _state.update {
                    it.copy(
                        selectedDate = null,
                        selectedDateCompletions = emptyMap(),
                        showDayProgress = false
                    )
                }
            }
            is HomeEvent.NavigateToCreateHabit -> {
                // Navigation will be handled by the composable
            }
            is HomeEvent.NavigateToHabitDetail -> {
                // Navigation will be handled by the composable
            }
            is HomeEvent.ReorderHabits -> {
                viewModelScope.launch {
                    // Update sort order in repository
                    val habitOrders = event.habitIds.mapIndexed { index, habitId ->
                        habitId to index
                    }.toMap()
                    habitRepository.updateHabitOrders(habitOrders)

                    // Update local state
                    _state.update { currentState ->
                        val reorderedHabits = event.habitIds.mapNotNull { habitId ->
                            currentState.habits.find { it.habit.id == habitId }
                        }
                        currentState.copy(habits = reorderedHabits)
                    }
                }
            }
            is HomeEvent.ChangeWeeklyChartType -> {
                _state.update { it.copy(weeklyChartType = event.chartType) }
            }
            is HomeEvent.RefreshChartOnly -> {
                // Just reload chart without affecting selected date or habits
                loadWeeklyChartData()
            }
        }
    }

    private fun getTodayDateString(): String {
        val today = com.cil.shift.core.common.currentDateTime()
        return "${today.year}-${today.monthNumber.toString().padStart(2, '0')}-${today.dayOfMonth.toString().padStart(2, '0')}"
    }

    fun loadCompletionsForDate(date: LocalDate) {
        viewModelScope.launch {
            val dateString = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
            val completions = habitRepository.getCompletionsForDate(dateString)

            // Convert list to map with habitId as key
            val completionsMap = completions.associateBy { it.habitId }

            _state.update { it.copy(selectedDateCompletions = completionsMap) }
        }
    }

    private fun loadWeeklyChartData() {
        viewModelScope.launch {
            getHabitsUseCase().collect { habits ->
                val today = com.cil.shift.core.common.currentDate()
                val weekData = mutableListOf<Pair<String, Float>>()

                val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

                for (i in 6 downTo 0) {
                    val date = today.minus(i, DateTimeUnit.DAY)
                    val dateString = date.toString()
                    val dayName = weekDays[date.dayOfWeek.ordinal]

                    var completedCount = 0
                    var totalHabitsForDay = 0

                    habits.forEach { habit ->
                        // Check if habit should be shown on this date
                        if (shouldShowHabitOnDate(habit, date)) {
                            totalHabitsForDay++
                            val completion = habitRepository.getCompletion(habit.id, dateString)
                            if (completion?.isCompleted == true) {
                                completedCount++
                            }
                        }
                    }

                    val rate = if (totalHabitsForDay > 0) completedCount.toFloat() / totalHabitsForDay else 0f
                    weekData.add(dayName to rate)
                }

                _state.update { it.copy(weeklyChartData = weekData) }
            }
        }
    }
}

sealed interface HomeEvent {
    data class ToggleHabit(val habitId: String) : HomeEvent
    data class IncrementHabit(val habitId: String, val amount: Int) : HomeEvent
    data class DecrementHabit(val habitId: String, val amount: Int) : HomeEvent
    data object ToggleShowAll : HomeEvent
    data class DaySelected(val date: LocalDate) : HomeEvent
    data object CloseDayProgress : HomeEvent
    data object NavigateToCreateHabit : HomeEvent
    data class NavigateToHabitDetail(val habitId: String) : HomeEvent
    data class ReorderHabits(val habitIds: List<String>) : HomeEvent
    data class ChangeWeeklyChartType(val chartType: WeeklyChartType) : HomeEvent
    data object RefreshChartOnly : HomeEvent
}
