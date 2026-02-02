package com.cil.shift.core.common.pomodoro

import android.content.Context
import android.content.SharedPreferences

class AndroidPomodoroPreferences(context: Context) : PomodoroPreferences {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "shift_pomodoro_prefs"
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

        private const val DEFAULT_FOCUS = 25
        private const val DEFAULT_SHORT_BREAK = 5
        private const val DEFAULT_LONG_BREAK = 15
        private const val DEFAULT_SESSIONS = 4
    }

    override fun getFocusDuration(): Int = prefs.getInt(KEY_FOCUS_DURATION, DEFAULT_FOCUS)
    override fun setFocusDuration(minutes: Int) { prefs.edit().putInt(KEY_FOCUS_DURATION, minutes).apply() }

    override fun getShortBreakDuration(): Int = prefs.getInt(KEY_SHORT_BREAK, DEFAULT_SHORT_BREAK)
    override fun setShortBreakDuration(minutes: Int) { prefs.edit().putInt(KEY_SHORT_BREAK, minutes).apply() }

    override fun getLongBreakDuration(): Int = prefs.getInt(KEY_LONG_BREAK, DEFAULT_LONG_BREAK)
    override fun setLongBreakDuration(minutes: Int) { prefs.edit().putInt(KEY_LONG_BREAK, minutes).apply() }

    override fun getSessionsBeforeLongBreak(): Int = prefs.getInt(KEY_SESSIONS_BEFORE_LONG, DEFAULT_SESSIONS)
    override fun setSessionsBeforeLongBreak(count: Int) { prefs.edit().putInt(KEY_SESSIONS_BEFORE_LONG, count).apply() }

    override fun isAutoStartBreaks(): Boolean = prefs.getBoolean(KEY_AUTO_START_BREAKS, false)
    override fun setAutoStartBreaks(enabled: Boolean) { prefs.edit().putBoolean(KEY_AUTO_START_BREAKS, enabled).apply() }

    override fun isAutoStartFocus(): Boolean = prefs.getBoolean(KEY_AUTO_START_FOCUS, false)
    override fun setAutoStartFocus(enabled: Boolean) { prefs.edit().putBoolean(KEY_AUTO_START_FOCUS, enabled).apply() }

    override fun getLinkedHabitId(): String? = prefs.getString(KEY_LINKED_HABIT_ID, null)
    override fun setLinkedHabitId(habitId: String?) {
        if (habitId == null) {
            prefs.edit().remove(KEY_LINKED_HABIT_ID).apply()
        } else {
            prefs.edit().putString(KEY_LINKED_HABIT_ID, habitId).apply()
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
        prefs.edit()
            .putString(KEY_TIMER_PHASE, phase)
            .putInt(KEY_TIMER_REMAINING, remainingSeconds)
            .putInt(KEY_TIMER_CURRENT_SESSION, currentSession)
            .putInt(KEY_TIMER_COMPLETED_SESSIONS, completedSessions)
            .putBoolean(KEY_TIMER_IS_PAUSED, isPaused)
            .putLong(KEY_TIMER_SAVED_AT, savedAtTimestamp)
            .putBoolean(KEY_TIMER_ACTIVE, true)
            .apply()
    }

    override fun getActiveTimer(): ActiveTimerState? {
        if (!prefs.getBoolean(KEY_TIMER_ACTIVE, false)) return null
        return ActiveTimerState(
            phase = prefs.getString(KEY_TIMER_PHASE, "FOCUS") ?: "FOCUS",
            remainingSeconds = prefs.getInt(KEY_TIMER_REMAINING, 0),
            currentSession = prefs.getInt(KEY_TIMER_CURRENT_SESSION, 1),
            completedSessions = prefs.getInt(KEY_TIMER_COMPLETED_SESSIONS, 0),
            isPaused = prefs.getBoolean(KEY_TIMER_IS_PAUSED, false),
            savedAtTimestamp = prefs.getLong(KEY_TIMER_SAVED_AT, 0L)
        )
    }

    override fun clearActiveTimer() {
        prefs.edit().putBoolean(KEY_TIMER_ACTIVE, false).apply()
    }

    override fun getTodayFocusMinutes(): Int = prefs.getInt(KEY_TODAY_FOCUS_MINUTES, 0)
    override fun addTodayFocusMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_TODAY_FOCUS_MINUTES, getTodayFocusMinutes() + minutes).apply()
    }

    override fun resetTodayFocusMinutes() {
        prefs.edit().putInt(KEY_TODAY_FOCUS_MINUTES, 0).apply()
    }

    override fun getTodayDateKey(): String? = prefs.getString(KEY_TODAY_DATE_KEY, null)
    override fun setTodayDateKey(dateKey: String) { prefs.edit().putString(KEY_TODAY_DATE_KEY, dateKey).apply() }

    override fun getTotalSessions(): Int = prefs.getInt(KEY_TOTAL_SESSIONS, 0)
    override fun incrementTotalSessions() {
        prefs.edit().putInt(KEY_TOTAL_SESSIONS, getTotalSessions() + 1).apply()
    }
}
