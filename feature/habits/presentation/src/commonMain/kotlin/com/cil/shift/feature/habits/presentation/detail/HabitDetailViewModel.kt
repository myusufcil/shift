package com.cil.shift.feature.habits.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cil.shift.core.common.currentDate
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import com.cil.shift.feature.habits.domain.usecase.DeleteHabitUseCase
import com.cil.shift.feature.habits.domain.usecase.ToggleHabitCompletionUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HabitDetailViewModel(
    private val habitId: String,
    private val selectedDate: String? = null, // Optional selected date, defaults to today
    private val repository: HabitRepository,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HabitDetailState())
    val state: StateFlow<HabitDetailState> = _state.asStateFlow()

    // Use selected date or today
    val targetDate: String = selectedDate ?: currentDate().toString()

    init {
        loadHabitDetails()
    }

    private fun loadHabitDetails() {
        viewModelScope.launch {
            try {
                val habit = repository.getHabitById(habitId)
                if (habit == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Habit not found"
                    )}
                    return@launch
                }

                repository.getCompletions(habitId)
                    .catch { e ->
                        _state.update { it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load completions"
                        )}
                    }
                    .collect { completions ->
                        val todayCompletion = completions.firstOrNull { it.date == targetDate }
                        val stats = calculateStats(completions)
                        _state.update { it.copy(
                            habit = habit,
                            completions = completions,
                            todayCompletion = todayCompletion,
                            currentStreak = stats.currentStreak,
                            bestStreak = stats.bestStreak,
                            completionRate = stats.completionRate,
                            selectedDateString = targetDate,
                            isLoading = false,
                            error = null
                        )}
                    }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )}
            }
        }
    }

    fun onEvent(event: HabitDetailEvent) {
        when (event) {
            is HabitDetailEvent.ShowDeleteDialog -> {
                _state.update { it.copy(showDeleteDialog = true) }
            }
            is HabitDetailEvent.DismissDeleteDialog -> {
                _state.update { it.copy(showDeleteDialog = false) }
            }
            is HabitDetailEvent.DeleteHabit -> {
                viewModelScope.launch {
                    deleteHabitUseCase(habitId)
                    _state.update { it.copy(
                        showDeleteDialog = false,
                        isDeleted = true
                    )}
                }
            }
            is HabitDetailEvent.ToggleCompletion -> {
                viewModelScope.launch {
                    toggleHabitCompletionUseCase(habitId, targetDate)
                }
            }
            is HabitDetailEvent.IncrementValue -> {
                viewModelScope.launch {
                    val currentCompletion = _state.value.todayCompletion
                    val targetValue = _state.value.habit?.targetValue ?: Int.MAX_VALUE
                    val newValue = ((currentCompletion?.currentValue ?: 0) + event.amount).coerceAtMost(targetValue)

                    // Optimistic update
                    _state.update { currentState ->
                        currentState.copy(
                            todayCompletion = currentCompletion?.copy(currentValue = newValue)
                                ?: com.cil.shift.feature.habits.domain.model.HabitCompletion(
                                    habitId = habitId,
                                    date = targetDate,
                                    isCompleted = newValue >= targetValue,
                                    currentValue = newValue
                                )
                        )
                    }

                    repository.updateCurrentValue(habitId, targetDate, newValue)
                }
            }
            is HabitDetailEvent.DecrementValue -> {
                viewModelScope.launch {
                    val currentCompletion = _state.value.todayCompletion
                    val targetValue = _state.value.habit?.targetValue ?: Int.MAX_VALUE
                    val newValue = maxOf(0, (currentCompletion?.currentValue ?: 0) - event.amount)

                    // Optimistic update
                    _state.update { currentState ->
                        currentState.copy(
                            todayCompletion = currentCompletion?.copy(currentValue = newValue)
                                ?: com.cil.shift.feature.habits.domain.model.HabitCompletion(
                                    habitId = habitId,
                                    date = targetDate,
                                    isCompleted = newValue >= targetValue,
                                    currentValue = newValue
                                )
                        )
                    }

                    repository.updateCurrentValue(habitId, targetDate, newValue)
                }
            }
            is HabitDetailEvent.StartEditingName -> {
                _state.update {
                    it.copy(
                        isEditingName = true,
                        editedName = it.habit?.name ?: ""
                    )
                }
            }
            is HabitDetailEvent.CancelEditingName -> {
                _state.update {
                    it.copy(
                        isEditingName = false,
                        editedName = ""
                    )
                }
            }
            is HabitDetailEvent.UpdateEditedName -> {
                _state.update {
                    it.copy(editedName = event.name)
                }
            }
            is HabitDetailEvent.SaveEditedName -> {
                viewModelScope.launch {
                    val currentHabit = _state.value.habit
                    val newName = _state.value.editedName.trim()

                    if (currentHabit != null && newName.isNotEmpty()) {
                        val updatedHabit = currentHabit.copy(name = newName)
                        repository.updateHabit(updatedHabit)

                        _state.update {
                            it.copy(
                                habit = updatedHabit,
                                isEditingName = false,
                                editedName = ""
                            )
                        }
                    } else {
                        // Cancel if name is empty
                        _state.update {
                            it.copy(
                                isEditingName = false,
                                editedName = ""
                            )
                        }
                    }
                }
            }
            is HabitDetailEvent.StartEditingNote -> {
                _state.update {
                    it.copy(
                        isEditingNote = true,
                        editedNote = it.todayCompletion?.note ?: ""
                    )
                }
            }
            is HabitDetailEvent.CancelEditingNote -> {
                _state.update {
                    it.copy(
                        isEditingNote = false,
                        editedNote = ""
                    )
                }
            }
            is HabitDetailEvent.UpdateEditedNote -> {
                _state.update {
                    it.copy(editedNote = event.note)
                }
            }
            is HabitDetailEvent.SaveEditedNote -> {
                viewModelScope.launch {
                    val newNote = _state.value.editedNote.trim()

                    // Save note (can be empty to clear it)
                    repository.updateCompletionNote(
                        habitId = habitId,
                        date = targetDate,
                        note = if (newNote.isEmpty()) null else newNote
                    )

                    // Update local state optimistically
                    _state.update { currentState ->
                        currentState.copy(
                            todayCompletion = currentState.todayCompletion?.copy(
                                note = if (newNote.isEmpty()) null else newNote
                            ),
                            isEditingNote = false,
                            editedNote = ""
                        )
                    }
                }
            }
        }
    }

    private fun calculateStats(
        completions: List<com.cil.shift.feature.habits.domain.model.HabitCompletion>
    ): HabitStats {
        if (completions.isEmpty()) {
            return HabitStats(0, 0, 0f)
        }

        // Filter only completed entries
        val completed = completions.filter { it.isCompleted }

        if (completed.isEmpty()) {
            return HabitStats(0, 0, 0f)
        }

        // Sort by date descending
        val sortedCompletions = completed.sortedByDescending { it.date }

        // Calculate current streak
        var currentStreak = 0
        var bestStreak = 0
        var tempStreak = 0

        try {
            val dates = sortedCompletions.map {
                kotlinx.datetime.LocalDate.parse(it.date)
            }.sortedDescending()

            val today = currentDate()

            // Check if today or yesterday is completed
            if (dates.isNotEmpty()) {
                val daysSinceLatest = today.toEpochDays() - dates.first().toEpochDays()

                if (daysSinceLatest <= 1) {
                    currentStreak = 1
                    tempStreak = 1

                    // Count consecutive days
                    for (i in 1 until dates.size) {
                        val diff = (dates[i - 1].toEpochDays() - dates[i].toEpochDays()).toInt()
                        if (diff == 1) {
                            currentStreak++
                            tempStreak++
                        } else {
                            tempStreak = 1
                        }
                        bestStreak = maxOf(bestStreak, tempStreak)
                    }
                }
            }

            bestStreak = maxOf(bestStreak, currentStreak)
        } catch (e: Exception) {
            // Fallback if date parsing fails
            currentStreak = 0
            bestStreak = 0
        }

        // Calculate completion rate (completed days / total days since creation)
        val completionRate = if (completions.isNotEmpty()) {
            val totalDays = completions.size
            val completedDays = completed.size
            (completedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        return HabitStats(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            completionRate = completionRate
        )
    }
}

private data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val completionRate: Float
)

sealed interface HabitDetailEvent {
    data object ShowDeleteDialog : HabitDetailEvent
    data object DismissDeleteDialog : HabitDetailEvent
    data object DeleteHabit : HabitDetailEvent
    data object ToggleCompletion : HabitDetailEvent
    data class IncrementValue(val amount: Int) : HabitDetailEvent
    data class DecrementValue(val amount: Int) : HabitDetailEvent
    data object StartEditingName : HabitDetailEvent
    data object CancelEditingName : HabitDetailEvent
    data class UpdateEditedName(val name: String) : HabitDetailEvent
    data object SaveEditedName : HabitDetailEvent
    data object StartEditingNote : HabitDetailEvent
    data object CancelEditingNote : HabitDetailEvent
    data class UpdateEditedNote(val note: String) : HabitDetailEvent
    data object SaveEditedNote : HabitDetailEvent
}
