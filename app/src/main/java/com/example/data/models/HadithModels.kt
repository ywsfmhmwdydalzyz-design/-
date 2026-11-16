package com.example.data.models

data class NawawiHadith(
    val id: Int,
    val title: String,
    val narrator: String, // e.g. "عن أمير المؤمنين أبي حفص عمر بن الخطاب رضي الله عنه"
    val matn: String,     // Full text with tashkeel
    val reference: String,// e.g. "رواه البخاري ومسلم"
    val explanation: String, // شرح ميسر
    val benefits: List<String> // الفوائد المستخلصة
)

data class DailyHadith(
    val title: String,
    val matn: String,
    val narrator: String,
    val reference: String,
    val benefit: String
)

data class SakinaMessage(
    val title: String,
    val message: String,
    val verseOrQuote: String
)

data class DailyAyah(
    val surahName: String,
    val ayahNumber: Int,
    val ayahText: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val reflection: String,
    val theme: String = "تدبر وتأمل"
)
