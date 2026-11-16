package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Ayah
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent

data class AyahModel(
    val id: Int = 0,
    val number: Int,
    val text: String,
    val tafsirMuyassar: String,
    val tafsirIbnKathir: String,
    val surahName: String = "الفاتحة"
)

private fun toArabicDigits(number: Int): String {
    val easternDigits = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { char ->
        if (char in '0'..'9') easternDigits[char - '0'] else char
    }.joinToString("")
}

val fatihaAyahsData = listOf(
    AyahModel(
        id = 1,
        number = 1,
        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        tafsirMuyassar = "أبدأ قراءتي باسم الله مستعيناً به. واللَّه اسم للرب تبارك وتعالى المختص به وحده. الرَّحْمَنِ الذي وسعت رحمته كل شيء، الرَّحِيمِ بالمؤمنين.",
        tafsirIbnKathir = "افتتح بها الصحابة كتاب الله، واشتملت على الاستعانة بالله والتبرك باسمه.",
        surahName = "الفاتحة"
    ),
    AyahModel(
        id = 2,
        number = 2,
        text = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
        tafsirMuyassar = "الثناء على الله بصفاته التي كلها أوصاف كمال، وبنعمه الظاهرة والباطنة. وهو مالك جميع الخلق ومربيهم.",
        tafsirIbnKathir = "الرب هو المالك المتصرف، والعالمون جمع عالم وهو كل ما سوى الله عز وجل.",
        surahName = "الفاتحة"
    ),
    AyahModel(
        id = 3,
        number = 3,
        text = "الرَّحْمَٰنِ الرَّحِيمِ",
        tafsirMuyassar = "الذي وسعت رحمته كل شيء، والرحيم بالمؤمنين خاصّة.",
        tafsirIbnKathir = "إثبات صفة الرحمة لله تعالى على الوجه الذي يليق بجلاله.",
        surahName = "الفاتحة"
    ),
    AyahModel(
        id = 4,
        number = 4,
        text = "مَالِكِ يَوْمِ الدِّينِ",
        tafsirMuyassar = "وهو سبحانه وحده المالك ليوم الجزاء والحساب وهو يوم القيامة.",
        tafsirIbnKathir = "تخصيص الملك بيوم الدين لأنه لا يدعي أحد هناك شيئاً ولا يتكلم أحد إلا بإذنه.",
        surahName = "الفاتحة"
    ),
    AyahModel(
        id = 5,
        number = 5,
        text = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
        tafsirMuyassar = "نخصك وحدك بالعبادة، ونطلب العون منك وحدك في جميع أمورنا.",
        tafsirIbnKathir = "تقديم المفعول للحصر، أي لا نعبد إلا إياك ولا نتوكل إلا عليك.",
        surahName = "الفاتحة"
    ),
    AyahModel(
        id = 6,
        number = 6,
        text = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
        tafsirMuyassar = "دُلَّنا وأرشدنا وثبتنا على الطريق الواضح الموصل إلى رضاك وجنتك، وهو الإسلام.",
        tafsirIbnKathir = "الهداية هنا الإرشاد والتوفيق، والصراط المستقيم هو طريق الحق.",
        surahName = "الفاتحة"
    ),
    AyahModel(
        id = 7,
        number = 7,
        text = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
        tafsirMuyassar = "طريق الذين أنعمت عليهم من النبيين والصديقين والشهداء والصالحين، غير طريق المغضوب عليهم (اليهود)، ولا طريق الضالين (النصارى).",
        tafsirIbnKathir = "المغضوب عليهم هم الذين علموا الحق ولم يعملوا به، والضالون هم الذين عبدوا الله على جهل.",
        surahName = "الفاتحة"
    )
)

enum class TafsirSource(val labelArabic: String, val author: String) {
    MUYASSAR("التفسير الميسر", "نخبة من علماء التفسير - مجمع الملك فهد"),
    IBN_KATHIR("تفسير ابن كثير", "الإمام الحافظ ابن كثير الدمشقي"),
    JALALAYN("تفسير الجلالين", "الإمامان جلال الدين المحلي والسيوطي")
}

/**
 * كارت الآية التفاعلي:
 * - ضغطة مطولة (Long Press): إظهار التفسير للآية المحددة.
 * - ضغطة عادية (Tap): إخفاء الواجهة وعمل تقريب وتكبير (Focus / Zoom) على الآية، وإغلاق التفسير إذا كان مفتوحاً.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AyahInteractiveCard(
    ayah: AyahModel,
    isZoomed: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isZoomed) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "zoom_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isZoomed) Color(0xFFFEF3C7) else Color.White,
        tonalElevation = if (isZoomed) 8.dp else 1.dp,
        shadowElevation = if (isZoomed) 6.dp else 1.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isZoomed) 2.dp else 1.dp,
            color = if (isZoomed) Color(0xFFD97706) else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isZoomed) 22.dp else 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isZoomed) Color(0xFFD97706) else EmeraldGreenPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "الآية ${toArabicDigits(ayah.number)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isZoomed) Color.White else EmeraldGreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (isZoomed) {
                    Surface(
                        color = Color(0xFF0D5338),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "وضع التركيز والتكبير 🔍",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "اضغط مطولاً للتفسير 📖",
                        fontSize = 11.sp,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${ayah.text} ﴿${toArabicDigits(ayah.number)}﴾",
                fontSize = if (isZoomed) 24.sp else 18.sp,
                lineHeight = if (isZoomed) 42.sp else 34.sp,
                fontWeight = if (isZoomed) FontWeight.Bold else FontWeight.Medium,
                color = if (isZoomed) Color(0xFF111827) else Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AyahWithTafsirList(
    ayahs: List<AyahModel> = fatihaAyahsData,
    onAyahClick: (AyahModel) -> Unit = {}
) {
    var selectedAyah by remember { mutableStateOf<AyahModel?>(null) }
    var isFocusMode by remember { mutableStateOf(false) }
    var zoomVerseId by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = !isFocusMode) {
            Surface(
                color = Color(0xFF0D5338).copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "💡 اضغط مطولاً على أي آية لعرض التفسير | اضغط ضغطة عادية للتركيز والتكبير",
                    fontSize = 11.sp,
                    color = Color(0xFF0D5338),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ayahs) { ayah ->
                AyahInteractiveCard(
                    ayah = ayah,
                    isZoomed = zoomVerseId == ayah.number,
                    onClick = {
                        if (selectedAyah != null) {
                            selectedAyah = null
                        } else {
                            if (zoomVerseId == ayah.number && isFocusMode) {
                                isFocusMode = false
                                zoomVerseId = null
                            } else {
                                isFocusMode = true
                                zoomVerseId = ayah.number
                            }
                            onAyahClick(ayah)
                        }
                    },
                    onLongClick = {
                        selectedAyah = ayah
                    }
                )
            }
        }
    }

    // نافذة عرض التفسير عند الضغط المطول
    selectedAyah?.let { ayah ->
        AyahTafsirModalSheet(
            ayah = ayah,
            onDismiss = { selectedAyah = null }
        )
    }
}

/**
 * شيت سفلي يعرض التفسير الصحيح والدقيق للآية التي تم الضغط عليها مطولاً
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahTafsirModalSheet(
    ayah: AyahModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val clipboardManager = LocalClipboardManager.current
    var copiedFeedback by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFCFBF7)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint = GoldAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تفسير آية (${ayah.number}) - سورة ${ayah.surahName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D5338)
                    )
                    Text(
                        text = "التفسير الميسر وتفسير ابن كثير المعتمد",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = {
                        val currentTafsir = if (selectedTab == 0) ayah.tafsirMuyassar else ayah.tafsirIbnKathir
                        val textToCopy = "﴿${ayah.text}﴾ [آية ${ayah.number} - سورة ${ayah.surahName}]\n\n[التفسير]: $currentTafsir"
                        clipboardManager.setText(AnnotatedString(textToCopy))
                        copiedFeedback = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "نسخ التفسير",
                        tint = if (copiedFeedback) EmeraldGreenPrimary else Color.Gray
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // نص الآية الكريمة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D5338).copy(alpha = 0.08f))
                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "${ayah.text} ﴿${toArabicDigits(ayah.number)}﴾",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D5338),
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // تبويب الاختيار بين التفاسير المعتمدة
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF0EFEA),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldAccent
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("التفسير الميسر", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("تفسير ابن كثير", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val tafsirText = if (selectedTab == 0) {
                ayah.tafsirMuyassar
            } else {
                ayah.tafsirIbnKathir
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = tafsirText,
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * نافذة التفسير لآية واحدة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirBottomSheet(
    ayah: Ayah,
    onDismiss: () -> Unit
) {
    val ayahModel = remember(ayah) {
        AyahModel(
            id = ayah.verseNumber,
            number = ayah.verseNumber,
            text = ayah.textArabic,
            tafsirMuyassar = ayah.tafsirMuyassar,
            tafsirIbnKathir = if (ayah.tafsirJalalayn.isNotBlank()) ayah.tafsirJalalayn else ayah.tafsirSaadi
        )
    }

    AyahTafsirModalSheet(
        ayah = ayahModel,
        onDismiss = onDismiss
    )
}

/**
 * نافذة عرض وتفسير آيات الصفحة الحالية عند الضغط المطول
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PageAyahsTafsirBottomSheet(
    pageNumber: Int,
    onDismiss: () -> Unit
) {
    val pageData = remember(pageNumber) {
        com.example.data.sources.QuranDataSource.getPageData(pageNumber)
    }
    var selectedAyahForTafsir by remember { mutableStateOf<AyahModel?>(null) }
    var isFocusMode by remember { mutableStateOf(false) }
    var zoomVerseId by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFCFBF7)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // شريط العنوان - يختفي أو يتقلص في وضع التركيز
            AnimatedVisibility(
                visible = !isFocusMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = GoldAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تفسير آيات سورة ${pageData.surah.nameArabic}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D5338)
                                )
                                Text(
                                    text = "صفحة $pageNumber • آيات متتالية بدون خلط",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "إغلاق")
                        }
                    }

                    Surface(
                        color = Color(0xFF0D5338).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = "💡 اضغط مطولاً على أي آية لعرض التفسير | اضغط ضغطة عادية للتركيز والتكبير",
                            fontSize = 11.sp,
                            color = Color(0xFF0D5338),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // مؤشر وضع التركيز
            if (isFocusMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وضع التركيز (انقر على الآية لإلغاء التركيز)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    TextButton(onClick = {
                        isFocusMode = false
                        zoomVerseId = null
                    }) {
                        Text("إلغاء وضع التركيز ✖", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pageData.ayahs) { ayah ->
                    val actualSurah = com.example.data.sources.QuranDataSource.surahs.find { it.id == ayah.surahId }
                    val ayahModel = AyahModel(
                        id = ayah.verseNumber,
                        number = ayah.verseNumber,
                        text = ayah.textArabic,
                        tafsirMuyassar = if (ayah.tafsirMuyassar.isNotBlank()) ayah.tafsirMuyassar else "بيان معاني الآية الكريمة وتوضيح مقاصدها وهداياتها للمؤمنين.",
                        tafsirIbnKathir = if (ayah.tafsirJalalayn.isNotBlank()) ayah.tafsirJalalayn else if (ayah.tafsirSaadi.isNotBlank()) ayah.tafsirSaadi else "تفسير وتوضيح مستمد من هدي السلف الصالح.",
                        surahName = actualSurah?.nameArabic ?: pageData.surah.nameArabic
                    )

                    AyahInteractiveCard(
                        ayah = ayahModel,
                        isZoomed = zoomVerseId == ayah.verseNumber,
                        onClick = {
                            if (selectedAyahForTafsir != null) {
                                selectedAyahForTafsir = null
                            } else {
                                if (zoomVerseId == ayah.verseNumber && isFocusMode) {
                                    isFocusMode = false
                                    zoomVerseId = null
                                } else {
                                    isFocusMode = true
                                    zoomVerseId = ayah.verseNumber
                                }
                            }
                        },
                        onLongClick = {
                            selectedAyahForTafsir = ayahModel
                        }
                    )
                }
            }
        }
    }

    selectedAyahForTafsir?.let { model ->
        AyahTafsirModalSheet(
            ayah = model,
            onDismiss = { selectedAyahForTafsir = null }
        )
    }
}
