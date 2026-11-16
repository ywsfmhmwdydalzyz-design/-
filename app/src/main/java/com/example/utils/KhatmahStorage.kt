package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.KhatmahPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KhatmahStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sakina_khatmah_prefs", Context.MODE_PRIVATE)

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val _khatmahState = MutableStateFlow(loadState())
    val khatmahState: StateFlow<KhatmahPlan> = _khatmahState.asStateFlow()

    private fun loadState(): KhatmahPlan {
        val id = prefs.getString("khatmah_id", "active_khatmah") ?: "active_khatmah"
        val title = prefs.getString("khatmah_title", "الختمة الحالية") ?: "الختمة الحالية"
        val dailyPages = prefs.getInt("khatmah_daily_pages", 20)
        val currentPage = prefs.getInt("khatmah_current_page", 1).coerceIn(1, 604)
        val startPage = prefs.getInt("khatmah_start_page", 1).coerceIn(1, 604)
        val totalPages = prefs.getInt("khatmah_total_pages", 604)
        val startTimestamp = prefs.getLong("khatmah_start_timestamp", System.currentTimeMillis())
        val lastDate = prefs.getString("khatmah_last_date", "") ?: ""
        val isCompleted = prefs.getBoolean("khatmah_is_completed", false)
        val completedCount = prefs.getInt("khatmah_completed_count", 0)

        val pagesReadToday = if (lastDate == todayDateString) {
            prefs.getInt("khatmah_pages_today", 0)
        } else {
            0
        }

        return KhatmahPlan(
            id = id,
            title = title,
            dailyPages = dailyPages,
            currentPage = currentPage,
            startPage = startPage,
            totalPages = totalPages,
            startTimestamp = startTimestamp,
            lastReadDate = lastDate,
            pagesReadToday = pagesReadToday,
            isCompleted = isCompleted,
            completedCount = completedCount
        )
    }

    private fun saveState(plan: KhatmahPlan) {
        prefs.edit()
            .putString("khatmah_id", plan.id)
            .putString("khatmah_title", plan.title)
            .putInt("khatmah_daily_pages", plan.dailyPages)
            .putInt("khatmah_current_page", plan.currentPage)
            .putInt("khatmah_start_page", plan.startPage)
            .putInt("khatmah_total_pages", plan.totalPages)
            .putLong("khatmah_start_timestamp", plan.startTimestamp)
            .putString("khatmah_last_date", todayDateString)
            .putInt("khatmah_pages_today", plan.pagesReadToday)
            .putBoolean("khatmah_is_completed", plan.isCompleted)
            .putInt("khatmah_completed_count", plan.completedCount)
            .apply()
        _khatmahState.value = plan
    }

    /**
     * تحديث عدد الصفحات اليومية المستهدفة
     */
    fun setDailyPages(pages: Int) {
        val current = _khatmahState.value
        val updated = current.copy(dailyPages = maxOf(1, pages))
        saveState(updated)
    }

    /**
     * تحديث الصفحة الحالية للختمة
     */
    fun setCurrentPage(page: Int) {
        val current = _khatmahState.value
        val clampedPage = page.coerceIn(1, 604)
        val pagesAdded = maxOf(0, clampedPage - current.currentPage)
        val isNowCompleted = clampedPage >= 604
        val newCompletedCount = if (isNowCompleted && !current.isCompleted) current.completedCount + 1 else current.completedCount

        val updated = current.copy(
            currentPage = clampedPage,
            pagesReadToday = current.pagesReadToday + pagesAdded,
            isCompleted = isNowCompleted,
            completedCount = newCompletedCount,
            lastReadDate = todayDateString
        )
        saveState(updated)
    }

    /**
     * إضافة صفحات مقروءة إلى التقدم
     */
    fun addPagesRead(pagesToAdd: Int) {
        val current = _khatmahState.value
        val newPage = (current.currentPage + pagesToAdd).coerceIn(1, 604)
        val isNowCompleted = newPage >= 604
        val newCompletedCount = if (isNowCompleted && !current.isCompleted) current.completedCount + 1 else current.completedCount

        val updated = current.copy(
            currentPage = newPage,
            pagesReadToday = current.pagesReadToday + maxOf(0, pagesToAdd),
            isCompleted = isNowCompleted,
            completedCount = newCompletedCount,
            lastReadDate = todayDateString
        )
        saveState(updated)
    }

    /**
     * بدء ختمة جديدة
     */
    fun startNewKhatmah(title: String = "ختمة جديدة", dailyPages: Int = 20, startPage: Int = 1) {
        val current = _khatmahState.value
        val newPlan = KhatmahPlan(
            id = "khatmah_${System.currentTimeMillis()}",
            title = title,
            dailyPages = maxOf(1, dailyPages),
            currentPage = startPage.coerceIn(1, 604),
            startPage = startPage.coerceIn(1, 604),
            totalPages = 604,
            startTimestamp = System.currentTimeMillis(),
            lastReadDate = todayDateString,
            pagesReadToday = 0,
            isCompleted = false,
            completedCount = current.completedCount
        )
        saveState(newPlan)
    }

    /**
     * تعليم الختمة الحالية كمكتملة
     */
    fun markCompleted() {
        val current = _khatmahState.value
        val updated = current.copy(
            currentPage = 604,
            isCompleted = true,
            completedCount = current.completedCount + 1
        )
        saveState(updated)
    }

    /**
     * إعادة ضبط الختمة الحالية إلى البداية
     */
    fun resetCurrentKhatmah() {
        val current = _khatmahState.value
        val updated = current.copy(
            currentPage = 1,
            pagesReadToday = 0,
            isCompleted = false,
            startTimestamp = System.currentTimeMillis()
        )
        saveState(updated)
    }
}
