package com.cil.shift.core.common.onboarding

import platform.Foundation.NSUserDefaults

class IOSOnboardingPreferences : OnboardingPreferences {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    override fun isOnboardingCompleted(): Boolean {
        return userDefaults.boolForKey(KEY_ONBOARDING_COMPLETED)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        userDefaults.setBool(completed, KEY_ONBOARDING_COMPLETED)
        userDefaults.synchronize()
    }

    override fun getUserName(): String {
        return userDefaults.stringForKey(KEY_USER_NAME) ?: ""
    }

    override fun setUserName(name: String) {
        userDefaults.setObject(name, KEY_USER_NAME)
        userDefaults.synchronize()
    }

    override fun getAgeRange(): String? {
        return userDefaults.stringForKey(KEY_AGE_RANGE)
    }

    override fun setAgeRange(ageRange: String?) {
        userDefaults.setObject(ageRange, KEY_AGE_RANGE)
        userDefaults.synchronize()
    }

    override fun getFocusAreas(): Set<String> {
        val stored = userDefaults.stringForKey(KEY_FOCUS_AREAS) ?: return emptySet()
        return if (stored.isBlank()) emptySet() else stored.split(",").toSet()
    }

    override fun setFocusAreas(areas: Set<String>) {
        userDefaults.setObject(areas.joinToString(","), KEY_FOCUS_AREAS)
        userDefaults.synchronize()
    }

    override fun getDailyRhythm(): String? {
        return userDefaults.stringForKey(KEY_DAILY_RHYTHM)
    }

    override fun setDailyRhythm(rhythm: String?) {
        userDefaults.setObject(rhythm, KEY_DAILY_RHYTHM)
        userDefaults.synchronize()
    }

    override fun getWeeklyGoal(): String? {
        return userDefaults.stringForKey(KEY_WEEKLY_GOAL)
    }

    override fun setWeeklyGoal(goal: String?) {
        userDefaults.setObject(goal, KEY_WEEKLY_GOAL)
        userDefaults.synchronize()
    }

    override fun getStartingHabitCount(): String? {
        return userDefaults.stringForKey(KEY_STARTING_HABIT_COUNT)
    }

    override fun setStartingHabitCount(count: String?) {
        userDefaults.setObject(count, KEY_STARTING_HABIT_COUNT)
        userDefaults.synchronize()
    }

    override fun getLastConfettiDate(): String? {
        return userDefaults.stringForKey(KEY_LAST_CONFETTI_DATE)
    }

    override fun setLastConfettiDate(date: String?) {
        userDefaults.setObject(date, KEY_LAST_CONFETTI_DATE)
        userDefaults.synchronize()
    }

    override fun hasSeenProductWalkthrough(): Boolean {
        return userDefaults.boolForKey(KEY_PRODUCT_WALKTHROUGH_SEEN)
    }

    override fun setProductWalkthroughSeen(seen: Boolean) {
        userDefaults.setBool(seen, KEY_PRODUCT_WALKTHROUGH_SEEN)
        userDefaults.synchronize()
    }

    override fun hasSeenCalendarWalkthrough(): Boolean {
        return userDefaults.boolForKey(KEY_CALENDAR_WALKTHROUGH_SEEN)
    }

    override fun setCalendarWalkthroughSeen(seen: Boolean) {
        userDefaults.setBool(seen, KEY_CALENDAR_WALKTHROUGH_SEEN)
        userDefaults.synchronize()
    }

    override fun getCalendarViewMode(): String {
        return userDefaults.stringForKey(KEY_CALENDAR_VIEW_MODE) ?: "WEEK"
    }

    override fun setCalendarViewMode(mode: String) {
        userDefaults.setObject(mode, KEY_CALENDAR_VIEW_MODE)
        userDefaults.synchronize()
    }

    // Week view
    override fun getWeekViewColumnWidth(): Int {
        val value = userDefaults.integerForKey(KEY_WEEK_VIEW_COLUMN_WIDTH).toInt()
        return if (value == 0) 80 else value.coerceIn(40, 150)
    }
    override fun setWeekViewColumnWidth(width: Int) {
        userDefaults.setInteger(width.coerceIn(40, 150).toLong(), KEY_WEEK_VIEW_COLUMN_WIDTH)
        userDefaults.synchronize()
    }
    override fun getWeekViewRowHeight(): Int {
        val value = userDefaults.integerForKey(KEY_WEEK_VIEW_ROW_HEIGHT).toInt()
        return if (value == 0) 70 else value.coerceIn(50, 150)
    }
    override fun setWeekViewRowHeight(height: Int) {
        userDefaults.setInteger(height.coerceIn(50, 150).toLong(), KEY_WEEK_VIEW_ROW_HEIGHT)
        userDefaults.synchronize()
    }

    // Month view
    override fun getMonthViewColumnWidth(): Int {
        val value = userDefaults.integerForKey(KEY_MONTH_VIEW_COLUMN_WIDTH).toInt()
        return if (value == 0) 56 else value.coerceIn(40, 150)
    }
    override fun setMonthViewColumnWidth(width: Int) {
        userDefaults.setInteger(width.coerceIn(40, 150).toLong(), KEY_MONTH_VIEW_COLUMN_WIDTH)
        userDefaults.synchronize()
    }
    override fun getMonthViewRowHeight(): Int {
        val value = userDefaults.integerForKey(KEY_MONTH_VIEW_ROW_HEIGHT).toInt()
        return if (value == 0) 70 else value.coerceIn(50, 150)
    }
    override fun setMonthViewRowHeight(height: Int) {
        userDefaults.setInteger(height.coerceIn(50, 150).toLong(), KEY_MONTH_VIEW_ROW_HEIGHT)
        userDefaults.synchronize()
    }

    // Day view
    override fun getDayViewColumnWidth(): Int {
        val value = userDefaults.integerForKey(KEY_DAY_VIEW_COLUMN_WIDTH).toInt()
        return if (value == 0) 100 else value.coerceIn(40, 200)
    }
    override fun setDayViewColumnWidth(width: Int) {
        userDefaults.setInteger(width.coerceIn(40, 200).toLong(), KEY_DAY_VIEW_COLUMN_WIDTH)
        userDefaults.synchronize()
    }
    override fun getDayViewRowHeight(): Int {
        val value = userDefaults.integerForKey(KEY_DAY_VIEW_ROW_HEIGHT).toInt()
        return if (value == 0) 80 else value.coerceIn(50, 150)
    }
    override fun setDayViewRowHeight(height: Int) {
        userDefaults.setInteger(height.coerceIn(50, 150).toLong(), KEY_DAY_VIEW_ROW_HEIGHT)
        userDefaults.synchronize()
    }

    // 3-day view
    override fun getDay3ViewColumnWidth(): Int {
        val value = userDefaults.integerForKey(KEY_DAY3_VIEW_COLUMN_WIDTH).toInt()
        return if (value == 0) 90 else value.coerceIn(40, 180)
    }
    override fun setDay3ViewColumnWidth(width: Int) {
        userDefaults.setInteger(width.coerceIn(40, 180).toLong(), KEY_DAY3_VIEW_COLUMN_WIDTH)
        userDefaults.synchronize()
    }
    override fun getDay3ViewRowHeight(): Int {
        val value = userDefaults.integerForKey(KEY_DAY3_VIEW_ROW_HEIGHT).toInt()
        return if (value == 0) 75 else value.coerceIn(50, 150)
    }
    override fun setDay3ViewRowHeight(height: Int) {
        userDefaults.setInteger(height.coerceIn(50, 150).toLong(), KEY_DAY3_VIEW_ROW_HEIGHT)
        userDefaults.synchronize()
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
    }
}
