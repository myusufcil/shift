package com.cil.shift.feature.pomodoro.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cil.shift.core.common.localization.LocalizationManager
import com.cil.shift.core.common.pomodoro.PomodoroPreferences
import com.cil.shift.feature.habits.domain.model.HabitType
import com.cil.shift.feature.habits.domain.repository.HabitRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.cancellation.CancellationException

class PomodoroViewModel(
    private val pomodoroPreferences: PomodoroPreferences,
    private val habitRepository: HabitRepository,
    private val localizationManager: LocalizationManager
) : ViewModel() {

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var saveCounter = 0

    init {
        loadSettings()
        loadStats()
        loadAvailableHabits()
        restoreActiveTimer()
    }

    fun onEvent(event: PomodoroEvent) {
        when (event) {
            is PomodoroEvent.Start -> startTimer()
            is PomodoroEvent.Pause -> pauseTimer()
            is PomodoroEvent.Resume -> resumeTimer()
            is PomodoroEvent.Reset -> resetTimer()
            is PomodoroEvent.Skip -> skipPhase()
            is PomodoroEvent.ToggleSettings -> toggleSettings()
            is PomodoroEvent.UpdateFocusDuration -> updateFocusDuration(event.minutes)
            is PomodoroEvent.UpdateShortBreak -> updateShortBreak(event.minutes)
            is PomodoroEvent.UpdateLongBreak -> updateLongBreak(event.minutes)
            is PomodoroEvent.UpdateSessionCount -> updateSessionCount(event.count)
            is PomodoroEvent.SetAutoStartBreaks -> setAutoStartBreaks(event.enabled)
            is PomodoroEvent.SetAutoStartFocus -> setAutoStartFocus(event.enabled)
            is PomodoroEvent.LinkHabit -> linkHabit(event.habitId)
        }
    }

    private fun loadSettings() {
        val focus = pomodoroPreferences.getFocusDuration()
        val shortBreak = pomodoroPreferences.getShortBreakDuration()
        val longBreak = pomodoroPreferences.getLongBreakDuration()
        val sessions = pomodoroPreferences.getSessionsBeforeLongBreak().coerceAtLeast(1)
        val autoBreaks = pomodoroPreferences.isAutoStartBreaks()
        val autoFocus = pomodoroPreferences.isAutoStartFocus()
        val linkedHabit = pomodoroPreferences.getLinkedHabitId()

        _state.update {
            it.copy(
                focusDuration = focus,
                shortBreakDuration = shortBreak,
                longBreakDuration = longBreak,
                sessionsBeforeLongBreak = sessions,
                autoStartBreaks = autoBreaks,
                autoStartFocus = autoFocus,
                linkedHabitId = linkedHabit,
                remainingSeconds = focus * 60,
                totalPhaseSeconds = focus * 60
            )
        }
    }

    private fun loadStats() {
        val todayKey = getTodayDateKey()
        val storedKey = pomodoroPreferences.getTodayDateKey()
        if (storedKey != todayKey) {
            pomodoroPreferences.setTodayDateKey(todayKey)
            pomodoroPreferences.resetTodayFocusMinutes()
        }
        _state.update {
            it.copy(
                todayFocusMinutes = pomodoroPreferences.getTodayFocusMinutes(),
                totalSessions = pomodoroPreferences.getTotalSessions()
            )
        }
    }

    private fun loadAvailableHabits() {
        viewModelScope.launch {
            try {
                val habits = habitRepository.getHabits().first()
                val timerHabits = habits
                    .filter { it.habitType == HabitType.TIMER }
                    .map { LinkableHabit(it.id, it.name, it.icon, it.color) }
                _state.update { it.copy(availableHabits = timerHabits) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Habits not available
            }
        }
    }

    private fun restoreActiveTimer() {
        val saved = pomodoroPreferences.getActiveTimer() ?: return
        val phase = try {
            PomodoroPhase.valueOf(saved.phase)
        } catch (_: Exception) {
            PomodoroPhase.FOCUS
        }

        val totalSeconds = when (phase) {
            PomodoroPhase.FOCUS -> _state.value.focusDuration * 60
            PomodoroPhase.SHORT_BREAK -> _state.value.shortBreakDuration * 60
            PomodoroPhase.LONG_BREAK -> _state.value.longBreakDuration * 60
        }

        if (saved.isPaused) {
            _state.update {
                it.copy(
                    phase = phase,
                    status = TimerStatus.PAUSED,
                    remainingSeconds = saved.remainingSeconds,
                    totalPhaseSeconds = totalSeconds,
                    currentSession = saved.currentSession,
                    completedSessions = saved.completedSessions
                )
            }
        } else {
            val now = Clock.System.now().toEpochMilliseconds()
            val elapsed = ((now - saved.savedAtTimestamp) / 1000).toInt()
            val remaining = (saved.remainingSeconds - elapsed).coerceAtLeast(0)

            if (remaining > 0) {
                _state.update {
                    it.copy(
                        phase = phase,
                        status = TimerStatus.RUNNING,
                        remainingSeconds = remaining,
                        totalPhaseSeconds = totalSeconds,
                        currentSession = saved.currentSession,
                        completedSessions = saved.completedSessions
                    )
                }
                startCountdown()
            } else {
                // Timer expired while away - complete the phase
                // Calculate actual elapsed focus time for accurate stats
                val actualRemaining = saved.remainingSeconds
                _state.update {
                    it.copy(
                        phase = phase,
                        remainingSeconds = 0,
                        currentSession = saved.currentSession,
                        completedSessions = saved.completedSessions,
                        totalPhaseSeconds = totalSeconds
                    )
                }
                onPhaseComplete()
            }
        }
    }

    private fun startTimer() {
        _state.update { it.copy(status = TimerStatus.RUNNING) }
        startCountdown()
        saveTimerState()
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(status = TimerStatus.PAUSED) }
        saveTimerState()
    }

    private fun resumeTimer() {
        _state.update { it.copy(status = TimerStatus.RUNNING) }
        startCountdown()
        saveTimerState()
    }

    private fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        pomodoroPreferences.clearActiveTimer()

        val focusDuration = _state.value.focusDuration
        _state.update {
            it.copy(
                phase = PomodoroPhase.FOCUS,
                status = TimerStatus.IDLE,
                remainingSeconds = focusDuration * 60,
                totalPhaseSeconds = focusDuration * 60,
                currentSession = 1,
                completedSessions = 0
            )
        }
    }

    private fun skipPhase() {
        timerJob?.cancel()
        timerJob = null
        onPhaseComplete()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        saveCounter = 0
        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.status == TimerStatus.RUNNING) {
                delay(1000L)
                _state.update {
                    it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0))
                }
                saveCounter++
                if (saveCounter >= 10) {
                    saveTimerState()
                    saveCounter = 0
                }
            }
            if (_state.value.remainingSeconds <= 0) {
                onPhaseComplete()
            }
        }
    }

    private fun onPhaseComplete() {
        val current = _state.value

        when (current.phase) {
            PomodoroPhase.FOCUS -> {
                // Calculate actual elapsed minutes (not full duration)
                val elapsedSeconds = current.totalPhaseSeconds - current.remainingSeconds
                val actualFocusMinutes = (elapsedSeconds / 60).coerceAtLeast(0)

                if (actualFocusMinutes > 0) {
                    pomodoroPreferences.addTodayFocusMinutes(actualFocusMinutes)
                }
                pomodoroPreferences.incrementTotalSessions()
                val newCompleted = current.completedSessions + 1

                // Update linked habit with actual elapsed minutes
                if (actualFocusMinutes > 0) {
                    current.linkedHabitId?.let { habitId ->
                        viewModelScope.launch {
                            try {
                                val today = getTodayDateKey()
                                val completion = habitRepository.getCompletion(habitId, today)
                                val currentValue = completion?.currentValue ?: 0
                                habitRepository.updateCurrentValue(habitId, today, currentValue + actualFocusMinutes)
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) { }
                        }
                    }
                }

                val sessionsBeforeLong = current.sessionsBeforeLongBreak.coerceAtLeast(1)
                val isLongBreak = newCompleted % sessionsBeforeLong == 0
                val nextPhase = if (isLongBreak) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
                val breakDuration = if (isLongBreak) current.longBreakDuration else current.shortBreakDuration

                _state.update {
                    it.copy(
                        phase = nextPhase,
                        status = if (current.autoStartBreaks) TimerStatus.RUNNING else TimerStatus.IDLE,
                        remainingSeconds = breakDuration * 60,
                        totalPhaseSeconds = breakDuration * 60,
                        completedSessions = newCompleted,
                        todayFocusMinutes = pomodoroPreferences.getTodayFocusMinutes(),
                        totalSessions = pomodoroPreferences.getTotalSessions()
                    )
                }

                if (current.autoStartBreaks) {
                    startCountdown()
                    saveTimerState()
                } else {
                    pomodoroPreferences.clearActiveTimer()
                }
            }

            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> {
                val focusDuration = current.focusDuration

                // After long break, reset cycle; after short break, increment session
                val isAfterLongBreak = current.phase == PomodoroPhase.LONG_BREAK
                val nextSession = if (isAfterLongBreak) 1 else current.currentSession + 1
                val nextCompleted = if (isAfterLongBreak) 0 else current.completedSessions

                _state.update {
                    it.copy(
                        phase = PomodoroPhase.FOCUS,
                        status = if (current.autoStartFocus) TimerStatus.RUNNING else TimerStatus.IDLE,
                        remainingSeconds = focusDuration * 60,
                        totalPhaseSeconds = focusDuration * 60,
                        currentSession = nextSession,
                        completedSessions = nextCompleted
                    )
                }

                if (current.autoStartFocus) {
                    startCountdown()
                    saveTimerState()
                } else {
                    pomodoroPreferences.clearActiveTimer()
                }
            }
        }
    }

    private fun saveTimerState() {
        val s = _state.value
        if (s.status == TimerStatus.IDLE) return
        pomodoroPreferences.saveActiveTimer(
            phase = s.phase.name,
            remainingSeconds = s.remainingSeconds,
            currentSession = s.currentSession,
            completedSessions = s.completedSessions,
            isPaused = s.status == TimerStatus.PAUSED,
            savedAtTimestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    private fun toggleSettings() {
        _state.update { it.copy(showSettings = !it.showSettings) }
    }

    private fun updateFocusDuration(minutes: Int) {
        pomodoroPreferences.setFocusDuration(minutes)
        _state.update {
            if (it.status == TimerStatus.IDLE && it.phase == PomodoroPhase.FOCUS) {
                it.copy(
                    focusDuration = minutes,
                    remainingSeconds = minutes * 60,
                    totalPhaseSeconds = minutes * 60
                )
            } else {
                it.copy(focusDuration = minutes)
            }
        }
    }

    private fun updateShortBreak(minutes: Int) {
        pomodoroPreferences.setShortBreakDuration(minutes)
        _state.update {
            if (it.status == TimerStatus.IDLE && it.phase == PomodoroPhase.SHORT_BREAK) {
                it.copy(
                    shortBreakDuration = minutes,
                    remainingSeconds = minutes * 60,
                    totalPhaseSeconds = minutes * 60
                )
            } else {
                it.copy(shortBreakDuration = minutes)
            }
        }
    }

    private fun updateLongBreak(minutes: Int) {
        pomodoroPreferences.setLongBreakDuration(minutes)
        _state.update {
            if (it.status == TimerStatus.IDLE && it.phase == PomodoroPhase.LONG_BREAK) {
                it.copy(
                    longBreakDuration = minutes,
                    remainingSeconds = minutes * 60,
                    totalPhaseSeconds = minutes * 60
                )
            } else {
                it.copy(longBreakDuration = minutes)
            }
        }
    }

    private fun updateSessionCount(count: Int) {
        val safeCount = count.coerceAtLeast(1)
        pomodoroPreferences.setSessionsBeforeLongBreak(safeCount)
        _state.update { it.copy(sessionsBeforeLongBreak = safeCount) }
    }

    private fun setAutoStartBreaks(enabled: Boolean) {
        pomodoroPreferences.setAutoStartBreaks(enabled)
        _state.update { it.copy(autoStartBreaks = enabled) }
    }

    private fun setAutoStartFocus(enabled: Boolean) {
        pomodoroPreferences.setAutoStartFocus(enabled)
        _state.update { it.copy(autoStartFocus = enabled) }
    }

    private fun linkHabit(habitId: String?) {
        pomodoroPreferences.setLinkedHabitId(habitId)
        _state.update { it.copy(linkedHabitId = habitId) }
    }

    private fun getTodayDateKey(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
    }

    override fun onCleared() {
        super.onCleared()
        if (_state.value.status != TimerStatus.IDLE) {
            saveTimerState()
        }
        timerJob?.cancel()
    }
}
