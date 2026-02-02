package com.cil.shift.core.common.pomodoro

interface PomodoroPreferences {
    // Settings
    fun getFocusDuration(): Int
    fun setFocusDuration(minutes: Int)
    fun getShortBreakDuration(): Int
    fun setShortBreakDuration(minutes: Int)
    fun getLongBreakDuration(): Int
    fun setLongBreakDuration(minutes: Int)
    fun getSessionsBeforeLongBreak(): Int
    fun setSessionsBeforeLongBreak(count: Int)
    fun isAutoStartBreaks(): Boolean
    fun setAutoStartBreaks(enabled: Boolean)
    fun isAutoStartFocus(): Boolean
    fun setAutoStartFocus(enabled: Boolean)
    fun getLinkedHabitId(): String?
    fun setLinkedHabitId(habitId: String?)

    // Active timer persistence
    fun saveActiveTimer(
        phase: String,
        remainingSeconds: Int,
        currentSession: Int,
        completedSessions: Int,
        isPaused: Boolean,
        savedAtTimestamp: Long
    )
    fun getActiveTimer(): ActiveTimerState?
    fun clearActiveTimer()

    // Stats
    fun getTodayFocusMinutes(): Int
    fun addTodayFocusMinutes(minutes: Int)
    fun resetTodayFocusMinutes()
    fun getTodayDateKey(): String?
    fun setTodayDateKey(dateKey: String)
    fun getTotalSessions(): Int
    fun incrementTotalSessions()
}

data class ActiveTimerState(
    val phase: String,
    val remainingSeconds: Int,
    val currentSession: Int,
    val completedSessions: Int,
    val isPaused: Boolean,
    val savedAtTimestamp: Long
)
