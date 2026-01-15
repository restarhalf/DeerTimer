package me.restarhalf.deer.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TimerState(
    val isSessionActive: Boolean = false,
    val accumulatedSeconds: Int = 0,
    val startTimeMillis: Long = 0L,
    val isRunning: Boolean = false
)

object TimerRepository {
    private const val PREFS_NAME = "timer_prefs"
    private const val KEY_IS_SESSION_ACTIVE = "is_session_active"
    private const val KEY_ACCUMULATED_SECONDS = "accumulated_seconds"
    private const val KEY_START_TIME_MILLIS = "start_time_millis"
    private const val KEY_IS_RUNNING = "is_running"

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _timerState.value = TimerState(
            isSessionActive = prefs.getBoolean(KEY_IS_SESSION_ACTIVE, false),
            accumulatedSeconds = prefs.getInt(KEY_ACCUMULATED_SECONDS, 0),
            startTimeMillis = prefs.getLong(KEY_START_TIME_MILLIS, 0L),
            isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        )
    }

    fun setSessionActive(context: Context, active: Boolean) {
        _timerState.value = _timerState.value.copy(isSessionActive = active)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_IS_SESSION_ACTIVE, active)
        }
    }

    fun updateState(
        context: Context,
        accumulatedSeconds: Int,
        startTimeMillis: Long,
        isRunning: Boolean
    ) {
        _timerState.value = _timerState.value.copy(
            accumulatedSeconds = accumulatedSeconds,
            startTimeMillis = startTimeMillis,
            isRunning = isRunning
        )
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putInt(KEY_ACCUMULATED_SECONDS, accumulatedSeconds)
            putLong(KEY_START_TIME_MILLIS, startTimeMillis)
            putBoolean(KEY_IS_RUNNING, isRunning)
        }
    }

    fun clear(context: Context) {
        _timerState.value = TimerState()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { clear() }
    }
}
