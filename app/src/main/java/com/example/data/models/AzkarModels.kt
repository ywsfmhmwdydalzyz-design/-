package com.example.data.models

data class ZikrItem(
    val id: String,
    val text: String,
    val targetCount: Int,
    val rewardOrFadl: String,
    val source: String
)

enum class ZikrCategory(val titleArabic: String, val iconName: String) {
    MORNING("أذكار الصباح", "wb_sunny"),
    EVENING("أذكار المساء", "nights_stay"),
    WAKING("أذكار الاستيقاظ", "alarm"),
    SLEEP("أذكار النوم", "bedtime"),
    AFTER_PRAYER("أذكار بعد الصلاة", "mosque"),
    QURAN_SUNNAH_DUAS("أدعية الكتاب والسنة", "auto_stories"),
    TASBEEH("تسبيح واستغفار", "favorite")
}
