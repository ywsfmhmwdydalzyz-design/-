package com.example.data.models

data class HijriDate(
    val day: Int,
    val month: Int, // 1 to 12
    val year: Int,
    val monthNameArabic: String,
    val dayNameArabic: String,
    val gregorianDay: Int,
    val gregorianMonth: Int, // 1 to 12
    val gregorianYear: Int,
    val gregorianFormatted: String,
    val occasions: List<IslamicOccasion> = emptyList(),
    val isWhiteDay: Boolean = false, // 13, 14, 15
    val isFastingSunnah: Boolean = false // Monday or Thursday
) {
    val fullHijriFormatted: String
        get() = "$dayNameArabic، $day $monthNameArabic $year هـ"
}

data class IslamicOccasion(
    val id: String,
    val title: String,
    val description: String,
    val hijriDay: Int,
    val hijriMonth: Int, // 1 to 12
    val recommendedActs: String,
    val isMajor: Boolean = true
)

data class HijriMonthInfo(
    val monthNumber: Int,
    val monthNameArabic: String,
    val year: Int,
    val daysCount: Int,
    val days: List<HijriDate>
)
