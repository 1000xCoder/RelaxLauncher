package org.shekhawat.launcher.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.shekhawat.launcher.SharedPrefManager

class PomodoroViewModel(private val sharedPrefManager: SharedPrefManager) : ViewModel() {

    private val _pomodoroTime = MutableStateFlow(getPomodoroTime())
    val pomodoroTimeStateFlow: Flow<Long> = _pomodoroTime.asStateFlow()

    fun getPomodoroTime(): Long {
        return sharedPrefManager.getLong("POMODORO_TIME", 15 * 60) // seconds
    }

    fun setPomodoroTime(timeInSeconds: Long) {
        sharedPrefManager.saveLong("POMODORO_TIME", timeInSeconds)
        _pomodoroTime.value = timeInSeconds
    }
}