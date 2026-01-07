package com.cil.shift.core.common.onboarding

import android.content.Context
import android.content.SharedPreferences

class AndroidOnboardingPreferences(context: Context) : OnboardingPreferences {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "shift_onboarding_prefs",
        Context.MODE_PRIVATE
    )

    override fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    override fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }

    override fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    override fun getAgeRange(): String? {
        return prefs.getString(KEY_AGE_RANGE, null)
    }

    override fun setAgeRange(ageRange: String?) {
        prefs.edit().putString(KEY_AGE_RANGE, ageRange).apply()
    }

    override fun getFocusAreas(): Set<String> {
        return prefs.getStringSet(KEY_FOCUS_AREAS, emptySet()) ?: emptySet()
    }

    override fun setFocusAreas(areas: Set<String>) {
        prefs.edit().putStringSet(KEY_FOCUS_AREAS, areas).apply()
    }

    override fun getDailyRhythm(): String? {
        return prefs.getString(KEY_DAILY_RHYTHM, null)
    }

    override fun setDailyRhythm(rhythm: String?) {
        prefs.edit().putString(KEY_DAILY_RHYTHM, rhythm).apply()
    }

    override fun getWeeklyGoal(): String? {
        return prefs.getString(KEY_WEEKLY_GOAL, null)
    }

    override fun setWeeklyGoal(goal: String?) {
        prefs.edit().putString(KEY_WEEKLY_GOAL, goal).apply()
    }

    override fun getStartingHabitCount(): String? {
        return prefs.getString(KEY_STARTING_HABIT_COUNT, null)
    }

    override fun setStartingHabitCount(count: String?) {
        prefs.edit().putString(KEY_STARTING_HABIT_COUNT, count).apply()
    }

    override fun getLastConfettiDate(): String? {
        return prefs.getString(KEY_LAST_CONFETTI_DATE, null)
    }

    override fun setLastConfettiDate(date: String?) {
        prefs.edit().putString(KEY_LAST_CONFETTI_DATE, date).commit()
    }

    override fun hasSeenProductWalkthrough(): Boolean {
        return prefs.getBoolean(KEY_PRODUCT_WALKTHROUGH_SEEN, false)
    }

    override fun setProductWalkthroughSeen(seen: Boolean) {
        prefs.edit().putBoolean(KEY_PRODUCT_WALKTHROUGH_SEEN, seen).apply()
    }

    override fun hasSeenCalendarWalkthrough(): Boolean {
        return prefs.getBoolean(KEY_CALENDAR_WALKTHROUGH_SEEN, false)
    }

    override fun setCalendarWalkthroughSeen(seen: Boolean) {
        prefs.edit().putBoolean(KEY_CALENDAR_WALKTHROUGH_SEEN, seen).apply()
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_AGE_RANGE = "age_range"
        private const val KEY_FOCUS_AREAS = "focus_areas"
        private const val KEY_DAILY_RHYTHM = "daily_rhythm"
        private const val KEY_WEEKLY_GOAL = "weekly_goal"
        private const val KEY_STARTING_HABIT_COUNT = "starting_habit_count"
        private const val KEY_LAST_CONFETTI_DATE = "last_confetti_date"
        private const val KEY_PRODUCT_WALKTHROUGH_SEEN = "product_walkthrough_seen"
        private const val KEY_CALENDAR_WALKTHROUGH_SEEN = "calendar_walkthrough_seen"
    }
}
