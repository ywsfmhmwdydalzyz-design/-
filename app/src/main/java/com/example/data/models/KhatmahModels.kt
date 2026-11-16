package com.example.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

/**
 * نموذج خطة ختمة القرآن الكريم
 */
data class KhatmahPlan(
    val id: String = "active_khatmah",
    val title: String = "الختمة الحالية",
    val dailyPages: Int = 20, // عدد الصفحات المقروءة يومياً (الافتراضي 20 صفحة = جزء يومياً)
    val currentPage: Int = 1, // الصفحة الحالية التي وصل إليها القارئ (1 إلى 604)
    val startPage: Int = 1, // صفحة بداية الختمة (عادة 1)
    val totalPages: Int = 604, // إجمالي صفحات المصحف الشريف
    val startTimestamp: Long = System.currentTimeMillis(),
    val lastReadDate: String = "",
    val pagesReadToday: Int = 0,
    val isCompleted: Boolean = false,
    val completedDate: Long? = null,
    val completedCount: Int = 0 // عدد الختمات السابقة المنجزة
) {
    /**
     * عدد الصفحات المتبقية لإتمام الختمة
     */
    val remainingPages: Int
        get() = maxOf(0, totalPages - currentPage)

    /**
     * النسبة المئوية لإنجاز الختمة (0.0% إلى 100.0%)
     */
    val progressPercentage: Float
        get() = ((currentPage.toFloat() / totalPages.toFloat()) * 100f).coerceIn(0f, 100f)

    /**
     * نسبة الإنجاز كنص منسق (مثال: 45.2%)
     */
    val formattedPercentage: String
        get() = String.format(Locale.US, "%.1f%%", progressPercentage)

    /**
     * عدد الأيام المتبقية للختم بناءً على المعدل اليومي
     */
    val daysRemaining: Int
        get() {
            if (remainingPages <= 0) return 0
            if (dailyPages <= 0) return 0
            return ceil(remainingPages.toDouble() / dailyPages.toDouble()).toInt()
        }

    /**
     * تاريخ الانتهاء المتوقع بالمللي ثانية
     */
    fun getEstimatedCompletionDateMillis(): Long {
        if (daysRemaining <= 0) return System.currentTimeMillis()
        val dayInMillis = 24L * 60L * 60L * 1000L
        return System.currentTimeMillis() + (daysRemaining.toLong() * dayInMillis)
    }

    /**
     * تاريخ الانتهاء المتوقع منسقاً باللغة العربية (مثال: الخميس، 15 أكتوبر 2026)
     */
    fun getFormattedEstimatedDate(): String {
        if (remainingPages <= 0) return "أتممت الختمة بحمد الله! 🎉"
        if (dailyPages <= 0) return "يرجى تحديد الورد اليومي"
        val date = Date(getEstimatedCompletionDateMillis())
        val arabicLocale = Locale("ar")
        val formatter = SimpleDateFormat("EEEE، d MMMM yyyy", arabicLocale)
        return formatter.format(date)
    }

    /**
     * تاريخ البدء منسقاً باللغة العربية
     */
    fun getFormattedStartDate(): String {
        val date = Date(startTimestamp)
        val arabicLocale = Locale("ar")
        val formatter = SimpleDateFormat("d MMMM yyyy", arabicLocale)
        return formatter.format(date)
    }

    /**
     * رقم الجزء الحالي (من 1 إلى 30)
     */
    val currentJuz: Int
        get() = (((currentPage - 1) / 20) + 1).coerceIn(1, 30)

    /**
     * رقم الحزب الحالي (من 1 إلى 60)
     */
    val currentHizb: Int
        get() = (((currentPage - 1) / 10) + 1).coerceIn(1, 60)

    /**
     * حساب تاريخ الانتهاء لعدد صفحات افتراضي (للمعاينة الحية عند تعديل الهدف)
     */
    fun calculateDaysForPages(pages: Int): Int {
        if (remainingPages <= 0) return 0
        if (pages <= 0) return 0
        return ceil(remainingPages.toDouble() / pages.toDouble()).toInt()
    }

    fun calculateFormattedDateForPages(pages: Int): String {
        val days = calculateDaysForPages(pages)
        if (days <= 0) return "مكتمل"
        val date = Date(System.currentTimeMillis() + (days.toLong() * 24L * 60L * 60L * 1000L))
        val arabicLocale = Locale("ar")
        val formatter = SimpleDateFormat("d MMMM yyyy", arabicLocale)
        return formatter.format(date)
    }
}
