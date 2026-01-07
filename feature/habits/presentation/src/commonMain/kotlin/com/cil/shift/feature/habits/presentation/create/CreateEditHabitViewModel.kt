package com.cil.shift.feature.habits.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cil.shift.core.common.Result
import com.cil.shift.core.common.achievement.AchievementManager
import com.cil.shift.core.common.currentTimestamp
import com.cil.shift.core.common.honey.HoneyCheckResult
import com.cil.shift.core.common.honey.HoneyFeature
import com.cil.shift.core.common.honey.HoneyManager
import com.cil.shift.feature.habits.domain.model.Frequency
import com.cil.shift.feature.habits.domain.model.Habit
import com.cil.shift.feature.habits.domain.model.HabitType
import com.cil.shift.feature.habits.domain.usecase.CreateHabitUseCase
import com.cil.shift.feature.onboarding.presentation.suggestions.HabitSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek

class CreateEditHabitViewModel(
    private val habitId: String? = null,
    private val createHabitUseCase: CreateHabitUseCase,
    private val habitRepository: com.cil.shift.feature.habits.domain.repository.HabitRepository? = null,
    private val achievementManager: AchievementManager? = null,
    private val honeyManager: HoneyManager? = null
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEditHabitState(habitId = habitId))
    val state: StateFlow<CreateEditHabitState> = _state.asStateFlow()

    init {
        // Load habit if editing
        if (habitId != null && habitRepository != null) {
            loadHabit(habitId)
        }

        // Load honey status
        loadHoneyRequirements()
    }

    private fun loadHoneyRequirements() {
        viewModelScope.launch {
            // If editing an existing habit, no honey check needed
            if (habitId != null) {
                _state.update { it.copy(honeyRequired = null, isPremium = honeyManager?.isPremium() ?: false) }
                return@launch
            }

            // Get current habit count
            val habitCount = habitRepository?.getHabitCount() ?: 0

            // Check honey requirements
            val isPremium = honeyManager?.isPremium() ?: false
            val checkResult = honeyManager?.checkHabitCreation(habitCount) ?: HoneyCheckResult.Free

            val honeyRequired = when (checkResult) {
                is HoneyCheckResult.Free -> null
                is HoneyCheckResult.PremiumUser -> null
                is HoneyCheckResult.AlreadyUnlocked -> null
                is HoneyCheckResult.CanAfford -> checkResult.feature.cost
                is HoneyCheckResult.NotEnoughHoney -> checkResult.feature.cost
            }

            val honeyBalance = honeyManager?.honeyStatus?.value?.balance ?: 0

            _state.update {
                it.copy(
                    honeyRequired = honeyRequired,
                    honeyBalance = honeyBalance,
                    isPremium = isPremium
                )
            }
        }
    }

    private fun loadHabit(id: String) {
        viewModelScope.launch {
            habitRepository?.getHabitById(id)?.let { habit ->
                _state.update {
                    it.copy(
                        name = habit.name,
                        selectedIcon = habit.icon,
                        selectedColor = habit.color,
                        frequency = habit.frequency,
                        habitType = habit.habitType,
                        targetValue = habit.targetValue,
                        targetUnit = habit.targetUnit,
                        hasReminder = habit.reminderTime != null,
                        reminderTime = habit.reminderTime
                    )
                }
            }
        }
    }

    fun onEvent(event: CreateEditHabitEvent) {
        when (event) {
            is CreateEditHabitEvent.NameChanged -> {
                _state.update { it.copy(name = event.name) }
            }
            is CreateEditHabitEvent.IconSelected -> {
                _state.update { it.copy(selectedIcon = event.icon) }
            }
            is CreateEditHabitEvent.ColorSelected -> {
                _state.update { it.copy(selectedColor = event.color) }
            }
            is CreateEditHabitEvent.FrequencyChanged -> {
                _state.update { it.copy(frequency = event.frequency) }
            }
            is CreateEditHabitEvent.WeekdayToggled -> {
                val currentFrequency = _state.value.frequency
                if (currentFrequency is Frequency.Weekly) {
                    val updatedDays = if (event.day in currentFrequency.days) {
                        currentFrequency.days - event.day
                    } else {
                        currentFrequency.days + event.day
                    }
                    _state.update { it.copy(frequency = Frequency.Weekly(updatedDays)) }
                } else {
                    _state.update { it.copy(frequency = Frequency.Weekly(listOf(event.day))) }
                }
            }
            is CreateEditHabitEvent.HabitTypeSelected -> {
                _state.update { it.copy(habitType = event.habitType) }
            }
            is CreateEditHabitEvent.TargetValueChanged -> {
                _state.update { it.copy(targetValue = event.value) }
            }
            is CreateEditHabitEvent.TargetUnitChanged -> {
                _state.update { it.copy(targetUnit = event.unit) }
            }
            is CreateEditHabitEvent.TimeOfDaySelected -> {
                _state.update { it.copy(timeOfDay = event.timeOfDay) }
            }
            is CreateEditHabitEvent.ReminderToggled -> {
                _state.update { it.copy(hasReminder = event.enabled) }
            }
            is CreateEditHabitEvent.ReminderTimeChanged -> {
                _state.update { it.copy(reminderTime = event.time) }
            }
            is CreateEditHabitEvent.ReminderTimeAdded -> {
                _state.update {
                    it.copy(reminderTimes = it.reminderTimes + event.time)
                }
            }
            is CreateEditHabitEvent.ReminderTimeRemoved -> {
                _state.update {
                    it.copy(reminderTimes = it.reminderTimes - event.time)
                }
            }
            is CreateEditHabitEvent.NotesChanged -> {
                _state.update { it.copy(notes = event.notes) }
            }
            is CreateEditHabitEvent.IsNegativeChanged -> {
                _state.update { it.copy(isNegative = event.isNegative) }
            }
            is CreateEditHabitEvent.QuitStartDateChanged -> {
                _state.update { it.copy(quitStartDate = event.date) }
            }
            is CreateEditHabitEvent.SuggestionSelected -> {
                applySuggestion(event.suggestion)
            }
            is CreateEditHabitEvent.SaveHabit -> {
                saveHabit()
            }
            is CreateEditHabitEvent.DismissNotEnoughHoneyDialog -> {
                _state.update { it.copy(showNotEnoughHoneyDialog = false) }
            }
        }
    }

    private fun applySuggestion(suggestion: HabitSuggestion) {
        _state.update {
            it.copy(
                name = suggestion.name,
                selectedIcon = suggestion.icon,
                selectedColor = suggestion.color,
                habitType = suggestion.habitType,
                targetValue = suggestion.targetValue,
                targetUnit = suggestion.targetUnit,
                isNegative = suggestion.isNegative,
                quitStartDate = if (suggestion.habitType == HabitType.QUIT) currentTimestamp() else null
            )
        }
    }

    private fun saveHabit() {
        val currentState = _state.value

        if (!currentState.isValid) {
            _state.update { it.copy(error = "Habit name is required") }
            return
        }

        // Check honey requirements for new habits
        if (currentState.habitId == null && currentState.honeyRequired != null && !currentState.isPremium) {
            if (!currentState.canAffordHabit) {
                _state.update { it.copy(showNotEnoughHoneyDialog = true) }
                return
            }

            // Spend honey for creating this habit
            val habitCount = honeyManager?.honeyStatus?.value?.balance ?: 0
            val feature = when (currentState.honeyRequired) {
                HoneyFeature.CREATE_HABIT_4TH.cost -> HoneyFeature.CREATE_HABIT_4TH
                HoneyFeature.CREATE_HABIT_5TH.cost -> HoneyFeature.CREATE_HABIT_5TH
                else -> HoneyFeature.CREATE_HABIT_6TH_PLUS
            }
            val spent = honeyManager?.spendHoney(feature) ?: true
            if (!spent) {
                _state.update { it.copy(showNotEnoughHoneyDialog = true) }
                return
            }
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val habit = Habit(
                id = currentState.habitId ?: "",
                name = currentState.name,
                icon = currentState.selectedIcon,
                color = currentState.selectedColor,
                frequency = currentState.frequency,
                habitType = currentState.habitType,
                targetValue = currentState.targetValue,
                targetUnit = currentState.targetUnit,
                reminderTime = if (currentState.hasReminder) currentState.reminderTime else null,
                createdAt = 0L, // Will be set by use case
                isNegative = currentState.isNegative,
                quitStartDate = currentState.quitStartDate
            )

            when (val result = createHabitUseCase(habit)) {
                is Result.Success -> {
                    // Track habit creation achievement only for new habits
                    if (currentState.habitId == null) {
                        achievementManager?.recordHabitCreated()
                    }
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                }
                is Result.Error -> {
                    _state.update { it.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to save habit"
                    )}
                }
                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }
}

sealed interface CreateEditHabitEvent {
    data class NameChanged(val name: String) : CreateEditHabitEvent
    data class IconSelected(val icon: String) : CreateEditHabitEvent
    data class ColorSelected(val color: String) : CreateEditHabitEvent
    data class FrequencyChanged(val frequency: Frequency) : CreateEditHabitEvent
    data class WeekdayToggled(val day: DayOfWeek) : CreateEditHabitEvent
    data class HabitTypeSelected(val habitType: HabitType) : CreateEditHabitEvent
    data class TargetValueChanged(val value: Int?) : CreateEditHabitEvent
    data class TargetUnitChanged(val unit: String) : CreateEditHabitEvent
    data class TimeOfDaySelected(val timeOfDay: TimeOfDay) : CreateEditHabitEvent
    data class ReminderToggled(val enabled: Boolean) : CreateEditHabitEvent
    data class ReminderTimeChanged(val time: String) : CreateEditHabitEvent
    data class ReminderTimeAdded(val time: String) : CreateEditHabitEvent
    data class ReminderTimeRemoved(val time: String) : CreateEditHabitEvent
    data class NotesChanged(val notes: String) : CreateEditHabitEvent
    data class IsNegativeChanged(val isNegative: Boolean) : CreateEditHabitEvent
    data class QuitStartDateChanged(val date: Long?) : CreateEditHabitEvent
    data class SuggestionSelected(val suggestion: HabitSuggestion) : CreateEditHabitEvent
    data object SaveHabit : CreateEditHabitEvent
    data object DismissNotEnoughHoneyDialog : CreateEditHabitEvent
}
