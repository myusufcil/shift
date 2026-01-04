package com.cil.shift.feature.onboarding.presentation.suggestions

import com.cil.shift.feature.habits.domain.model.Frequency
import com.cil.shift.feature.habits.domain.model.HabitType

data class HabitSuggestion(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val category: SuggestionCategory,
    val habitType: HabitType,
    val targetValue: Int?,
    val targetUnit: String?,
    val description: String
)

enum class SuggestionCategory(val displayName: String, val emoji: String) {
    HEALTH("Health", "💪"),
    PRODUCTIVITY("Productivity", "⚡"),
    MINDFULNESS("Mindfulness", "🧘"),
    LEARNING("Learning", "📚"),
    LIFESTYLE("Lifestyle", "🌟")
}

val habitSuggestions = listOf(
    // Health
    HabitSuggestion(
        id = "drink_water",
        name = "Drink Water",
        icon = "water",
        color = "#9FE7DD",
        category = SuggestionCategory.HEALTH,
        habitType = HabitType.MEASURABLE,
        targetValue = 2000,
        targetUnit = "ml",
        description = "Stay hydrated throughout the day"
    ),
    HabitSuggestion(
        id = "morning_exercise",
        name = "Morning Exercise",
        icon = "run",
        color = "#FFB88C",
        category = SuggestionCategory.HEALTH,
        habitType = HabitType.TIMER,
        targetValue = 30,
        targetUnit = "min",
        description = "Start your day with physical activity"
    ),
    HabitSuggestion(
        id = "workout_session",
        name = "Workout Session",
        icon = "dumbbell",
        color = "#FF9E9E",
        category = SuggestionCategory.HEALTH,
        habitType = HabitType.SESSION,
        targetValue = 45,
        targetUnit = "min",
        description = "Complete a workout session"
    ),
    HabitSuggestion(
        id = "healthy_breakfast",
        name = "Eat Healthy Breakfast",
        icon = "utensils",
        color = "#FFE5A3",
        category = SuggestionCategory.HEALTH,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Daily",
        description = "Start your day with a nutritious meal"
    ),

    // Productivity
    HabitSuggestion(
        id = "deep_work",
        name = "Deep Work",
        icon = "code",
        color = "#4E7CFF",
        category = SuggestionCategory.PRODUCTIVITY,
        habitType = HabitType.TIMER,
        targetValue = 120,
        targetUnit = "min",
        description = "Focus on your most important work"
    ),
    HabitSuggestion(
        id = "plan_tomorrow",
        name = "Plan Tomorrow",
        icon = "briefcase",
        color = "#B5B9FF",
        category = SuggestionCategory.PRODUCTIVITY,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Evening",
        description = "Prepare for the next day"
    ),
    HabitSuggestion(
        id = "inbox_zero",
        name = "Clear Inbox",
        icon = "tools",
        color = "#AED9E0",
        category = SuggestionCategory.PRODUCTIVITY,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Daily",
        description = "Process all emails"
    ),

    // Mindfulness
    HabitSuggestion(
        id = "meditation",
        name = "Meditation",
        icon = "meditation",
        color = "#E0BBE4",
        category = SuggestionCategory.MINDFULNESS,
        habitType = HabitType.SESSION,
        targetValue = 15,
        targetUnit = "min",
        description = "Practice mindfulness meditation"
    ),
    HabitSuggestion(
        id = "gratitude_journal",
        name = "Gratitude Journal",
        icon = "heart",
        color = "#FFDFD3",
        category = SuggestionCategory.MINDFULNESS,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Evening",
        description = "Write down things you're grateful for"
    ),
    HabitSuggestion(
        id = "evening_walk",
        name = "Evening Walk",
        icon = "leaf",
        color = "#C1E1C1",
        category = SuggestionCategory.MINDFULNESS,
        habitType = HabitType.TIMER,
        targetValue = 20,
        targetUnit = "min",
        description = "Take a relaxing evening walk"
    ),

    // Learning
    HabitSuggestion(
        id = "read_book",
        name = "Read Book",
        icon = "book",
        color = "#FAE7B5",
        category = SuggestionCategory.LEARNING,
        habitType = HabitType.TIMER,
        targetValue = 30,
        targetUnit = "min",
        description = "Read for personal development"
    ),
    HabitSuggestion(
        id = "learn_language",
        name = "Language Practice",
        icon = "brain",
        color = "#D4E7C5",
        category = SuggestionCategory.LEARNING,
        habitType = HabitType.TIMER,
        targetValue = 20,
        targetUnit = "min",
        description = "Practice a new language"
    ),
    HabitSuggestion(
        id = "online_course",
        name = "Online Course",
        icon = "code",
        color = "#9FE7DD",
        category = SuggestionCategory.LEARNING,
        habitType = HabitType.TIMER,
        targetValue = 45,
        targetUnit = "min",
        description = "Continue learning through online courses"
    ),

    // Lifestyle
    HabitSuggestion(
        id = "early_sleep",
        name = "Sleep Before 11 PM",
        icon = "moon",
        color = "#B5B9FF",
        category = SuggestionCategory.LIFESTYLE,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Nightly",
        description = "Maintain a healthy sleep schedule"
    ),
    HabitSuggestion(
        id = "creative_hobby",
        name = "Creative Hobby",
        icon = "palette",
        color = "#FFB88C",
        category = SuggestionCategory.LIFESTYLE,
        habitType = HabitType.TIMER,
        targetValue = 30,
        targetUnit = "min",
        description = "Spend time on creative activities"
    ),
    HabitSuggestion(
        id = "morning_coffee",
        name = "Morning Coffee Ritual",
        icon = "coffee",
        color = "#FFDFD3",
        category = SuggestionCategory.LIFESTYLE,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Morning",
        description = "Enjoy your morning coffee mindfully"
    )
)
