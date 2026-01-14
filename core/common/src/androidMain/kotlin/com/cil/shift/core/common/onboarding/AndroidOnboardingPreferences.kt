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

    override fun getCalendarViewMode(): String {
        return prefs.getString(KEY_CALENDAR_VIEW_MODE, "WEEK") ?: "WEEK"
    }

    override fun setCalendarViewMode(mode: String) {
        prefs.edit().putString(KEY_CALENDAR_VIEW_MODE, mode).apply()
    }

    // Week view
    override fun getWeekViewColumnWidth(): Int {
        return prefs.getInt(KEY_WEEK_VIEW_COLUMN_WIDTH, 80).coerceIn(40, 150)
    }
    override fun setWeekViewColumnWidth(width: Int) {
        prefs.edit().putInt(KEY_WEEK_VIEW_COLUMN_WIDTH, width.coerceIn(40, 150)).apply()
    }
    override fun getWeekViewRowHeight(): Int {
        return prefs.getInt(KEY_WEEK_VIEW_ROW_HEIGHT, 70).coerceIn(50, 150)
    }
    override fun setWeekViewRowHeight(height: Int) {
        prefs.edit().putInt(KEY_WEEK_VIEW_ROW_HEIGHT, height.coerceIn(50, 150)).apply()
    }

    // Month view
    override fun getMonthViewColumnWidth(): Int {
        return prefs.getInt(KEY_MONTH_VIEW_COLUMN_WIDTH, 56).coerceIn(40, 150)
    }
    override fun setMonthViewColumnWidth(width: Int) {
        prefs.edit().putInt(KEY_MONTH_VIEW_COLUMN_WIDTH, width.coerceIn(40, 150)).apply()
    }
    override fun getMonthViewRowHeight(): Int {
        return prefs.getInt(KEY_MONTH_VIEW_ROW_HEIGHT, 70).coerceIn(50, 150)
    }
    override fun setMonthViewRowHeight(height: Int) {
        prefs.edit().putInt(KEY_MONTH_VIEW_ROW_HEIGHT, height.coerceIn(50, 150)).apply()
    }

    // Day view
    override fun getDayViewColumnWidth(): Int {
        return prefs.getInt(KEY_DAY_VIEW_COLUMN_WIDTH, 100).coerceIn(40, 200)
    }
    override fun setDayViewColumnWidth(width: Int) {
        prefs.edit().putInt(KEY_DAY_VIEW_COLUMN_WIDTH, width.coerceIn(40, 200)).apply()
    }
    override fun getDayViewRowHeight(): Int {
        return prefs.getInt(KEY_DAY_VIEW_ROW_HEIGHT, 80).coerceIn(50, 150)
    }
    override fun setDayViewRowHeight(height: Int) {
        prefs.edit().putInt(KEY_DAY_VIEW_ROW_HEIGHT, height.coerceIn(50, 150)).apply()
    }

    // 3-day view
    override fun getDay3ViewColumnWidth(): Int {
        return prefs.getInt(KEY_DAY3_VIEW_COLUMN_WIDTH, 90).coerceIn(40, 180)
    }
    override fun setDay3ViewColumnWidth(width: Int) {
        prefs.edit().putInt(KEY_DAY3_VIEW_COLUMN_WIDTH, width.coerceIn(40, 180)).apply()
    }
    override fun getDay3ViewRowHeight(): Int {
        return prefs.getInt(KEY_DAY3_VIEW_ROW_HEIGHT, 75).coerceIn(50, 150)
    }
    override fun setDay3ViewRowHeight(height: Int) {
        prefs.edit().putInt(KEY_DAY3_VIEW_ROW_HEIGHT, height.coerceIn(50, 150)).apply()
    }

    // Timer persistence
    override fun saveRunningTimer(habitId: String, startTimestamp: Long) {
        val timers = getRunningTimersInternal().toMutableMap()
        timers[habitId] = startTimestamp
        saveRunningTimersInternal(timers)
    }

    override fun removeRunningTimer(habitId: String) {
        val timers = getRunningTimersInternal().toMutableMap()
        timers.remove(habitId)
        saveRunningTimersInternal(timers)
    }

    override fun getRunningTimers(): Map<String, Long> {
        return getRunningTimersInternal()
    }

    override fun clearAllRunningTimers() {
        prefs.edit().putString(KEY_RUNNING_TIMERS, "").apply()
    }

    private fun getRunningTimersInternal(): Map<String, Long> {
        val timersString = prefs.getString(KEY_RUNNING_TIMERS, "") ?: ""
        if (timersString.isEmpty()) return emptyMap()

        return try {
            timersString.split(";")
                .filter { it.isNotEmpty() }
                .mapNotNull { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        parts[0] to parts[1].toLongOrNull()
                    } else null
                }
                .filter { it.second != null }
                .associate { it.first to it.second!! }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveRunningTimersInternal(timers: Map<String, Long>) {
        val timersString = timers.entries.joinToString(";") { "${it.key}:${it.value}" }
        prefs.edit().putString(KEY_RUNNING_TIMERS, timersString).apply()
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
        private const val KEY_CALENDAR_VIEW_MODE = "calendar_view_mode"
        private const val KEY_WEEK_VIEW_COLUMN_WIDTH = "week_view_column_width"
        private const val KEY_WEEK_VIEW_ROW_HEIGHT = "week_view_row_height"
        private const val KEY_MONTH_VIEW_COLUMN_WIDTH = "month_view_column_width"
        private const val KEY_MONTH_VIEW_ROW_HEIGHT = "month_view_row_height"
        private const val KEY_DAY_VIEW_COLUMN_WIDTH = "day_view_column_width"
        private const val KEY_DAY_VIEW_ROW_HEIGHT = "day_view_row_height"
        private const val KEY_DAY3_VIEW_COLUMN_WIDTH = "day3_view_column_width"
        private const val KEY_DAY3_VIEW_ROW_HEIGHT = "day3_view_row_height"
        private const val KEY_RUNNING_TIMERS = "running_timers"
    }
}
