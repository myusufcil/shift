package com.cil.shift.feature.onboarding.presentation.suggestions

data class HabitSuggestionsState(
    val selectedCategory: SuggestionCategory = SuggestionCategory.HEALTH,
    val selectedHabits: Set<String> = emptySet(),
    val isCreating: Boolean = false,
    val error: String? = null
)

sealed interface HabitSuggestionsEvent {
    data class CategorySelected(val category: SuggestionCategory) : HabitSuggestionsEvent
    data class HabitToggled(val habitId: String) : HabitSuggestionsEvent
    data object CreateSelectedHabits : HabitSuggestionsEvent
    data object SkipSuggestions : HabitSuggestionsEvent
}
