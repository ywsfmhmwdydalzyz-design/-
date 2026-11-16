package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.models.MushafPageLayout
import com.example.data.models.MushafToken
import com.example.data.models.Surah
import com.example.data.sources.OfflineQuranManager
import com.example.data.sources.QuranDataSource
import com.example.ui.screens.toEasternArabicDigits
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.KfgqpcUthmanicHafsFontFamily
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// ألوان ورق المصحف العاجي الهادئ والوضع الليلي المريح للعين
val QuranPageBgLight = Color(0xFFFAF3E0)
val QuranPageInnerLight = Color(0xFFFDF8EB)
val QuranPageBgDark = Color(0xFF0E1713)
val QuranPageInnerDark = Color(0xFF13201A)

private val MushafGoldLight = Color(0xFFC59B38)
private val MushafGoldDark = Color(0xFFD4AF37)
private val MushafEmeraldLight = Color(0xFF0D5338)
private val MushafEmeraldDark = Color(0xFF3CA374)
private val MushafInkLight = Color(0xFF1A1916)
private val MushafInkDark = Color(0xFFF5EFE0)

/**
 * عارض صفحة مصحف المدينة المنورة الاحترافي
 * - يعتمد خط مجمع الملك فهد الرسمي (KFGQPC Uthmanic Script Hafs)
 * - يدعم التخطيط الحقيقي ذا الـ 15 سطراً الموثق لكل صفحة (من الصفحة 1 إلى 604)
 * - خلفية ورقية عاجية هادئة مع إطار زخرفي إسلامي ذهبي وأخضر ولوحات السور وأرقام الآيات الدائرية
 * - يدعم الهواتف والأجهزة اللوحية والكمبيوتر، وتغيير حجم العرض دون تشويه النص، والوضع الليلي، وملء الشاشة
 */
@Composable
fun MadaniQuranPageView(
    pageNumber: Int,
    isDarkMode: Boolean = false,
    displayScale: Float = 1.0f,
    useScannedImages: Boolean = false,
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    onToggleBars: () -> Unit = {}
) {
    val safePage = pageNumber.coerceIn(1, 604)
    val context = LocalContext.current

    var zoomScale by remember(safePage) { mutableFloatStateOf(1f) }
    var offset by remember(safePage) { mutableStateOf(Offset.Zero) }

    val bgColor = if (isDarkMode) QuranPageBgDark else QuranPageBgLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("madani_full_page_$safePage"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(safePage) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (zoomScale * zoom).coerceIn(1f, 3.2f)
                        zoomScale = newScale
                        if (newScale > 1.02f) {
                            val maxX = (size.width * (newScale - 1f)) / 2f
                            val maxY = (size.height * (newScale - 1f)) / 2f
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                            )
                        } else {
                            zoomScale = 1f
                            offset = Offset.Zero
                        }
                    }
                }
                .pointerInput(safePage) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (zoomScale > 1.05f) {
                                zoomScale = 1f
                                offset = Offset.Zero
                            } else {
                                zoomScale = 1.75f
                            }
                        },
                        onTap = {
                            onToggleBars()
                        }
                    )
                }
                .graphicsLayer(
                    scaleX = zoomScale,
                    scaleY = zoomScale,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentAlignment = Alignment.Center
        ) {
            if (useScannedImages) {
                ScannedImageOrFallbackMushaf(
                    safePage = safePage,
                    isDarkMode = isDarkMode,
                    displayScale = displayScale,
                    context = context
                )
            } else {
                AuthenticUthmanicMushafPage(
                    safePage = safePage,
                    isDarkMode = isDarkMode,
                    displayScale = displayScale
                )
            }
        }

        // زر استعادة الحجم الطبيعي عند التكبير باللمس
        AnimatedVisibility(
            visible = zoomScale > 1.05f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Surface(
                onClick = {
                    zoomScale = 1f
                    offset = Offset.Zero
                },
                color = Color.Black.copy(alpha = 0.72f),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomOutMap,
                        contentDescription = "استعادة الحجم الطبيعي",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الحجم الطبيعي",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannedImageOrFallbackMushaf(
    safePage: Int,
    isDarkMode: Boolean,
    displayScale: Float,
    context: android.content.Context
) {
    val bgColor = if (isDarkMode) QuranPageBgDark else QuranPageBgLight
    val nightFilter = remember(isDarkMode) {
        if (isDarkMode) {
            val matrix = ColorMatrix(
                floatArrayOf(
                    -0.85f, 0f, 0f, 0f, 240f,
                    0f, -0.85f, 0f, 0f, 240f,
                    0f, 0f, -0.85f, 0f, 240f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            ColorFilter.colorMatrix(matrix)
        } else null
    }
    val imageUrl = remember(safePage) {
        "https://android.quran.com/data/width_1260/page%03d.png".format(safePage)
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .addHeader("User-Agent", "Mozilla/5.0")
            .build(),
        contentDescription = "صفحة المصحف $safePage",
        contentScale = ContentScale.Fit,
        colorFilter = nightFilter,
        modifier = Modifier.fillMaxSize(),
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = if (isDarkMode) MushafGoldDark else MushafEmeraldLight,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        error = {
            AuthenticUthmanicMushafPage(
                safePage = safePage,
                isDarkMode = isDarkMode,
                displayScale = displayScale
            )
        }
    )
}

/**
 * عارض صفحة المصحف بالرسم العثماني الأصلي (KFGQPC Uthmanic Script Hafs)
 * يحاكي مصحف المدينة المنورة الورقي بدقة عالية على الهواتف والأجهزة اللوحية والكمبيوتر
 */
@Composable
fun AuthenticUthmanicMushafPage(
    safePage: Int,
    isDarkMode: Boolean,
    displayScale: Float = 1.0f
) {
    val madaniLayout = remember(safePage) {
        OfflineQuranManager.getMadaniPageLayout(safePage)
    }
    val isVerified15Line = remember(safePage, madaniLayout) {
        OfflineQuranManager.isMadani15LineVerified() && madaniLayout?.isVerified15LineLayout == true
    }

    val outerBg = if (isDarkMode) QuranPageBgDark else QuranPageBgLight
    val paperBg = if (isDarkMode) QuranPageInnerDark else QuranPageInnerLight
    val goldColor = if (isDarkMode) MushafGoldDark else MushafGoldLight
    val emeraldColor = if (isDarkMode) MushafEmeraldDark else MushafEmeraldLight
    val inkColor = if (isDarkMode) MushafInkDark else MushafInkLight

    val surah = remember(safePage) { QuranDataSource.getSurahForPage(safePage) }
    val juzNum = madaniLayout?.juzNumber ?: QuranDataSource.getJuzForPage(safePage)
    val hizbNum = madaniLayout?.hizbNumber ?: QuranDataSource.getHizbForPage(safePage)
    val rubNum = madaniLayout?.rubElHizbNumber ?: 1

    // تحديد اتجاه كعب الصفحة (صفحة فردية = يمين، صفحة زوجية = يسار) كما في المصحف الورقي
    val isOddPage = safePage % 2 == 1

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(outerBg),
        contentAlignment = Alignment.Center
    ) {
        val screenW = maxWidth
        val screenH = maxHeight
        val isTabletOrDesktop = screenW >= 600.dp

        // الحفاظ على نسبة عرض متزنة على الأجهزة اللوحية والكمبيوتر حتى لا يتمدد النص أو يتشوه
        val maxContentWidth = if (isTabletOrDesktop) {
            (560.dp * displayScale).coerceAtMost(screenW - 24.dp)
        } else {
            screenW
        }

        val useScrollablePage = displayScale > 1.06f || screenH < 560.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (useScrollablePage) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxWidth()
                    .then(
                        if (useScrollablePage) Modifier.heightIn(min = screenH)
                        else Modifier.fillMaxHeight()
                    )
                    .background(
                        Brush.horizontalGradient(
                            if (isOddPage) {
                                listOf(
                                    outerBg,
                                    paperBg,
                                    paperBg,
                                    if (isDarkMode) Color(0xFF0A110E) else Color(0xFFEBDDC0)
                                )
                            } else {
                                listOf(
                                    if (isDarkMode) Color(0xFF0A110E) else Color(0xFFEBDDC0),
                                    paperBg,
                                    paperBg,
                                    outerBg
                                )
                            }
                        )
                    )
                    .padding(horizontal = if (isTabletOrDesktop) 14.dp else 8.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                // الإطار الزخرفي الإسلامي المحيط بالصفحة
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = screenH - 20.dp)
                        .background(paperBg, RoundedCornerShape(8.dp))
                ) {
                    MushafOrnamentalFrame(
                        goldColor = goldColor,
                        emeraldColor = emeraldColor,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.matchParentSize()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = screenH - 20.dp)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                    // 1. ترويسة الصفحة العلوية: اسم السورة والجزء والحزب
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الْجُزْءُ ${toEasternArabicDigits(juzNum)} • الْحِزْبُ ${toEasternArabicDigits(hizbNum)}",
                            fontFamily = AmiriFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = emeraldColor
                        )
                        Text(
                            text = "سُورَةُ ${surah.nameArabic}",
                            fontFamily = AmiriFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldColor
                        )
                    }

                    // خط فاصل ذهبي دقيق أسفل الترويسة
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        goldColor.copy(alpha = 0.65f),
                                        emeraldColor.copy(alpha = 0.45f),
                                        goldColor.copy(alpha = 0.65f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 2. متن الآيات الكريمة: التخطيط ذو الـ 15 سطراً الموثق أو البديل المنظم
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isVerified15Line && madaniLayout != null) {
                            if (safePage <= 2) {
                                OpeningSpreadMushafContent(
                                    layout = madaniLayout,
                                    surah = surah,
                                    inkColor = inkColor,
                                    goldColor = goldColor,
                                    emeraldColor = emeraldColor,
                                    isDarkMode = isDarkMode,
                                    displayScale = displayScale
                                )
                            } else {
                                FifteenLineMadaniPageContent(
                                    layout = madaniLayout,
                                    viewportHeightDp = (screenH - 120.dp).coerceAtLeast(320.dp),
                                    inkColor = inkColor,
                                    goldColor = goldColor,
                                    emeraldColor = emeraldColor,
                                    isDarkMode = isDarkMode,
                                    displayScale = displayScale
                                )
                            }
                        } else {
                            UnverifiedPageSafeContent(
                                safePage = safePage,
                                inkColor = inkColor,
                                goldColor = goldColor,
                                emeraldColor = emeraldColor,
                                isDarkMode = isDarkMode,
                                displayScale = displayScale
                            )
                        }
                    }

                    // 3. تذييل الصفحة: رقم الصفحة داخل جامة ذهبية مزخرفة + علامات الربع والسجدة
                    MushafPageFooter(
                        pageNumber = safePage,
                        rubNumber = rubNum,
                        hasSajdah = madaniLayout?.sajdahVerses?.isNotEmpty() == true,
                        goldColor = goldColor,
                        emeraldColor = emeraldColor,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
        }
    }
}

/**
 * تخطيط الصفحات 1 و 2 (الفاتحة وفاتحة البقرة) داخل إطار مذهب متمركز مثل مصحف المدينة
 */
@Composable
private fun OpeningSpreadMushafContent(
    layout: MushafPageLayout,
    surah: Surah,
    inkColor: Color,
    goldColor: Color,
    emeraldColor: Color,
    isDarkMode: Boolean,
    displayScale: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SurahOrnamentalBanner(
            surah = surah,
            goldColor = goldColor,
            emeraldColor = emeraldColor,
            isDarkMode = isDarkMode
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (surah.id != 1 && surah.id != 9) {
            Text(
                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                fontFamily = KfgqpcUthmanicHafsFontFamily,
                fontSize = (22f * displayScale).sp,
                color = emeraldColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(listOf(goldColor, emeraldColor, goldColor)),
                    shape = RoundedCornerShape(22.dp)
                )
                .background(
                    color = if (isDarkMode) Color(0x220D5338) else Color(0x14C59B38),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 10.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
                val baseSp = (21.5f * displayScale).coerceIn(15f, 32f)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    layout.lines.keys.sorted().forEach { lineNum ->
                        val tokens = layout.lines[lineNum].orEmpty()
                        MushafFittedLine(
                            tokens = tokens,
                            availableWidthPx = widthPx,
                            preferredFontSizeSp = baseSp,
                            inkColor = inkColor,
                            goldColor = goldColor,
                            emeraldColor = emeraldColor,
                            isDarkMode = isDarkMode
                        )
                    }
                }
            }
        }
    }
}

/**
 * تخطيط المصحف ذو الـ 15 سطراً الموثق (الصفحات 3 إلى 604)
 * يعرض السطور الـ 15 بالضبط وفق بيانات مصحف المدينة مع لوحات السور والبسملة
 */
@Composable
private fun FifteenLineMadaniPageContent(
    layout: MushafPageLayout,
    viewportHeightDp: Dp,
    inkColor: Color,
    goldColor: Color,
    emeraldColor: Color,
    isDarkMode: Boolean,
    displayScale: Float
) {
    val surahStartByLine = remember(layout) {
        layout.surahStarts.associateBy { it.startLine }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = viewportHeightDp),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val availableWidthPx = with(density) { maxWidth.toPx() }
        val availableHeightDp = viewportHeightDp

        // احتساب حجم الخط الأساسي المتناسب مع أبعاد الشاشة وحجم العرض المختار
        val responsiveBaseSp = remember(maxWidth, availableHeightDp, displayScale) {
            val byWidth = (maxWidth.value / 19.8f)
            val byHeight = (availableHeightDp.value / 31.0f)
            (min(byWidth, byHeight) * displayScale).coerceIn(14.5f, 30f)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = viewportHeightDp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            for (lineNum in 1..15) {
                val surahHeader = surahStartByLine[lineNum]
                if (surahHeader != null) {
                    val startedSurah = QuranDataSource.surahs.find { it.id == surahHeader.surahId }
                    if (startedSurah != null) {
                        SurahOrnamentalBanner(
                            surah = startedSurah,
                            goldColor = goldColor,
                            emeraldColor = emeraldColor,
                            isDarkMode = isDarkMode,
                            compact = true
                        )
                        if (startedSurah.id != 1 && startedSurah.id != 9) {
                            Text(
                                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                fontFamily = KfgqpcUthmanicHafsFontFamily,
                                fontSize = (responsiveBaseSp * 0.94f).sp,
                                color = emeraldColor,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                val lineTokens = layout.lines[lineNum]
                if (!lineTokens.isNullOrEmpty()) {
                    MushafFittedLine(
                        tokens = lineTokens,
                        availableWidthPx = availableWidthPx,
                        preferredFontSizeSp = responsiveBaseSp,
                        inkColor = inkColor,
                        goldColor = goldColor,
                        emeraldColor = emeraldColor,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

/**
 * سطر مصحف واحد مضبوط العرض تلقائياً عبر TextMeasurer لضمان بقاء السطر متماسكاً
 * بدون تداخل في التشكيل أو كسر غير طبيعي للكلمات مع دوائر أرقام الآيات المزخرفة
 */
@Composable
private fun MushafFittedLine(
    tokens: List<MushafToken>,
    availableWidthPx: Float,
    preferredFontSizeSp: Float,
    inkColor: Color,
    goldColor: Color,
    emeraldColor: Color,
    isDarkMode: Boolean
) {
    val textMeasurer = rememberTextMeasurer()

    val (annotatedLine, inlineContentMap) = remember(tokens, inkColor, goldColor, emeraldColor, isDarkMode) {
        val inlineMap = mutableMapOf<String, InlineTextContent>()
        val built = buildAnnotatedString {
            tokens.forEachIndexed { index, token ->
                if (token.isEndMarker) {
                    val markerId = "ayah_${token.surahId}_${token.verseNumber}"
                    append(" ")
                    appendInlineContent(markerId, " ۝ ")
                    inlineMap[markerId] = InlineTextContent(
                        Placeholder(
                            width = 1.38.em,
                            height = 1.38.em,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                        )
                    ) {
                        AyahRosetteMarker(
                            verseNumber = token.verseNumber,
                            goldColor = goldColor,
                            emeraldColor = emeraldColor,
                            inkColor = inkColor,
                            isDarkMode = isDarkMode
                        )
                    }
                } else {
                    withStyle(
                        SpanStyle(
                            fontFamily = KfgqpcUthmanicHafsFontFamily,
                            color = inkColor
                        )
                    ) {
                        append(token.text)
                    }
                }
                if (index < tokens.lastIndex) {
                    append(" ")
                }
            }
        }
        built to inlineMap
    }

    // قياس العرض الفعلي للسطر وملاءمته بدقة دون تشويه
    val fittedFontSize: TextUnit = remember(annotatedLine, availableWidthPx, preferredFontSizeSp) {
        if (availableWidthPx <= 0f) {
            preferredFontSizeSp.sp
        } else {
            val plainForMeasure = buildString {
                tokens.forEachIndexed { i, t ->
                    append(if (t.isEndMarker) " ۝ " else t.text)
                    if (i < tokens.lastIndex) append(" ")
                }
            }
            var candidate = preferredFontSizeSp
            val measured = textMeasurer.measure(
                text = AnnotatedString(plainForMeasure),
                style = TextStyle(
                    fontFamily = KfgqpcUthmanicHafsFontFamily,
                    fontSize = candidate.sp,
                    textDirection = TextDirection.Rtl
                ),
                maxLines = 1,
                softWrap = false
            )
            val targetMaxPx = availableWidthPx * 0.96f
            if (measured.size.width > targetMaxPx && measured.size.width > 0) {
                val ratio = targetMaxPx / measured.size.width.toFloat()
                candidate = (candidate * ratio).coerceAtLeast(11.5f)
            }
            candidate.sp
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text = annotatedLine,
            inlineContent = inlineContentMap,
            style = TextStyle(
                fontFamily = KfgqpcUthmanicHafsFontFamily,
                fontSize = fittedFontSize,
                lineHeight = fittedFontSize * 1.65f,
                color = inkColor,
                textAlign = TextAlign.Center,
                textDirection = TextDirection.Rtl
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.5.dp)
        )
    }
}

/**
 * شارة رقم الآية داخل علامة دائرية إسلامية مزخرفة بالذهبي والأخضر
 */
@Composable
private fun AyahRosetteMarker(
    verseNumber: Int,
    goldColor: Color,
    emeraldColor: Color,
    inkColor: Color,
    isDarkMode: Boolean
) {
    val arabicDigits = remember(verseNumber) { toEasternArabicDigits(verseNumber) }
    val fillTint = if (isDarkMode) Color(0xFF1C3027) else Color(0xFFF6E9C8)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.44f

            // رسم بتلات الزخرفة الثمانية المحيطة بالدائرة
            val outerR = r * 1.12f
            val petalsPath = Path()
            val points = 16
            for (i in 0 until points) {
                val angle = (i * 2.0 * Math.PI / points)
                val radius = if (i % 2 == 0) outerR else r * 0.88f
                val x = cx + (radius * cos(angle)).toFloat()
                val y = cy + (radius * sin(angle)).toFloat()
                if (i == 0) petalsPath.moveTo(x, y) else petalsPath.lineTo(x, y)
            }
            petalsPath.close()

            drawPath(path = petalsPath, color = fillTint)
            drawPath(path = petalsPath, color = goldColor, style = Stroke(width = 1.4f))
            drawCircle(
                color = emeraldColor.copy(alpha = 0.65f),
                radius = r * 0.72f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.0f)
            )
        }

        Text(
            text = arabicDigits,
            fontFamily = AmiriFontFamily,
            fontSize = if (arabicDigits.length >= 3) 8.sp else 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = inkColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * لوحة اسم السورة المزخرفة في بداية كل سورة (Cartouche Header Banner)
 */
@Composable
fun SurahOrnamentalBanner(
    surah: Surah,
    goldColor: Color,
    emeraldColor: Color,
    isDarkMode: Boolean,
    compact: Boolean = false
) {
    val bannerBg = if (isDarkMode) Color(0xFF162A21) else Color(0xFFF3E6C6)
    val heightDp = if (compact) 34.dp else 44.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = if (compact) 2.dp else 4.dp)
            .height(heightDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // المستطيل الزخرفي الأساسي
            drawRoundRect(
                color = bannerBg,
                topLeft = Offset(8f, 2f),
                size = Size(w - 16f, h - 4f),
                cornerRadius = CornerRadius(14f, 14f)
            )
            drawRoundRect(
                color = goldColor,
                topLeft = Offset(8f, 2f),
                size = Size(w - 16f, h - 4f),
                cornerRadius = CornerRadius(14f, 14f),
                style = Stroke(width = 2.2f)
            )
            drawRoundRect(
                color = emeraldColor.copy(alpha = 0.75f),
                topLeft = Offset(14f, 6f),
                size = Size(w - 28f, h - 12f),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 1.1f)
            )

            // ميداليات جانبية مذهبة يميناً ويساراً
            val sideR = h * 0.28f
            listOf(24f, w - 24f).forEach { cx ->
                drawCircle(color = emeraldColor.copy(alpha = 0.16f), radius = sideR, center = Offset(cx, h / 2f))
                drawCircle(color = goldColor, radius = sideR, center = Offset(cx, h / 2f), style = Stroke(width = 1.4f))
                drawCircle(color = goldColor, radius = sideR * 0.35f, center = Offset(cx, h / 2f))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = surah.type,
                fontFamily = AmiriFontFamily,
                fontSize = if (compact) 10.sp else 11.sp,
                color = emeraldColor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "سُورَةُ ${surah.nameArabic}",
                fontFamily = AmiriFontFamily,
                fontSize = if (compact) 15.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFFFF8E6) else Color(0xFF0A3D29),
                textAlign = TextAlign.Center
            )

            Text(
                text = "آيَاتُهَا ${toEasternArabicDigits(surah.totalVerses)}",
                fontFamily = AmiriFontFamily,
                fontSize = if (compact) 10.sp else 11.sp,
                color = emeraldColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * الإطار الزخرفي الإسلامي المحيط بصفحة المصحف باللون الذهبي والأخضر
 */
@Composable
private fun MushafOrnamentalFrame(
    goldColor: Color,
    emeraldColor: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // الإطار الخارجي الأخضر الزمردي
        drawRoundRect(
            color = emeraldColor.copy(alpha = if (isDarkMode) 0.55f else 0.72f),
            topLeft = Offset(3f, 3f),
            size = Size(w - 6f, h - 6f),
            cornerRadius = CornerRadius(14f, 14f),
            style = Stroke(width = 2.0f)
        )

        // الإطار الذهبي المزدوج
        drawRoundRect(
            color = goldColor,
            topLeft = Offset(9f, 9f),
            size = Size(w - 18f, h - 18f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = 2.4f)
        )

        drawRoundRect(
            color = goldColor.copy(alpha = 0.5f),
            topLeft = Offset(14f, 14f),
            size = Size(w - 28f, h - 28f),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 1.0f)
        )

        // زخارف الزوايا الأربع (Islamic Corner Medallions)
        val cornerOffset = 18f
        val cornerRadius = 11f
        val corners = listOf(
            Offset(cornerOffset, cornerOffset),
            Offset(w - cornerOffset, cornerOffset),
            Offset(cornerOffset, h - cornerOffset),
            Offset(w - cornerOffset, h - cornerOffset)
        )
        corners.forEach { c ->
            drawCircle(color = emeraldColor.copy(alpha = 0.15f), radius = cornerRadius, center = c)
            drawCircle(color = goldColor, radius = cornerRadius, center = c, style = Stroke(width = 1.6f))
            drawCircle(color = emeraldColor, radius = cornerRadius * 0.45f, center = c)
        }
    }
}

/**
 * تذييل الصفحة بالجامة الذهبية لرقم الصفحة وعلامات الربع والسجدة
 */
@Composable
private fun MushafPageFooter(
    pageNumber: Int,
    rubNumber: Int,
    hasSajdah: Boolean,
    goldColor: Color,
    emeraldColor: Color,
    isDarkMode: Boolean
) {
    val quarterLabel = remember(rubNumber) {
        val hizb = ((rubNumber - 1) / 4) + 1
        when ((rubNumber - 1) % 4) {
            0 -> "۞ الحزب ${toEasternArabicDigits(hizb)}"
            1 -> "ربع الحزب ${toEasternArabicDigits(hizb)}"
            2 -> "نصف الحزب ${toEasternArabicDigits(hizb)}"
            else -> "ثلاثة أرباع ${toEasternArabicDigits(hizb)}"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = quarterLabel,
            fontFamily = AmiriFontFamily,
            fontSize = 11.sp,
            color = emeraldColor,
            fontWeight = FontWeight.SemiBold
        )

        // رقم الصفحة داخل ميدالية ذهبية
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isDarkMode) Color(0xFF192B22) else Color(0xFFF3E7C8),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, goldColor)
        ) {
            Text(
                text = toEasternArabicDigits(pageNumber),
                fontFamily = AmiriFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFFFF8E6) else Color(0xFF0A3D29),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
            )
        }

        Text(
            text = if (hasSajdah) "۩ موضع سجدة" else "رواية حفص",
            fontFamily = AmiriFontFamily,
            fontSize = 11.sp,
            color = if (hasSajdah) goldColor else emeraldColor.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * عارض احتياطي صريح في حال لم يتحقق تخطيط الـ 15 سطراً
 * يعرض النص القرآني الموثق دون ادعاء مطابقة تخطيط الصفحات الورقية
 */
@Composable
private fun UnverifiedPageSafeContent(
    safePage: Int,
    inkColor: Color,
    goldColor: Color,
    emeraldColor: Color,
    isDarkMode: Boolean,
    displayScale: Float
) {
    val pageData = remember(safePage) { OfflineQuranManager.getPageData(safePage) }

    val inlineContent = remember(pageData, goldColor, emeraldColor, inkColor, isDarkMode) {
        pageData.ayahs.associate { ayah ->
            val key = "fallback_${ayah.surahId}_${ayah.verseNumber}"
            key to InlineTextContent(
                Placeholder(
                    width = 1.35.em,
                    height = 1.35.em,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                )
            ) {
                AyahRosetteMarker(
                    verseNumber = ayah.verseNumber,
                    goldColor = goldColor,
                    emeraldColor = emeraldColor,
                    inkColor = inkColor,
                    isDarkMode = isDarkMode
                )
            }
        }
    }

    val pageText = remember(pageData, inkColor) {
        buildAnnotatedString {
            pageData.ayahs.forEach { ayah ->
                withStyle(
                    SpanStyle(
                        fontFamily = KfgqpcUthmanicHafsFontFamily,
                        color = inkColor
                    )
                ) {
                    append(ayah.textArabic)
                    append(" ")
                }
                appendInlineContent("fallback_${ayah.surahId}_${ayah.verseNumber}", " ۝ ")
                append(" ")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (safePage == pageData.surah.pageNumber) {
            SurahOrnamentalBanner(
                surah = pageData.surah,
                goldColor = goldColor,
                emeraldColor = emeraldColor,
                isDarkMode = isDarkMode
            )
            if (pageData.surah.id != 1 && pageData.surah.id != 9) {
                Text(
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                    fontFamily = KfgqpcUthmanicHafsFontFamily,
                    fontSize = (20f * displayScale).sp,
                    color = emeraldColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        val fontSp = (20.5f * displayScale).coerceIn(15f, 32f)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = pageText,
                inlineContent = inlineContent,
                style = TextStyle(
                    fontFamily = KfgqpcUthmanicHafsFontFamily,
                    fontSize = fontSp.sp,
                    lineHeight = (fontSp * 2.25f).sp,
                    color = inkColor,
                    textAlign = TextAlign.Center,
                    textDirection = TextDirection.Rtl
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
