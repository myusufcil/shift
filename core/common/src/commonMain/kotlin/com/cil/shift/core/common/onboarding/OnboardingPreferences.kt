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

    // Product walkthrough tracking
    fun hasSeenProductWalkthrough(): Boolean
    fun setProductWalkthroughSeen(seen: Boolean)

    // Calendar walkthrough tracking
    fun hasSeenCalendarWalkthrough(): Boolean
    fun setCalendarWalkthroughSeen(seen: Boolean)

    // Calendar view mode preference
    fun getCalendarViewMode(): String
    fun setCalendarViewMode(mode: String)

    // Calendar grid settings - separate width/height for each view mode
    // Week view
    fun getWeekViewColumnWidth(): Int  // default 80dp
    fun setWeekViewColumnWidth(width: Int)
    fun getWeekViewRowHeight(): Int    // default 70dp
    fun setWeekViewRowHeight(height: Int)

    // Month view
    fun getMonthViewColumnWidth(): Int // default 56dp
    fun setMonthViewColumnWidth(width: Int)
    fun getMonthViewRowHeight(): Int   // default 70dp
    fun setMonthViewRowHeight(height: Int)

    // Day view (1 day)
    fun getDayViewColumnWidth(): Int   // default 100dp
    fun setDayViewColumnWidth(width: Int)
    fun getDayViewRowHeight(): Int     // default 80dp
    fun setDayViewRowHeight(height: Int)

    // 3-day view
    fun getDay3ViewColumnWidth(): Int   // default 90dp
    fun setDay3ViewColumnWidth(width: Int)
    fun getDay3ViewRowHeight(): Int     // default 75dp
    fun setDay3ViewRowHeight(height: Int)
}
