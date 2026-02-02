package com.cil.shift.feature.pomodoro.presentation

enum class PomodoroPhase { FOCUS, SHORT_BREAK, LONG_BREAK }
enum class TimerStatus { IDLE, RUNNING, PAUSED }

data class PomodoroState(
    // Settings
    val focusDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val linkedHabitId: String? = null,
    // Timer
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val status: TimerStatus = TimerStatus.IDLE,
    val remainingSeconds: Int = 25 * 60,
    val totalPhaseSeconds: Int = 25 * 60,
    // Sessions
    val currentSession: Int = 1,
    val completedSessions: Int = 0,
    // UI
    val showSettings: Boolean = false,
    val todayFocusMinutes: Int = 0,
    val totalSessions: Int = 0,
    val availableHabits: List<LinkableHabit> = emptyList()
)

data class LinkableHabit(
    val id: String,
    val name: String,
    val icon: String,
    val color: String
)

sealed interface PomodoroEvent {
    data object Start : PomodoroEvent
    data object Pause : PomodoroEvent
    data object Resume : PomodoroEvent
    data object Reset : PomodoroEvent
    data object Skip : PomodoroEvent
    data object ToggleSettings : PomodoroEvent
    data class UpdateFocusDuration(val minutes: Int) : PomodoroEvent
    data class UpdateShortBreak(val minutes: Int) : PomodoroEvent
    data class UpdateLongBreak(val minutes: Int) : PomodoroEvent
    data class UpdateSessionCount(val count: Int) : PomodoroEvent
    data class SetAutoStartBreaks(val enabled: Boolean) : PomodoroEvent
    data class SetAutoStartFocus(val enabled: Boolean) : PomodoroEvent
    data class LinkHabit(val habitId: String?) : PomodoroEvent
}
