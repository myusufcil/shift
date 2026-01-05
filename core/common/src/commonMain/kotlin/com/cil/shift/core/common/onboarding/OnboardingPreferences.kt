package com.cil.shift.core.common.onboarding

interface OnboardingPreferences {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)

    // User profile data
    fun getUserName(): String
    fun setUserName(name: String)

    fun getAgeRange(): String?
    fun setAgeRange(ageRange: String?)

    fun getFocusAreas(): Set<String>
    fun setFocusAreas(areas: Set<String>)

    fun getDailyRhythm(): String?
    fun setDailyRhythm(rhythm: String?)

    fun getWeeklyGoal(): String?
    fun setWeeklyGoal(goal: String?)

    fun getStartingHabitCount(): String?
    fun setStartingHabitCount(count: String?)

    // Confetti tracking
    fun getLastConfettiDate(): String?
    fun setLastConfettiDate(date: String?)
}
