package com.cil.shift.core.common.pomodoro

import platform.Foundation.NSUserDefaults

class IOSPomodoroPreferences : PomodoroPreferences {

    private val defaults = NSUserDefaults.standardUserDefaults

    companion object {
        private const val KEY_FOCUS_DURATION = "pomo_focus_duration"
        private const val KEY_SHORT_BREAK = "pomo_short_break"
        private const val KEY_LONG_BREAK = "pomo_long_break"
        private const val KEY_SESSIONS_BEFORE_LONG = "pomo_sessions_before_long"
        private const val KEY_AUTO_START_BREAKS = "pomo_auto_start_breaks"
        private const val KEY_AUTO_START_FOCUS = "pomo_auto_start_focus"
        private const val KEY_LINKED_HABIT_ID = "pomo_linked_habit_id"

        private const val KEY_TIMER_PHASE = "pomo_timer_phase"
        private const val KEY_TIMER_REMAINING = "pomo_timer_remaining"
        private const val KEY_TIMER_CURRENT_SESSION = "pomo_timer_current_session"
        private const val KEY_TIMER_COMPLETED_SESSIONS = "pomo_timer_completed_sessions"
        private const val KEY_TIMER_IS_PAUSED = "pomo_timer_is_paused"
        private const val KEY_TIMER_SAVED_AT = "pomo_timer_saved_at"
        private const val KEY_TIMER_ACTIVE = "pomo_timer_active"

        private const val KEY_TODAY_FOCUS_MINUTES = "pomo_today_focus_minutes"
        private const val KEY_TODAY_DATE_KEY = "pomo_today_date_key"
        private const val KEY_TOTAL_SESSIONS = "pomo_total_sessions"
        private const val KEY_INITIALIZED = "pomo_initialized"

        private const val DEFAULT_FOCUS = 25
        private const val DEFAULT_SHORT_BREAK = 5
        private const val DEFAULT_LONG_BREAK = 15
        private const val DEFAULT_SESSIONS = 4
    }

    init {
        if (!defaults.boolForKey(KEY_INITIALIZED)) {
            defaults.setInteger(DEFAULT_FOCUS.toLong(), KEY_FOCUS_DURATION)
            defaults.setInteger(DEFAULT_SHORT_BREAK.toLong(), KEY_SHORT_BREAK)
            defaults.setInteger(DEFAULT_LONG_BREAK.toLong(), KEY_LONG_BREAK)
            defaults.setInteger(DEFAULT_SESSIONS.toLong(), KEY_SESSIONS_BEFORE_LONG)
            defaults.setBool(true, KEY_INITIALIZED)
        }
    }

    override fun getFocusDuration(): Int {
        val value = defaults.integerForKey(KEY_FOCUS_DURATION)
        return if (value == 0L) DEFAULT_FOCUS else value.toInt()
    }

    override fun setFocusDuration(minutes: Int) {
        defaults.setInteger(minutes.toLong(), KEY_FOCUS_DURATION)
    }

    override fun getShortBreakDuration(): Int {
        val value = defaults.integerForKey(KEY_SHORT_BREAK)
        return if (value == 0L) DEFAULT_SHORT_BREAK else value.toInt()
    }

    override fun setShortBreakDuration(minutes: Int) {
        defaults.setInteger(minutes.toLong(), KEY_SHORT_BREAK)
    }

    override fun getLongBreakDuration(): Int {
        val value = defaults.integerForKey(KEY_LONG_BREAK)
        return if (value == 0L) DEFAULT_LONG_BREAK else value.toInt()
    }

    override fun setLongBreakDuration(minutes: Int) {
        defaults.setInteger(minutes.toLong(), KEY_LONG_BREAK)
    }

    override fun getSessionsBeforeLongBreak(): Int {
        val value = defaults.integerForKey(KEY_SESSIONS_BEFORE_LONG)
        return if (value == 0L) DEFAULT_SESSIONS else value.toInt()
    }

    override fun setSessionsBeforeLongBreak(count: Int) {
        defaults.setInteger(count.toLong(), KEY_SESSIONS_BEFORE_LONG)
    }

    override fun isAutoStartBreaks(): Boolean = defaults.boolForKey(KEY_AUTO_START_BREAKS)
    override fun setAutoStartBreaks(enabled: Boolean) { defaults.setBool(enabled, KEY_AUTO_START_BREAKS) }

    override fun isAutoStartFocus(): Boolean = defaults.boolForKey(KEY_AUTO_START_FOCUS)
    override fun setAutoStartFocus(enabled: Boolean) { defaults.setBool(enabled, KEY_AUTO_START_FOCUS) }

    override fun getLinkedHabitId(): String? = defaults.stringForKey(KEY_LINKED_HABIT_ID)
    override fun setLinkedHabitId(habitId: String?) {
        if (habitId == null) {
            defaults.removeObjectForKey(KEY_LINKED_HABIT_ID)
        } else {
            defaults.setObject(habitId, KEY_LINKED_HABIT_ID)
        }
    }

    override fun saveActiveTimer(
        phase: String,
        remainingSeconds: Int,
        currentSession: Int,
        completedSessions: Int,
        isPaused: Boolean,
        savedAtTimestamp: Long
    ) {
        defaults.setObject(phase, KEY_TIMER_PHASE)
        defaults.setInteger(remainingSeconds.toLong(), KEY_TIMER_REMAINING)
        defaults.setInteger(currentSession.toLong(), KEY_TIMER_CURRENT_SESSION)
        defaults.setInteger(completedSessions.toLong(), KEY_TIMER_COMPLETED_SESSIONS)
        defaults.setBool(isPaused, KEY_TIMER_IS_PAUSED)
        defaults.setDouble(savedAtTimestamp.toDouble(), KEY_TIMER_SAVED_AT)
        defaults.setBool(true, KEY_TIMER_ACTIVE)
    }

    override fun getActiveTimer(): ActiveTimerState? {
        if (!defaults.boolForKey(KEY_TIMER_ACTIVE)) return null
        return ActiveTimerState(
            phase = defaults.stringForKey(KEY_TIMER_PHASE) ?: "FOCUS",
            remainingSeconds = defaults.integerForKey(KEY_TIMER_REMAINING).toInt(),
            currentSession = defaults.integerForKey(KEY_TIMER_CURRENT_SESSION).toInt().coerceAtLeast(1),
            completedSessions = defaults.integerForKey(KEY_TIMER_COMPLETED_SESSIONS).toInt(),
            isPaused = defaults.boolForKey(KEY_TIMER_IS_PAUSED),
            savedAtTimestamp = defaults.doubleForKey(KEY_TIMER_SAVED_AT).toLong()
        )
    }

    override fun clearActiveTimer() {
        defaults.setBool(false, KEY_TIMER_ACTIVE)
    }

    override fun getTodayFocusMinutes(): Int = defaults.integerForKey(KEY_TODAY_FOCUS_MINUTES).toInt()
    override fun addTodayFocusMinutes(minutes: Int) {
        defaults.setInteger((getTodayFocusMinutes() + minutes).toLong(), KEY_TODAY_FOCUS_MINUTES)
    }

    override fun resetTodayFocusMinutes() {
        defaults.setInteger(0L, KEY_TODAY_FOCUS_MINUTES)
    }

    override fun getTodayDateKey(): String? = defaults.stringForKey(KEY_TODAY_DATE_KEY)
    override fun setTodayDateKey(dateKey: String) { defaults.setObject(dateKey, KEY_TODAY_DATE_KEY) }

    override fun getTotalSessions(): Int = defaults.integerForKey(KEY_TOTAL_SESSIONS).toInt()
    override fun incrementTotalSessions() {
        defaults.setInteger((getTotalSessions() + 1).toLong(), KEY_TOTAL_SESSIONS)
    }
}
