package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.WirdState
import com.example.data.models.WirdUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WirdStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sakina_wird_prefs", Context.MODE_PRIVATE)

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val _wirdState = MutableStateFlow(loadState())
    val wirdState: StateFlow<WirdState> = _wirdState.asStateFlow()

    private fun loadState(): WirdState {
        val target = prefs.getInt("wird_target", 10)
        val unitName = prefs.getString("wird_unit", WirdUnit.PAGES.name) ?: WirdUnit.PAGES.name
        val unit = try { WirdUnit.valueOf(unitName) } catch (_: Exception) { WirdUnit.PAGES }
        val lastDate = prefs.getString("wird_last_date", "") ?: ""
        val streak = prefs.getInt("wird_streak", 1)

        val current = if (lastDate == todayDateString) {
            prefs.getInt("wird_current", 0)
        } else {
            0
        }

        return WirdState(
            target = target,
            current = current,
            unit = unit,
            streakDays = streak,
            lastCompletedDate = lastDate
        )
    }

    private fun saveState(state: WirdState) {
        prefs.edit()
            .putInt("wird_target", state.target)
            .putInt("wird_current", state.current)
            .putString("wird_unit", state.unit.name)
            .putInt("wird_streak", state.streakDays)
            .putString("wird_last_date", todayDateString)
            .apply()
        _wirdState.value = state
    }

    fun increment() {
        val current = _wirdState.value
        val newCount = current.current + 1
        val updated = current.copy(
            current = newCount,
            lastCompletedDate = todayDateString
        )
        saveState(updated)
    }

    fun decrement() {
        val current = _wirdState.value
        if (current.current > 0) {
            val updated = current.copy(
                current = current.current - 1,
                lastCompletedDate = todayDateString
            )
            saveState(updated)
        }
    }

    fun setTarget(target: Int, unit: WirdUnit) {
        val current = _wirdState.value
        val updated = current.copy(
            target = maxOf(1, target),
            unit = unit
        )
        saveState(updated)
    }

    fun resetDaily() {
        val current = _wirdState.value
        val updated = current.copy(current = 0)
        saveState(updated)
    }
}
