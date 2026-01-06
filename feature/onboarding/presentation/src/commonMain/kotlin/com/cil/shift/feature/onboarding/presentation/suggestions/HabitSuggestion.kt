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
    val description: String,
    val isNegative: Boolean = false,  // For NEGATIVE/reduce type habits
    val quitStartDate: Long? = null   // For QUIT type habits (null means "start today")
)

enum class SuggestionCategory(val displayName: String, val emoji: String, val colorHex: String) {
    HEALTH("Health", "💪", "#9FE7DD"),
    PRODUCTIVITY("Productivity", "⚡", "#4E7CFF"),
    MINDFULNESS("Mindfulness", "🧘", "#E0BBE4"),
    LEARNING("Learning", "📚", "#FAE7B5"),
    LIFESTYLE("Lifestyle", "🌟", "#FFB88C"),
    QUIT("Quit", "🚭", "#FF6B6B"),       // Red/orange for quit habits
    REDUCE("Reduce", "📉", "#FFB347"),   // Amber for reduce/limit habits
    CHORES("Chores", "🏠", "#87CEEB")    // Light blue for chores
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
        habitType = HabitType.TIMER,
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
        habitType = HabitType.TIMER,
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
    ),

    // Quit - Sobriety counter habits
    HabitSuggestion(
        id = "quit_smoking",
        name = "Smoking",
        icon = "cigarette",
        color = "#FF6B6B",
        category = SuggestionCategory.QUIT,
        habitType = HabitType.QUIT,
        targetValue = null,
        targetUnit = null,
        description = "Track your smoke-free journey"
    ),
    HabitSuggestion(
        id = "quit_alcohol",
        name = "Alcohol",
        icon = "wine",
        color = "#FF8E72",
        category = SuggestionCategory.QUIT,
        habitType = HabitType.QUIT,
        targetValue = null,
        targetUnit = null,
        description = "Track your sobriety journey"
    ),
    HabitSuggestion(
        id = "quit_social_media",
        name = "Social Media",
        icon = "phone",
        color = "#FF7F50",
        category = SuggestionCategory.QUIT,
        habitType = HabitType.QUIT,
        targetValue = null,
        targetUnit = null,
        description = "Break free from social media addiction"
    ),
    HabitSuggestion(
        id = "quit_junk_food",
        name = "Junk Food",
        icon = "utensils",
        color = "#FF6347",
        category = SuggestionCategory.QUIT,
        habitType = HabitType.QUIT,
        targetValue = null,
        targetUnit = null,
        description = "Stop eating junk food"
    ),
    HabitSuggestion(
        id = "quit_caffeine",
        name = "Caffeine",
        icon = "coffee",
        color = "#E74C3C",
        category = SuggestionCategory.QUIT,
        habitType = HabitType.QUIT,
        targetValue = null,
        targetUnit = null,
        description = "Live caffeine-free"
    ),

    // Reduce - "Less than X" habits
    HabitSuggestion(
        id = "less_coffee",
        name = "Less Coffee",
        icon = "coffee",
        color = "#FFB347",
        category = SuggestionCategory.REDUCE,
        habitType = HabitType.NEGATIVE,
        targetValue = 2,
        targetUnit = "cups",
        description = "Limit coffee intake",
        isNegative = true
    ),
    HabitSuggestion(
        id = "less_sugar",
        name = "Less Sugar",
        icon = "utensils",
        color = "#FFD700",
        category = SuggestionCategory.REDUCE,
        habitType = HabitType.NEGATIVE,
        targetValue = 25,
        targetUnit = "g",
        description = "Reduce daily sugar consumption",
        isNegative = true
    ),
    HabitSuggestion(
        id = "less_screen_time",
        name = "Less Screen Time",
        icon = "phone",
        color = "#FFA500",
        category = SuggestionCategory.REDUCE,
        habitType = HabitType.NEGATIVE,
        targetValue = 2,
        targetUnit = "hours",
        description = "Limit phone/screen usage",
        isNegative = true
    ),
    HabitSuggestion(
        id = "less_snacking",
        name = "Less Snacking",
        icon = "utensils",
        color = "#FFCC80",
        category = SuggestionCategory.REDUCE,
        habitType = HabitType.NEGATIVE,
        targetValue = 2,
        targetUnit = "snacks",
        description = "Reduce unhealthy snacking",
        isNegative = true
    ),
    HabitSuggestion(
        id = "less_soda",
        name = "Less Soda",
        icon = "water",
        color = "#FF9800",
        category = SuggestionCategory.REDUCE,
        habitType = HabitType.NEGATIVE,
        targetValue = 1,
        targetUnit = "cans",
        description = "Cut down on sugary drinks",
        isNegative = true
    ),
    HabitSuggestion(
        id = "less_smoking",
        name = "Fewer Cigarettes",
        icon = "cigarette",
        color = "#FFAB91",
        category = SuggestionCategory.REDUCE,
        habitType = HabitType.NEGATIVE,
        targetValue = 5,
        targetUnit = "cigarettes",
        description = "Gradually reduce smoking",
        isNegative = true
    ),

    // Chores - Household tasks
    HabitSuggestion(
        id = "do_dishes",
        name = "Wash Dishes",
        icon = "tools",
        color = "#87CEEB",
        category = SuggestionCategory.CHORES,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Daily",
        description = "Keep the kitchen clean"
    ),
    HabitSuggestion(
        id = "do_laundry",
        name = "Do Laundry",
        icon = "tools",
        color = "#ADD8E6",
        category = SuggestionCategory.CHORES,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Weekly",
        description = "Wash and fold clothes"
    ),
    HabitSuggestion(
        id = "vacuum_clean",
        name = "Vacuum",
        icon = "tools",
        color = "#B0C4DE",
        category = SuggestionCategory.CHORES,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Weekly",
        description = "Keep floors clean"
    ),
    HabitSuggestion(
        id = "water_plants",
        name = "Water Plants",
        icon = "leaf",
        color = "#90EE90",
        category = SuggestionCategory.CHORES,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Every 2 days",
        description = "Keep your plants healthy"
    ),
    HabitSuggestion(
        id = "take_out_trash",
        name = "Take Out Trash",
        icon = "tools",
        color = "#A9A9A9",
        category = SuggestionCategory.CHORES,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Daily",
        description = "Empty the trash bins"
    ),
    HabitSuggestion(
        id = "make_bed",
        name = "Make Bed",
        icon = "moon",
        color = "#DDA0DD",
        category = SuggestionCategory.CHORES,
        habitType = HabitType.SIMPLE,
        targetValue = null,
        targetUnit = "Morning",
        description = "Start the day with a tidy room"
    ),
    HabitSuggestion(
        id = "clean_room",
        name = "Clean Room",
        icon = "tools",
        color = "#98FB98",
        category = SuggestionCategory.CHORES,
        habitType = HabitType.TIMER,
        targetValue = 15,
        targetUnit = "min",
        description = "Tidy up your space"
    )
)
