package com.cil.shift.feature.onboarding.presentation.walkthrough

data class WalkthroughState(
    val currentPage: Int = 0,
    val totalPages: Int = 6,
    // Page 3: Age selection
    val selectedAge: AgeRange? = null,
    // Page 4: Focus areas
    val selectedFocusAreas: Set<FocusArea> = emptySet(),
    // Page 5: User name + Daily rhythm
    val userName: String = "",
    val dailyRhythm: DailyRhythm? = null,
    // Page 6: Weekly goal + Starting habits
    val weeklyGoal: WeeklyGoal? = null,
    val startingHabitCount: StartingHabitCount? = null
) {
    val isLastPage: Boolean
        get() = currentPage == totalPages - 1

    // Next button enabled logic per page
    val isNextEnabled: Boolean
        get() = when (currentPage) {
            0, 1 -> true // Intro pages - always enabled
            2 -> selectedAge != null // Age selection
            3 -> selectedFocusAreas.isNotEmpty() // Focus areas - at least one
            4 -> userName.trim().isNotEmpty() && dailyRhythm != null // Name + rhythm
            5 -> weeklyGoal != null && startingHabitCount != null // Goals
            else -> true
        }
}

enum class AgeRange(val label: String) {
    AGE_18_24("18-24"),
    AGE_25_34("25-34"),
    AGE_35_44("35-44"),
    AGE_45_PLUS("45+")
}

enum class FocusArea(val label: String, val emoji: String) {
    HEALTH("Health", "💪"),
    CAREER("Career", "💼"),
    SPORTS("Sports", "⚽"),
    MINDFULNESS("Mindfulness", "🧘"),
    FINANCE("Finance", "💰"),
    OTHER("Other", "✨")
}

enum class DailyRhythm(val label: String, val emoji: String, val description: String) {
    MORNING_PERSON("Morning Person", "🌅", "I'm most productive in the morning"),
    NIGHT_OWL("Night Owl", "🌙", "I'm most productive at night")
}

enum class WeeklyGoal(val label: String, val days: String) {
    LIGHT("Light", "3-4 days/week"),
    MODERATE("Moderate", "5-6 days/week"),
    INTENSIVE("Intensive", "Every day")
}

enum class StartingHabitCount(val label: String, val description: String) {
    FEW("Start Small", "1-2 habits"),
    MODERATE("Balanced", "3-4 habits"),
    MANY("Ambitious", "5+ habits")
}

data class WalkthroughPage(
    val title: String,
    val description: String,
    val imageDescription: String,
    val emoji: String
)

// Only 2 intro pages - rest are interactive
val walkthroughPages = listOf(
    WalkthroughPage(
        title = "Transform\nYour Life",
        description = "Change your habits, redesign your life. Small daily actions lead to extraordinary results.",
        imageDescription = "Welcome illustration",
        emoji = "🚀"
    ),
    WalkthroughPage(
        title = "Build Better\nHabits",
        description = "Break bad habits, create new routines, and track your progress. We're here to help you every step of the way.",
        imageDescription = "Habits illustration",
        emoji = "✨"
    )
)
