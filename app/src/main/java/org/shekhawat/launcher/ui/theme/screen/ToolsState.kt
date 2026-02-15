package org.shekhawat.launcher.ui.theme.screen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

/**
 * Global singleton that holds Pomodoro and Timer state so it survives
 * the ToolsSheet being closed and reopened.
 */
object ToolsState {

    // ── Pomodoro ──
    private val _pomodoroRunning = MutableStateFlow(false)
    val pomodoroRunning: StateFlow<Boolean> = _pomodoroRunning.asStateFlow()

    private val _pomodoroStartTime = MutableStateFlow(LocalDateTime.now())
    val pomodoroStartTime: StateFlow<LocalDateTime> = _pomodoroStartTime.asStateFlow()

    private val _pomodoroTotalSeconds = MutableStateFlow(15L * 60)
    val pomodoroTotalSeconds: StateFlow<Long> = _pomodoroTotalSeconds.asStateFlow()

    private val _pomodoroRemaining = MutableStateFlow(15L * 60)
    val pomodoroRemaining: StateFlow<Long> = _pomodoroRemaining.asStateFlow()

    fun startPomodoro(totalSeconds: Long) {
        _pomodoroTotalSeconds.value = totalSeconds
        _pomodoroRemaining.value = totalSeconds
        _pomodoroStartTime.value = LocalDateTime.now()
        _pomodoroRunning.value = true
    }

    fun stopPomodoro(totalSeconds: Long) {
        _pomodoroRunning.value = false
        _pomodoroRemaining.value = totalSeconds
    }

    fun restartPomodoro(totalSeconds: Long) {
        _pomodoroStartTime.value = LocalDateTime.now()
        _pomodoroRemaining.value = totalSeconds
        _pomodoroTotalSeconds.value = totalSeconds
    }

    fun updatePomodoroRemaining(remaining: Long) {
        _pomodoroRemaining.value = remaining
    }

    fun pomodoroCompleted(totalSeconds: Long) {
        _pomodoroRunning.value = false
        _pomodoroRemaining.value = totalSeconds
    }

    // ── Timer (stopwatch) ──
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerStartTime = MutableStateFlow(LocalDateTime.now())
    val timerStartTime: StateFlow<LocalDateTime> = _timerStartTime.asStateFlow()

    private val _timerPausedElapsed = MutableStateFlow(0L)
    val timerPausedElapsed: StateFlow<Long> = _timerPausedElapsed.asStateFlow()

    private val _timerElapsed = MutableStateFlow(0L)
    val timerElapsed: StateFlow<Long> = _timerElapsed.asStateFlow()

    fun startTimer() {
        _timerStartTime.value = LocalDateTime.now()
        _timerRunning.value = true
    }

    fun pauseTimer() {
        _timerPausedElapsed.value = _timerElapsed.value
        _timerRunning.value = false
    }

    fun resetTimer() {
        _timerRunning.value = false
        _timerPausedElapsed.value = 0L
        _timerElapsed.value = 0L
        _timerStartTime.value = LocalDateTime.now()
    }

    fun updateTimerElapsed(elapsed: Long) {
        _timerElapsed.value = elapsed
    }
}
