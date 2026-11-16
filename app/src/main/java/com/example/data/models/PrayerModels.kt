package com.example.data.models

data class PrayerTimeInfo(
    val nameArabic: String,
    val nameEnglish: String,
    val hour: Int,
    val minute: Int,
    val formattedTime: String,
    val isNext: Boolean = false
)

data class MuadhinVoice(
    val id: String,
    val nameArabic: String,
    val location: String,
    val audioUrl: String
)

data class DayPrayerTimes(
    val dateFormatted: String,
    val hijriDate: String,
    val fajr: PrayerTimeInfo,
    val sunrise: PrayerTimeInfo,
    val dhuhr: PrayerTimeInfo,
    val asr: PrayerTimeInfo,
    val maghrib: PrayerTimeInfo,
    val isha: PrayerTimeInfo
)
