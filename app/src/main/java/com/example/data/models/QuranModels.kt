package com.example.data.models

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val type: String, // مكية / مدنية
    val totalVerses: Int,
    val pageNumber: Int = 1
)

data class Ayah(
    val surahId: Int,
    val verseNumber: Int,
    val textArabic: String,
    val tafsirJalalayn: String,
    val tafsirMuyassar: String,
    val tafsirSaadi: String
)

data class Reciter(
    val id: String,
    val nameArabic: String,
    val style: String,
    val baseUrl: String,
    val fallbackBaseUrl: String? = null
)

enum class QuranDisplayMode {
    PAGES,          // صفحات المصحف المصورة
    VERSES_TAFSIR   // قراءة الآيات والتفسير المتتالي
}

data class QuranPageData(
    val pageNumber: Int,
    val juzNumber: Int,
    val hizbNumber: Int,
    val surah: Surah,
    val ayahs: List<Ayah>,
    val isSurahStart: Boolean = false
)

data class MushafToken(
    val text: String,
    val isEndMarker: Boolean = false,
    val verseKey: String? = null,
    val surahId: Int = 1,
    val verseNumber: Int = 1
)

data class MushafSurahHeader(
    val surahId: Int,
    val startLine: Int
)

data class MushafPageLayout(
    val pageNumber: Int,
    val juzNumber: Int,
    val hizbNumber: Int,
    val rubElHizbNumber: Int,
    val sajdahVerses: List<String> = emptyList(),
    val surahStarts: List<MushafSurahHeader> = emptyList(),
    val lines: Map<Int, List<MushafToken>> = emptyMap(),
    val isVerified15LineLayout: Boolean = false
)

data class MushafQuarter(
    val rubNumber: Int,       // 1..240
    val juzNumber: Int,       // 1..30
    val hizbNumber: Int,      // 1..60
    val quarterInHizb: Int,   // 1..4 (ربع، نصف، ثلاثة أرباع، رأس الحزب)
    val pageNumber: Int,
    val surahId: Int,
    val verseNumber: Int,
    val previewText: String
)

data class QuranBookmark(
    val id: String = java.util.UUID.randomUUID().toString(),
    val pageNumber: Int,
    val surahName: String,
    val surahId: Int,
    val juzNumber: Int,
    val hizbNumber: Int,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
