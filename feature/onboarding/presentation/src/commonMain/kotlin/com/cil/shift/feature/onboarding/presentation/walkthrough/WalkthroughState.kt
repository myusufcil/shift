package com.cil.shift.feature.onboarding.presentation.walkthrough

data class WalkthroughState(
    val currentPage: Int = 0,
    val totalPages: Int = 4
) {
    val isLastPage: Boolean
        get() = currentPage == totalPages - 1
}

data class WalkthroughPage(
    val title: String,
    val description: String,
    val imageDescription: String,
    val emoji: String
)

val walkthroughPages = listOf(
    WalkthroughPage(
        title = "Welcome to\nShift",
        description = "Your personal companion for building better habits and transforming your daily routine",
        imageDescription = "Welcome illustration",
        emoji = "👋"
    ),
    WalkthroughPage(
        title = "Track Your\nHabits",
        description = "Create and manage daily habits. Mark them complete as you build consistency day by day",
        imageDescription = "Habit tracking illustration",
        emoji = "✅"
    ),
    WalkthroughPage(
        title = "Visualize\nProgress",
        description = "Beautiful charts and statistics show your journey. Watch your success grow over time",
        imageDescription = "Statistics illustration",
        emoji = "📊"
    ),
    WalkthroughPage(
        title = "Build\nStreaks",
        description = "Stay consistent and build powerful streaks. Small steps lead to big transformations",
        imageDescription = "Streak illustration",
        emoji = "🔥"
    )
)
