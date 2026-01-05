package com.cil.shift.feature.onboarding.presentation.walkthrough

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WalkthroughViewModel : ViewModel() {
    private val _state = MutableStateFlow(WalkthroughState())
    val state: StateFlow<WalkthroughState> = _state.asStateFlow()

    fun onEvent(event: WalkthroughEvent) {
        when (event) {
            is WalkthroughEvent.PageChanged -> {
                _state.update { it.copy(currentPage = event.page) }
            }
            WalkthroughEvent.NextPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage < _state.value.totalPages - 1) {
                    _state.update { it.copy(currentPage = currentPage + 1) }
                }
            }
            is WalkthroughEvent.AgeSelected -> {
                _state.update { it.copy(selectedAge = event.age) }
            }
            is WalkthroughEvent.FocusAreaToggled -> {
                _state.update { currentState ->
                    val currentAreas = currentState.selectedFocusAreas
                    val newAreas = if (event.area in currentAreas) {
                        currentAreas - event.area
                    } else {
                        currentAreas + event.area
                    }
                    currentState.copy(selectedFocusAreas = newAreas)
                }
            }
            is WalkthroughEvent.UserNameChanged -> {
                _state.update { it.copy(userName = event.name) }
            }
            is WalkthroughEvent.DailyRhythmSelected -> {
                _state.update { it.copy(dailyRhythm = event.rhythm) }
            }
            is WalkthroughEvent.WeeklyGoalSelected -> {
                _state.update { it.copy(weeklyGoal = event.goal) }
            }
            is WalkthroughEvent.StartingHabitCountSelected -> {
                _state.update { it.copy(startingHabitCount = event.count) }
            }
        }
    }
}

sealed interface WalkthroughEvent {
    data class PageChanged(val page: Int) : WalkthroughEvent
    data object NextPage : WalkthroughEvent
    data class AgeSelected(val age: AgeRange) : WalkthroughEvent
    data class FocusAreaToggled(val area: FocusArea) : WalkthroughEvent
    data class UserNameChanged(val name: String) : WalkthroughEvent
    data class DailyRhythmSelected(val rhythm: DailyRhythm) : WalkthroughEvent
    data class WeeklyGoalSelected(val goal: WeeklyGoal) : WalkthroughEvent
    data class StartingHabitCountSelected(val count: StartingHabitCount) : WalkthroughEvent
}
