package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sources.QuranDataSource

// نموذج بيانات نتائج البحث
data class SearchResult(
    val surahName: String,
    val ayahText: String,
    val ayahNumber: Int,
    val pageNumber: Int,
    val isSurahTitle: Boolean = false
)

/**
 * دالة مساعدة لتنظيف النص العربي من الحركات وتوحيد الألف والتاء المربوطة
 * لضمان البحث الفوري فائق السرعة بدون تأثر بالحركات
 */
fun normalizeArabic(text: String): String {
    return text
        .replace(Regex("[\\u064B-\\u065F\\u0670]"), "") // إزالة التشكيل والألف الخنجرية
        .replace("أ", "ا")
        .replace("إ", "ا")
        .replace("آ", "ا")
        .replace("ٱ", "ا")
        .replace("ة", "ه")
        .replace("ى", "ي")
        .trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSearchScreen(
    onBackClick: () -> Unit = {},
    onResultClick: (pageNumber: Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // بناء قاعدة بيانات البحث الشاملة للقرآن الكريم (السور والآيات ومواقعها) أوفلاين بالكامل
    val allAyahs = remember {
        val list = mutableListOf<SearchResult>()

        // 1. فهرس السور الـ 114 كاملة
        QuranDataSource.surahs.forEach { surah ->
            list.add(
                SearchResult(
                    surahName = "سورة ${surah.nameArabic}",
                    ayahText = "سورة ${surah.nameArabic} (${surah.type}) - عدد آياتها ${surah.totalVerses} آية - تقع في الجزء ${QuranDataSource.getJuzForPage(surah.pageNumber)}",
                    ayahNumber = 1,
                    pageNumber = surah.pageNumber,
                    isSurahTitle = true
                )
            )
        }

        // 2. آيات القرآن الكريم الكاملة الـ 6236 من قاعدة البيانات المحلية أوفلاين
        val offlineAyahs = com.example.data.sources.OfflineQuranManager.getAllSearchAyahs()
        if (offlineAyahs.isNotEmpty()) {
            list.addAll(offlineAyahs)
        } else {
            // آيات من ayahDatabase عند عدم تحميل الأوفلاين
            QuranDataSource.ayahDatabase.forEach { (surahId, ayahsList) ->
                val surah = QuranDataSource.surahs.find { it.id == surahId }
                if (surah != null) {
                    ayahsList.forEach { ayah ->
                        val page = when {
                            surahId == 1 -> 1
                            surahId == 2 && ayah.verseNumber in 1..5 -> 2
                            surahId == 2 && ayah.verseNumber in 6..16 -> 3
                            surahId == 2 && ayah.verseNumber == 255 -> 42
                            surahId == 2 && ayah.verseNumber in 285..286 -> 49
                            surahId == 18 && ayah.verseNumber in 1..10 -> 293
                            surahId == 36 && ayah.verseNumber in 1..12 -> 440
                            surahId == 67 && ayah.verseNumber in 1..10 -> 562
                            surahId == 112 -> 604
                            surahId == 113 -> 604
                            surahId == 114 -> 604
                            else -> surah.pageNumber
                        }

                        list.add(
                            SearchResult(
                                surahName = "سورة ${surah.nameArabic}",
                                ayahText = ayah.textArabic,
                                ayahNumber = ayah.verseNumber,
                                pageNumber = page,
                                isSurahTitle = false
                            )
                        )
                    }
                }
            }
        }
        list
    }

    // تصفية النتائج بناءً على كلمة البحث الفوري بدون بطء
    val filteredResults = remember(searchQuery) {
        val queryClean = normalizeArabic(searchQuery.trim())
        if (queryClean.isEmpty()) {
            emptyList()
        } else {
            // تحقق إذا كان المستخدم يبحث برقم صفحة مباشرة
            val asPageNumber = queryClean.toIntOrNull()
            if (asPageNumber != null && asPageNumber in 1..604) {
                val surahAtPage = QuranDataSource.getSurahForPage(asPageNumber)
                listOf(
                    SearchResult(
                        surahName = "سورة ${surahAtPage.nameArabic}",
                        ayahText = "الانتقال المباشر إلى الصفحة رقم $asPageNumber في المصحف الشريف (الجزء ${QuranDataSource.getJuzForPage(asPageNumber)})",
                        ayahNumber = 1,
                        pageNumber = asPageNumber,
                        isSurahTitle = true
                    )
                ) + allAyahs.filter {
                    normalizeArabic(it.ayahText).contains(queryClean) ||
                    normalizeArabic(it.surahName).contains(queryClean)
                }.take(30)
            } else {
                allAyahs.filter {
                    normalizeArabic(it.ayahText).contains(queryClean) ||
                    normalizeArabic(it.surahName).contains(queryClean)
                }.take(40) // حصر النتائج لتسريع العرض بسلاسة
            }
        }
    }

    Scaffold(
        modifier = Modifier.testTag("quran_search_screen"),
        topBar = {
            TopAppBar(
                title = { Text("البحث في القرآن الكريم", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("search_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D5338))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // شريط إدخال نص البحث
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_input"),
                placeholder = { Text("اكتب كلمة، آية، أو اسم سورة (مثل: الكهف، الرحمن، الفاتحة)...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = Color(0xFF0D5338)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D5338),
                    unfocusedBorderColor = Color(0xFFD4AF37).copy(alpha = 0.5f),
                    cursorColor = Color(0xFF0D5338)
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (searchQuery.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عدد النتائج: ${filteredResults.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0D5338)
                    )
                    Text(
                        text = "اضغط على الآية لفتح صفحتها بالمصحف",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // كلمات بحث مقترحة للوصول السريع
                Text(
                    text = "كلمات بحث شائعة:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("الفاتحة", "الكهف", "آية الكرسي", "الملك", "الرحمن", "يس").forEach { suggestion ->
                        SuggestionChip(
                            onClick = { searchQuery = suggestion },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFFF4EEDD)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // عرض نتائج البحث
            if (filteredResults.isEmpty() && searchQuery.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "لا توجد نتائج تطابق بحثك",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تأكد من كتابة الكلمة بشكل صحيح أو جرب البحث باسم السورة",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredResults) { result ->
                        SearchResultCard(
                            result = result,
                            searchQuery = searchQuery,
                            onClick = { onResultClick(result.pageNumber) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    result: SearchResult,
    searchQuery: String = "",
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_result_card_${result.pageNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFBF7)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (result.isSurahTitle) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (result.isSurahTitle) result.surahName else "${result.surahName} - آية ${result.ayahNumber}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D5338),
                        fontSize = 14.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFD4AF37).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "صفحة ${result.pageNumber}",
                        color = Color(0xFF8C6D1F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // عرض نص الآية مع إبراز الكلمة المبحوث عنها (Highlight)
            HighlightedAyahText(
                fullText = result.ayahText,
                query = searchQuery,
                fontSize = 15.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * مكون لعرض النص القرآني مع تظليل وإبراز كلمة البحث بخلفية ذهبية مميزة
 */
@Composable
fun HighlightedAyahText(
    fullText: String,
    query: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    val cleanQuery = normalizeArabic(query.trim())
    val cleanFullText = normalizeArabic(fullText)

    val annotated = remember(fullText, query) {
        buildAnnotatedString {
            if (cleanQuery.isEmpty() || !cleanFullText.contains(cleanQuery)) {
                append(fullText)
            } else {
                // تقسيم الكلمات وإبراز الكلمة المطابقة
                val words = fullText.split(" ")
                words.forEachIndexed { index, word ->
                    val cleanWord = normalizeArabic(word)
                    if (cleanWord.contains(cleanQuery)) {
                        withStyle(
                            SpanStyle(
                                color = Color(0xFF0D5338),
                                background = Color(0xFFD4AF37).copy(alpha = 0.35f),
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append(word)
                        }
                    } else {
                        append(word)
                    }
                    if (index < words.size - 1) append(" ")
                }
            }
        }
    }

    Text(
        text = annotated,
        fontSize = fontSize,
        color = Color(0xFF1E293B),
        textAlign = TextAlign.Right,
        lineHeight = 24.sp,
        modifier = modifier
    )
}
