package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.example.utils.QuranFontSettings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.models.Reciter
import com.example.data.models.Surah
import com.example.data.sources.OfflineQuranManager
import com.example.data.sources.QuranDataSource
import com.example.ui.components.MadaniQuranPageView
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccentLight
import com.example.utils.AudioPlayerManager
import com.example.utils.PlaybackState
import com.example.utils.QuranBookmarkStorage
import kotlinx.coroutines.launch

val GoldAccent = Color(0xFFD4AF37)
val PageBgColor = Color(0xFFFFFDF5)
val QuranGreen = Color(0xFF0D5338)

private fun formatTimeMs(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    audioPlayerManager: AudioPlayerManager? = null,
    bookmarkStorage: QuranBookmarkStorage? = null,
    khatmahStorage: com.example.utils.KhatmahStorage? = null,
    targetPage: Int? = null,
    onOpenKhatmah: () -> Unit = {},
    onControlsVisibilityChanged: (Boolean) -> Unit = {},
    onNavigateHome: () -> Unit = {},
    onBackClick: () -> Unit = onNavigateHome,
    onSurahIndexClick: () -> Unit = {}
) {
    val totalPages = 604
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        QuranFontSettings.init(context)
    }
    val scope = rememberCoroutineScope()

    val actualBookmarkStorage = remember(bookmarkStorage) {
        bookmarkStorage ?: QuranBookmarkStorage(context)
    }
    val actualAudioPlayer = remember(audioPlayerManager) {
        audioPlayerManager ?: AudioPlayerManager(context)
    }

    val bookmarks by actualBookmarkStorage.bookmarks.collectAsState()
    val lastReadPage by actualBookmarkStorage.lastReadPage.collectAsState()
    val playerState by actualAudioPlayer.uiState.collectAsState()

    var showBars by remember { mutableStateOf(false) }
    val isDarkMode by QuranFontSettings.isNightMode.collectAsState()
    val displayScale by QuranFontSettings.displayScale.collectAsState()
    val isFullscreen by QuranFontSettings.isFullscreen.collectAsState()
    val useImageMushaf by QuranFontSettings.useImageMushaf.collectAsState()
    var isTwoPageSpread by remember { mutableStateOf(false) }

    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showSurahIndexSheet by remember { mutableStateOf(false) }
    var showQuickJumpDialog by remember { mutableStateOf(false) }
    var showAudioSettingsSheet by remember { mutableStateOf(false) }
    var showSearchScreen by remember { mutableStateOf(false) }

    // إدارة وضع ملء الشاشة الحقيقي (إخفاء شريط الحالة والتنقل عند تفعيل وضع القراءة الكاملة)
    val view = LocalView.current
    DisposableEffect(isFullscreen, showBars) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (isFullscreen && !showBars) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            val windowOnDispose = (view.context as? Activity)?.window
            if (windowOnDispose != null) {
                WindowCompat.getInsetsController(windowOnDispose, view)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val initialPage = remember {
        if (targetPage != null && targetPage in 1..totalPages) {
            targetPage - 1
        } else {
            (actualBookmarkStorage.lastReadPage.value - 1).coerceIn(0, totalPages - 1)
        }
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { totalPages }
    )

    // Handle external page navigation requests
    LaunchedEffect(targetPage) {
        if (targetPage != null && targetPage in 1..totalPages) {
            pagerState.scrollToPage(targetPage - 1)
        }
    }

    // Keep last read page updated automatically
    LaunchedEffect(pagerState.currentPage) {
        actualBookmarkStorage.setLastReadPage(pagerState.currentPage + 1)
    }

    // Sync bar visibility with parent scaffold
    LaunchedEffect(showBars) {
        onControlsVisibilityChanged(showBars)
    }

    val currentPage = pagerState.currentPage + 1
    val isCurrentBookmarked = actualBookmarkStorage.isPageBookmarked(currentPage)
    val currentSurah = QuranDataSource.getSurahForPage(currentPage)

    if (showSearchScreen) {
        androidx.activity.compose.BackHandler {
            showSearchScreen = false
        }
        QuranSearchScreen(
            onBackClick = { showSearchScreen = false },
            onResultClick = { pageNumber ->
                showSearchScreen = false
                scope.launch {
                    pagerState.scrollToPage(pageNumber - 1)
                }
                actualBookmarkStorage.setLastReadPage(pageNumber)
            }
        )
        return
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) com.example.ui.components.QuranPageBgDark else com.example.ui.components.QuranPageBgLight)
    ) {
        val isWideScreen = maxWidth >= 840.dp
        val activeTwoPageSpread = isWideScreen && isTwoPageSpread

        // 1. تصفح صفحات مصحف المدينة المنورة (صفحة واحدة أو صفحتين متقابلتين على الأجهزة اللوحية والكمبيوتر)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (activeTwoPageSpread) {
                val spreadCount = (totalPages + 1) / 2
                val spreadInitial = (pagerState.currentPage / 2).coerceIn(0, spreadCount - 1)
                val spreadPagerState = rememberPagerState(
                    initialPage = spreadInitial,
                    pageCount = { spreadCount }
                )
                LaunchedEffect(spreadPagerState.currentPage) {
                    val rightPageNum = (spreadPagerState.currentPage * 2 + 1).coerceIn(1, totalPages)
                    if (pagerState.currentPage + 1 != rightPageNum) {
                        pagerState.scrollToPage(rightPageNum - 1)
                    }
                }
                HorizontalPager(
                    state = spreadPagerState,
                    modifier = Modifier.fillMaxSize()
                ) { spreadIdx ->
                    val rightPage = (spreadIdx * 2 + 1).coerceIn(1, totalPages)
                    val leftPage = (rightPage + 1).takeIf { it <= totalPages }
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            QuranPageContent(
                                pageNumber = rightPage,
                                isDarkMode = isDarkMode,
                                displayScale = displayScale,
                                useScannedImages = useImageMushaf,
                                isBookmarked = actualBookmarkStorage.isPageBookmarked(rightPage),
                                onToggleBookmark = {
                                    actualBookmarkStorage.toggleBookmark(rightPage)
                                },
                                onToggleBars = { showBars = !showBars }
                            )
                        }
                        if (leftPage != null) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                QuranPageContent(
                                    pageNumber = leftPage,
                                    isDarkMode = isDarkMode,
                                    displayScale = displayScale,
                                    useScannedImages = useImageMushaf,
                                    isBookmarked = actualBookmarkStorage.isPageBookmarked(leftPage),
                                    onToggleBookmark = {
                                        actualBookmarkStorage.toggleBookmark(leftPage)
                                    },
                                    onToggleBars = { showBars = !showBars }
                                )
                            }
                        }
                    }
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false
                ) { pageIndex ->
                    val pageNumber = pageIndex + 1
                    QuranPageContent(
                        pageNumber = pageNumber,
                        isDarkMode = isDarkMode,
                        displayScale = displayScale,
                        useScannedImages = useImageMushaf,
                        isBookmarked = actualBookmarkStorage.isPageBookmarked(pageNumber),
                        onToggleBookmark = {
                            val added = actualBookmarkStorage.toggleBookmark(pageNumber)
                            Toast.makeText(
                                context,
                                if (added) "تم حفظ صفحة $pageNumber في العلامات المرجعية 🔖" else "تمت إزالة العلامة المرجعية",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onToggleBars = { showBars = !showBars }
                    )
                }
            }
        }

        // 2. شريط تحكم علوي شفاف وبسيط يظهر بنقرة واحدة فقط (Single Tap)
        AnimatedVisibility(
            visible = showBars,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.78f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "سُورَةُ ${currentSurah.nameArabic} (ص ${toEasternArabicDigits(currentPage)})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "الجزء ${QuranDataSource.getJuzForPage(currentPage)} • الحزب ${QuranDataSource.getHizbForPage(currentPage)}",
                                fontSize = 11.sp,
                                color = GoldAccent
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // حفظ العلامة المرجعية
                        IconButton(onClick = {
                            val added = actualBookmarkStorage.toggleBookmark(currentPage)
                            Toast.makeText(
                                context,
                                if (added) "تم حفظ صفحة $currentPage في العلامات المرجعية 🔖" else "تمت إزالة العلامة المرجعية",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Icon(
                                imageVector = if (isCurrentBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "العلامة المرجعية",
                                tint = if (isCurrentBookmarked) GoldAccent else Color.White
                            )
                        }

                        // الانتقال السريع لرقم الصفحة
                        IconButton(onClick = { showQuickJumpDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = "الانتقال السريع إلى صفحة",
                                tint = Color.White
                            )
                        }

                        // البحث في القرآن
                        IconButton(onClick = { showSearchScreen = true }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "البحث في القرآن الكريم",
                                tint = Color.White
                            )
                        }

                        // فهرس السور
                        IconButton(onClick = {
                            if (onSurahIndexClick != {}) onSurahIndexClick()
                            else showSurahIndexSheet = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "فهرس السور",
                                tint = Color.White
                            )
                        }

                        // تشغيل / إيقاف التلاوة
                        val isPlaying = playerState.currentSurah?.id == currentSurah.id && playerState.playbackState == PlaybackState.PLAYING
                        if (playerState.playbackState == PlaybackState.BUFFERING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = GoldAccent
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        actualAudioPlayer.togglePlayPause()
                                    } else {
                                        actualAudioPlayer.playSurah(currentSurah)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل التلاوة",
                                    tint = if (isPlaying) GoldAccent else Color.White
                                )
                            }
                        }

                        // وضع ملء الشاشة
                        IconButton(onClick = {
                            QuranFontSettings.toggleFullscreen()
                            if (!isFullscreen) {
                                showBars = false
                            }
                        }) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                contentDescription = if (isFullscreen) "إنهاء ملء الشاشة" else "وضع ملء الشاشة",
                                tint = if (isFullscreen) GoldAccent else Color.White
                            )
                        }

                        // عرض صفحتين للأجهزة اللوحية والكمبيوتر
                        if (isWideScreen) {
                            IconButton(onClick = { isTwoPageSpread = !isTwoPageSpread }) {
                                Icon(
                                    imageVector = Icons.Filled.ViewAgenda,
                                    contentDescription = "عرض صفحتين متقابلتين",
                                    tint = if (isTwoPageSpread) GoldAccent else Color.White
                                )
                            }
                        }

                        // إعدادات التلاوة والخط والعرض
                        IconButton(onClick = { showAudioSettingsSheet = true }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "إعدادات المصحف والقراء والوضع الليلي",
                                tint = GoldAccent
                            )
                        }
                    }
                }
            }
        }

        // 3. شريط تحكم سفلي شفاف وبسيط يظهر بنقرة واحدة (Single Tap) ومشغل الصوت
        AnimatedVisibility(
            visible = showBars,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    // مشغل التلاوة الصوتية إن كانت نشطة
                    if (playerState.playbackState != PlaybackState.IDLE) {
                        val isPlaying = playerState.playbackState == PlaybackState.PLAYING
                        val isBuffering = playerState.playbackState == PlaybackState.BUFFERING
                        val currentRecitingSurah = playerState.currentSurah ?: currentSurah

                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.GraphicEq,
                                            contentDescription = null,
                                            tint = GoldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "سُورَةُ ${currentRecitingSurah.nameArabic} • ${playerState.currentReciter.nameArabic}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        color = GoldAccent.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${playerState.audioBitrateKbps} kbps HD",
                                            fontSize = 9.sp,
                                            color = GoldAccent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (playerState.durationMs > 0) {
                                    val progress = (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = GoldAccent,
                                        trackColor = Color.White.copy(alpha = 0.25f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${formatTimeMs(playerState.currentPositionMs)} / ${formatTimeMs(playerState.durationMs)}",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { actualAudioPlayer.playPreviousSurah() },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.SkipNext, contentDescription = "السورة السابقة", tint = Color.White, modifier = Modifier.size(18.dp))
                                        }

                                        if (isBuffering) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = GoldAccent
                                            )
                                        } else {
                                            IconButton(
                                                onClick = { actualAudioPlayer.togglePlayPause() },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                                    tint = GoldAccent,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { actualAudioPlayer.playNextSurah() },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.SkipPrevious, contentDescription = "السورة التالية", tint = Color.White, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = { actualAudioPlayer.stop() },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.Stop, contentDescription = "إيقاف", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Text(
                                        text = "بالخلفية 🔊",
                                        fontSize = 10.sp,
                                        color = GoldAccent
                                    )
                                }
                            }
                        }
                    }

                    // شريط التمرير والتنقل السريع بين الصفحات
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (pagerState.currentPage > 0) {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                }
                            },
                            enabled = pagerState.currentPage > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("❮", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (pagerState.currentPage > 0) GoldAccent else Color.Gray)
                        }

                        Slider(
                            value = currentPage.toFloat(),
                            onValueChange = { newVal ->
                                val targetIdx = (newVal.toInt() - 1).coerceIn(0, totalPages - 1)
                                scope.launch { pagerState.scrollToPage(targetIdx) }
                            },
                            valueRange = 1f..totalPages.toFloat(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = GoldAccent,
                                activeTrackColor = GoldAccent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )

                        IconButton(
                            onClick = {
                                if (pagerState.currentPage < totalPages - 1) {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                }
                            },
                            enabled = pagerState.currentPage < totalPages - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("❯", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (pagerState.currentPage < totalPages - 1) GoldAccent else Color.Gray)
                        }
                    }

                    // شريط معلومات الصفحة
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "صفحة ${toEasternArabicDigits(currentPage)} من ${toEasternArabicDigits(totalPages)}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "سُورَةُ ${currentSurah.nameArabic} • الجزء ${QuranDataSource.getJuzForPage(currentPage)}",
                            fontSize = 12.sp,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Quick Jump Dialog
    if (showQuickJumpDialog) {
        QuranQuickJumpDialog(
            currentPage = currentPage,
            onJumpToPage = { target ->
                scope.launch {
                    pagerState.scrollToPage((target.coerceIn(1, totalPages)) - 1)
                    showQuickJumpDialog = false
                }
            },
            onDismiss = { showQuickJumpDialog = false }
        )
    }

    // Surah Index Sheet
    if (showSurahIndexSheet) {
        SurahIndexModalSheet(
            currentSurahId = currentSurah.id,
            onSelectSurah = { surah ->
                scope.launch {
                    pagerState.scrollToPage(surah.pageNumber - 1)
                    showSurahIndexSheet = false
                }
            },
            onJumpToPage = { targetPage ->
                scope.launch {
                    pagerState.scrollToPage((targetPage.coerceIn(1, totalPages)) - 1)
                    showSurahIndexSheet = false
                }
            },
            onDismiss = { showSurahIndexSheet = false }
        )
    }

    // Saved Bookmarks Sheet
    if (showBookmarksSheet) {
        QuranBookmarksModalSheet(
            currentPage = currentPage,
            lastReadPage = lastReadPage,
            bookmarks = bookmarks,
            isCurrentPageBookmarked = isCurrentBookmarked,
            onToggleBookmarkCurrentPage = { note ->
                actualBookmarkStorage.toggleBookmark(currentPage, note)
            },
            onJumpToPage = { target ->
                scope.launch {
                    pagerState.scrollToPage((target.coerceIn(1, totalPages)) - 1)
                    showBookmarksSheet = false
                }
            },
            onDeleteBookmark = { id ->
                actualBookmarkStorage.removeBookmarkById(id)
            },
            onDismiss = { showBookmarksSheet = false }
        )
    }

    // Settings Modal Sheet (Night mode, Bitrate, Reciter, Volume Boost)
    if (showAudioSettingsSheet) {
        QuranSettingsModalSheet(
            currentReciter = playerState.currentReciter,
            isDarkMode = isDarkMode,
            currentVolumeGainMb = playerState.volumeGainMb,
            currentBitrate = playerState.audioBitrateKbps,
            onSelectReciter = { actualAudioPlayer.selectReciter(it) },
            onToggleDarkMode = { QuranFontSettings.toggleNightMode() },
            onVolumeGainChange = { actualAudioPlayer.setVolumeBoostLevel(it) },
            onBitrateChange = { actualAudioPlayer.setAudioBitrate(it) },
            onMaximizeSystemVolume = { actualAudioPlayer.maximizeSystemVolume() },
            onDismiss = { showAudioSettingsSheet = false }
        )
    }
}

/**
 * يعرض صورة صفحة المصحف الشريف الأصلية (مصحف مجمع الملك فهد لطباعة المصحف الشريف بالمدينة النبوية)
 * بحيث تملأ الشاشة بالكامل بدون هوامش زائدة أو كروت مشتتة.
 * تبدأ الصفحة 1 بسورة الفاتحة المزخرفة تماماً كما في الصفحة 4 من ملف الـ PDF.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranPageContent(
    pageNumber: Int,
    isDarkMode: Boolean = false,
    displayScale: Float = 1.0f,
    useScannedImages: Boolean = false,
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    onToggleBars: () -> Unit = {}
) {
    MadaniQuranPageView(
        pageNumber = pageNumber,
        isDarkMode = isDarkMode,
        displayScale = displayScale,
        useScannedImages = useScannedImages,
        isBookmarked = isBookmarked,
        onToggleBookmark = onToggleBookmark,
        onToggleBars = onToggleBars
    )
}


/**
 * نافذة فهرس المصحف الشامل: السور (114) • الأجزاء والأحزاب (30 / 60) • الأرباع (240)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahIndexModalSheet(
    currentSurahId: Int,
    onSelectSurah: (Surah) -> Unit,
    onJumpToPage: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: السور, 1: الأجزاء والأحزاب, 2: الأرباع
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allQuarters = remember { OfflineQuranManager.getAllQuarters() }

    // دالة لتطبيع النصوص العربية لتسهيل البحث الشامل بدون حساسية للهمزات والتشكيل
    fun normalizeArabic(text: String): String {
        return text
            .replace("[\\u064B-\\u065F\\u0670]".toRegex(), "") // إزالة حركات التشكيل
            .replace("[إأآا]".toRegex(), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .trim()
    }

    // استخراج رقم الصفحة أو السورة إذا كتب المستخدم أرقاماً (إنجليزية أو عربية مشرقية)
    val parsedNumber = remember(searchQuery) {
        val cleaned = searchQuery
            .replace("ص", "")
            .replace("صفحة", "")
            .replace("سورة", "")
            .trim()
            .map { ch ->
                when (ch) {
                    '٠' -> '0'; '١' -> '1'; '٢' -> '2'; '٣' -> '3'; '٤' -> '4'
                    '٥' -> '5'; '٦' -> '6'; '٧' -> '7'; '٨' -> '8'; '٩' -> '9'
                    else -> ch
                }
            }.joinToString("")
        cleaned.toIntOrNull()
    }

    val filteredSurahs = remember(searchQuery, parsedNumber) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            QuranDataSource.surahs
        } else if (parsedNumber != null && parsedNumber in 1..604) {
            // بحث برقم الصفحة أو رقم السورة
            val bySurahNumber = QuranDataSource.surahs.filter { it.id == parsedNumber }
            val byPageNumber = QuranDataSource.surahs.filter { surah ->
                // إيجاد السورة التي تقع فيها هذه الصفحة أو تبدأ عندها
                val nextSurah = QuranDataSource.surahs.getOrNull(surah.id)
                val endPage = (nextSurah?.pageNumber ?: 605) - 1
                parsedNumber in surah.pageNumber..endPage || surah.pageNumber == parsedNumber
            }
            (bySurahNumber + byPageNumber).distinctBy { it.id }
        } else {
            val normQ = normalizeArabic(q)
            QuranDataSource.surahs.filter {
                normalizeArabic(it.nameArabic).contains(normQ) ||
                it.nameEnglish.contains(q, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "فهرس مصحف المدينة الشامل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = QuranGreen
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            // التبويبات الثلاثة: السور (114) | الأجزاء والأحزاب | الأرباع (240)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = QuranGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = GoldAccent
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("السور (${toEasternArabicDigits(114)})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("الأجزاء والأحزاب", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("الأرباع (${toEasternArabicDigits(240)})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (activeTab == 0) {
                // شريط البحث المزدوج: باسم السورة أو برقم الصفحة
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث باسم السورة أو رقم الصفحة (1 - 604)...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = QuranGreen) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "مسح", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (parsedNumber != null && parsedNumber in 1..604) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        onClick = { onJumpToPage(parsedNumber) },
                        shape = RoundedCornerShape(10.dp),
                        color = GoldAccent.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📖", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الانتقال المباشر إلى الصفحة ${toEasternArabicDigits(parsedNumber)}",
                                    fontWeight = FontWeight.Bold,
                                    color = QuranGreen,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "انتقال ↵",
                                color = QuranGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredSurahs) { surah ->
                        val isSelected = surah.id == currentSurahId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSurah(surah) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) QuranGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(
                                    if (isSelected) listOf(GoldAccent, QuranGreen)
                                    else listOf(Color.LightGray.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) QuranGreen else GoldAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = toEasternArabicDigits(surah.id),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else QuranGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "سُورَةُ ${surah.nameArabic}",
                                            fontFamily = AmiriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = QuranGreen
                                        )
                                        Text(
                                            text = "${surah.type} • آياتها ${toEasternArabicDigits(surah.totalVerses)}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(QuranGreen.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ص ${toEasternArabicDigits(surah.pageNumber)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = QuranGreen
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == 1) {
                // تبويب الأجزاء (30) والأحزاب (60)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items((1..30).toList()) { juz ->
                        val juzStartPage = if (juz == 1) 1 else 2 + (juz - 1) * 20
                        val hizb1 = (juz - 1) * 2 + 1
                        val hizb2 = hizb1 + 1
                        val hizb1Page = allQuarters.find { it.hizbNumber == hizb1 && it.quarterInHizb == 1 }?.pageNumber ?: juzStartPage
                        val hizb2Page = allQuarters.find { it.hizbNumber == hizb2 && it.quarterInHizb == 1 }?.pageNumber ?: (juzStartPage + 10)
                        val startSurah = QuranDataSource.getSurahForPage(hizb1Page)

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onJumpToPage(hizb1Page) },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(QuranGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = toEasternArabicDigits(juz),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "الجزء ${toEasternArabicDigits(juz)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = QuranGreen
                                            )
                                            Text(
                                                text = "يبدأ عند سورة ${startSurah.nameArabic}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Text(
                                        text = "ص ${toEasternArabicDigits(hizb1Page)}",
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent,
                                        fontSize = 12.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        onClick = { onJumpToPage(hizb1Page) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = QuranGreen.copy(alpha = 0.1f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "۞ الحزب ${toEasternArabicDigits(hizb1)} (ص ${toEasternArabicDigits(hizb1Page)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = QuranGreen,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                    Surface(
                                        onClick = { onJumpToPage(hizb2Page) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = GoldAccent.copy(alpha = 0.16f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "۞ الحزب ${toEasternArabicDigits(hizb2)} (ص ${toEasternArabicDigits(hizb2Page)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = QuranGreen,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // تبويب الأرباع الـ 240 (ربع، نصف، ثلاثة أرباع، رأس الحزب)
                if (allQuarters.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "جاري تحميل فهرس الأرباع الـ ٢٤٠...",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allQuarters) { q ->
                            val quarterTitle = when (q.quarterInHizb) {
                                1 -> "رأس الحزب ${toEasternArabicDigits(q.hizbNumber)}"
                                2 -> "ربع الحزب ${toEasternArabicDigits(q.hizbNumber)}"
                                3 -> "نصف الحزب ${toEasternArabicDigits(q.hizbNumber)}"
                                else -> "ثلاثة أرباع الحزب ${toEasternArabicDigits(q.hizbNumber)}"
                            }
                            val surah = QuranDataSource.surahs.find { it.id == q.surahId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onJumpToPage(q.pageNumber) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "۞ $quarterTitle • الجزء ${toEasternArabicDigits(q.juzNumber)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = QuranGreen
                                        )
                                        Text(
                                            text = q.previewText,
                                            fontFamily = AmiriFontFamily,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "سورة ${surah?.nameArabic ?: ""} • آية ${toEasternArabicDigits(q.verseNumber)}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GoldAccent.copy(alpha = 0.18f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "ص ${toEasternArabicDigits(q.pageNumber)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = QuranGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * نافذة العلامات المرجعية وآخر موضع قراءة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranBookmarksModalSheet(
    currentPage: Int,
    lastReadPage: Int,
    bookmarks: List<com.example.data.models.QuranBookmark>,
    isCurrentPageBookmarked: Boolean,
    onToggleBookmarkCurrentPage: (String) -> Unit,
    onJumpToPage: (Int) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Bookmark, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "العلامات المرجعية ومواضع التلاوة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = QuranGreen
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Last read position card
            val lastPageInfo = remember(lastReadPage) { QuranDataSource.getPageData(lastReadPage) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJumpToPage(lastReadPage) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = QuranGreen.copy(alpha = 0.08f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, QuranGreen)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "آخر موضع تلاوة متوقف عنده:",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "صفحة ${toEasternArabicDigits(lastReadPage)} • سورة ${lastPageInfo.surah.nameArabic}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = QuranGreen
                        )
                    }
                    Button(
                        onClick = { onJumpToPage(lastReadPage) },
                        colors = ButtonDefaults.buttonColors(containerColor = QuranGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("متابعة 📖", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bookmark current page action
            OutlinedButton(
                onClick = { onToggleBookmarkCurrentPage("") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isCurrentPageBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isCurrentPageBookmarked) GoldAccent else QuranGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCurrentPageBookmarked) "إزالة علامة الصفحة الحالية (ص $currentPage)" else "حفظ علامة مرجعية للصفحة الحالية (ص $currentPage)",
                    fontWeight = FontWeight.Bold,
                    color = QuranGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "قائمة العلامات المحفوظة (${toEasternArabicDigits(bookmarks.size)}):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = QuranGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد علامات مرجعية محفوظة بعد.\nاضغط على شريط العلامة المرجعية في الركن لحفظ أي صفحة بسهولة.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(bookmarks) { bookmark ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onJumpToPage(bookmark.pageNumber) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.Bookmark, contentDescription = null, tint = GoldAccent)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "صفحة ${toEasternArabicDigits(bookmark.pageNumber)} • سورة ${bookmark.surahName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = QuranGreen
                                        )
                                        Text(
                                            text = "الجزء ${toEasternArabicDigits(bookmark.juzNumber)} • الحزب ${toEasternArabicDigits(bookmark.hizbNumber)}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Row {
                                    TextButton(onClick = { onJumpToPage(bookmark.pageNumber) }) {
                                        Text("انتقال", color = QuranGreen, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(onClick = { onDeleteBookmark(bookmark.id) }) {
                                        Text("✕", color = Color.Red, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * نافذة إعدادات المصحف والتلاوة والوضع الليلي
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSettingsModalSheet(
    currentReciter: Reciter,
    isDarkMode: Boolean,
    currentVolumeGainMb: Int,
    currentBitrate: Int = 192,
    onSelectReciter: (Reciter) -> Unit,
    onToggleDarkMode: () -> Unit,
    onVolumeGainChange: (Int) -> Unit,
    onBitrateChange: (Int) -> Unit = {},
    onMaximizeSystemVolume: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fontSizeSp by QuranFontSettings.fontSizeSp.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = null, tint = QuranGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعدادات المصحف الشريف والقراء والخط",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = QuranGreen
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ========================================================
            // 1. اختيار القارئ (Reciter Selection) - القراء المعتمدون
            // ========================================================
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🎙️ اختيار القارئ المعتمد للتلاوة الصوتية (${QuranDataSource.reciters.size} قراء):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = QuranGreen
                )
            }
            Text(
                text = "اختر قارئك المفضل بروابط بث مباشرة وسريعة بجودة عالية (192 kbps):",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                QuranDataSource.reciters.forEach { reciter ->
                    val isSelected = currentReciter.id == reciter.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectReciter(reciter) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) QuranGreen.copy(alpha = 0.14f) else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, QuranGreen) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reciter.nameArabic,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) QuranGreen else Color.DarkGray
                                )
                                Text(
                                    text = reciter.style,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isSelected) GoldAccent.copy(alpha = 0.25f) else Color.LightGray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "192 kbps",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) QuranGreen else Color.DarkGray,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = QuranGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // 2. نظام عرض المصحف وحجم العرض (Display Scale & Mode)
            // ========================================================
            val displayScale by QuranFontSettings.displayScale.collectAsState()
            val useImageMushaf by QuranFontSettings.useImageMushaf.collectAsState()
            val isVerified15Line = remember { OfflineQuranManager.isMadani15LineVerified() }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📖 نظام عرض المصحف وحجم العرض (بدون تشويه):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = QuranGreen
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // بطاقة التحقق والمصدر الرسمي KFGQPC
            Surface(
                color = if (isVerified15Line) QuranGreen.copy(alpha = 0.08f) else GoldAccent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isVerified15Line) QuranGreen.copy(alpha = 0.35f) else GoldAccent),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = if (isVerified15Line) QuranGreen else GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isVerified15Line)
                                "مصحف المدينة المنورة (١٥ سطراً موثقاً • ٦٠٤ صفحات • ٦٢٣٦ آية)"
                            else
                                "مصحف المدينة (النص العثماني الأوفلاين الموثق)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuranGreen
                        )
                        Text(
                            text = "خط مجمع الملك فهد لطباعة المصحف الشريف (KFGQPC Uthmanic Script Hafs v0.18 - مرخص مجاناً دون تعديل).",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!useImageMushaf) QuranGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, if (!useImageMushaf) GoldAccent else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { QuranFontSettings.setUseImageMushaf(false) }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "الرسم العثماني (١٥ سطراً أوفلاين)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!useImageMushaf) QuranGreen else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (useImageMushaf) QuranGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, if (useImageMushaf) GoldAccent else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { QuranFontSettings.setUseImageMushaf(true) }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "صور صفحات المصحف",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (useImageMushaf) QuranGreen else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "حجم العرض: ${(displayScale * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuranGreen
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalButton(
                                onClick = { QuranFontSettings.decreaseDisplayScale() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                enabled = displayScale > QuranFontSettings.MIN_SCALE
                            ) {
                                Text("تصغير -", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { QuranFontSettings.resetDisplayScale() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("١٠٠٪", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QuranGreen)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { QuranFontSettings.increaseDisplayScale() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = QuranGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                enabled = displayScale < QuranFontSettings.MAX_SCALE
                            ) {
                                Text("تكبير +", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Slider(
                        value = displayScale,
                        onValueChange = { QuranFontSettings.setDisplayScale(it) },
                        valueRange = QuranFontSettings.MIN_SCALE..QuranFontSettings.MAX_SCALE,
                        colors = SliderDefaults.colors(
                            thumbColor = QuranGreen,
                            activeTrackColor = QuranGreen,
                            inactiveTrackColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    // معاينة حية لخط KFGQPC Uthmanic Hafs الأصلي
                    Surface(
                        color = if (isDarkMode) Color(0xFF0E1713) else Color(0xFFFAF3E0),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.65f)),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                fontFamily = com.example.ui.theme.KfgqpcUthmanicHafsFontFamily,
                                fontSize = (20f * displayScale).coerceIn(15f, 30f).sp,
                                color = if (isDarkMode) GoldAccent else QuranGreen,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ",
                                fontFamily = com.example.ui.theme.KfgqpcUthmanicHafsFontFamily,
                                fontSize = (20f * displayScale).coerceIn(15f, 30f).sp,
                                color = if (isDarkMode) Color(0xFFF5EFE0) else Color(0xFF1A1916),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ========================================================
            // 3. الوضع الليلي (Night Mode / High Contrast)
            // ========================================================
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🌙 الوضع الليلي (Night Mode):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = QuranGreen
                )
            }
            Text(
                text = "خلفية داكنة مع خط عالي التباين والنقاء لراحة العين ومنع إجهاد البصر أثناء القراءة في الظلام:",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleDarkMode() },
                shape = RoundedCornerShape(12.dp),
                border = if (isDarkMode) androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF14241B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isDarkMode) "🌙" else "☀️",
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isDarkMode) "الوضع الليلي عالي التباين (مفعّل)" else "الوضع الليلي (غير مفعّل)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isDarkMode) GoldAccent else Color.DarkGray
                                )
                                Text(
                                    text = "تباين فائق للخط العربي مع خلفية داكنة هادئة",
                                    fontSize = 11.sp,
                                    color = if (isDarkMode) Color.LightGray else Color.Gray
                                )
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldAccent,
                                checkedTrackColor = QuranGreen,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        )
                    }

                    if (isDarkMode) {
                        Surface(
                            color = Color(0xFF09100C),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "✨ معاينة عالية التباين: ﴿ الرَّحْمَٰنِ الرَّحِيمِ ﴾ ناصعة ومريحة لعينيك في الظلام",
                                fontSize = 11.sp,
                                color = GoldAccent,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // 4. إعدادات جودة الصوت وحجمه (High Quality & Volume)
            // ========================================================
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔊 جودة الصوت وحجمه (Sound Quality & Volume):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = QuranGreen
                )
            }
            Text(
                text = "ضبط مشغل الصوت ليعمل بنقاء عالي (High Bitrate) ورفع الصوت للحد الأقصى:",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
            )

            // Bitrate selector
            Text(text = "معدل بث نقاء الصوت (Bitrate):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuranGreen)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair(128, "128 kbps (جودة قياسية)"),
                    Pair(192, "192 kbps (جودة فائقة HD) ⭐")
                ).forEach { (bitrate, label) ->
                    val isSelected = currentBitrate == bitrate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) QuranGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, if (isSelected) GoldAccent else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onBitrateChange(bitrate) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) QuranGreen else Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // تضخيم الصوت
            Text(text = "تضخيم ومعايرة صوت التلاوة:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuranGreen)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair(0, "عادي (100%)"),
                    Pair(800, "تضخيم (+8 dB)"),
                    Pair(1400, "أقصى قوة (+14 dB) 🚀")
                ).forEach { (gain, label) ->
                    val isSelected = currentVolumeGainMb == gain
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) QuranGreen.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, if (isSelected) GoldAccent else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onVolumeGainChange(gain) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) QuranGreen else Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onMaximizeSystemVolume,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = QuranGreen)
                Spacer(modifier = Modifier.width(6.dp))
                Text("رفع صوت وسائط الجهاز إلى الحد الأقصى (Max Volume)", fontSize = 12.sp, color = QuranGreen, fontWeight = FontWeight.Bold)
            }

            // Background Audio Info Card
            Surface(
                color = QuranGreen.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, QuranGreen.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = QuranGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "المعالجة بالخلفية مفعّلة (Background Audio)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = QuranGreen
                        )
                        Text(
                            text = "تستمر التلاوة العطرة بالعمل حتى لو تنقلت بين شاشات التطبيق أو أطفأت شاشة الهاتف.",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * نافذة الانتقال السريع
 */
@Composable
fun QuranQuickJumpDialog(
    currentPage: Int,
    onJumpToPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var pageInput by remember { mutableStateOf(currentPage.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "انتقال إلى صفحة أو جزء",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = QuranGreen
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "أدخل رقم الصفحة من 1 إلى 604:", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 3) {
                            pageInput = input
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "أو اختر جزءاً مباشراً:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuranGreen)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val juzStarts = listOf(
                        Pair(1, 1), Pair(2, 22), Pair(3, 42), Pair(4, 62), Pair(5, 82),
                        Pair(6, 102), Pair(7, 121), Pair(8, 142), Pair(9, 162), Pair(10, 182),
                        Pair(15, 282), Pair(20, 382), Pair(25, 482), Pair(28, 542), Pair(29, 562), Pair(30, 582)
                    )
                    items(juzStarts) { (juz, page) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(QuranGreen.copy(alpha = 0.1f))
                                .clickable { onJumpToPage(page) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("الجزء $juz (ص $page)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QuranGreen)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = pageInput.toIntOrNull()
                    if (target != null && target in 1..604) {
                        onJumpToPage(target)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = QuranGreen)
            ) {
                Text("انتقال ➔", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

fun getSurahName(page: Int): String {
    val surah = QuranDataSource.getSurahForPage(page)
    return "سورة ${surah.nameArabic}"
}

fun getJuzName(page: Int): String {
    val juzNumber = QuranDataSource.getJuzForPage(page)
    return "الجزء ${toEasternArabicDigits(juzNumber)}"
}

fun toEasternArabicDigits(number: Int): String {
    val easternDigits = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return number.toString().map { char ->
        if (char in '0'..'9') easternDigits[char - '0'] else char
    }.joinToString("")
}
