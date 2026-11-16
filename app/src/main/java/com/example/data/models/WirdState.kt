package com.example.data.models

enum class WirdUnit(val labelArabic: String) {
    PAGES("صفحات"),
    AYAHS("آيات"),
    JUZ("أجزاء")
}

data class WirdState(
    val target: Int = 10,
    val current: Int = 0,
    val unit: WirdUnit = WirdUnit.PAGES,
    val streakDays: Int = 1,
    val lastCompletedDate: String = ""
)
