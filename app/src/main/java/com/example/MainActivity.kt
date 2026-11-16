package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.Intent
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.components.NotificationSettingsDialog
import com.example.ui.components.SakinaBottomBar
import com.example.ui.components.SakinaTab
import com.example.ui.screens.AzanPlayerScreen
import com.example.ui.screens.AzkarScreen
import com.example.ui.screens.DuasScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KhatmahScreen
import com.example.ui.screens.NawawiScreen
import com.example.ui.screens.PrayerScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.theme.SakinaTheme
import com.example.utils.AthanManager
import com.example.utils.AudioPlayerManager
import com.example.utils.KhatmahStorage
import com.example.utils.LocationPrayerManager
import com.example.utils.OngoingPrayerNotificationManager
import com.example.utils.PrayerTimeManager
import com.example.utils.QuranBookmarkStorage
import com.example.utils.QuranFontSettings
import com.example.utils.SakinaNotificationManager
import com.example.utils.WirdStorage
import com.example.utils.PlaybackState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var athanManager: AthanManager
    private lateinit var wirdStorage: WirdStorage
    private lateinit var bookmarkStorage: QuranBookmarkStorage
    private lateinit var khatmahStorage: KhatmahStorage
    private lateinit var notificationManager: SakinaNotificationManager

    private var showAzanScreenOnStart by mutableStateOf(false)
    private var azanPrayerNameOnStart by mutableStateOf("صلاة الفجر")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // إضاءة الشاشة وفتحها فوق شاشة القفل عند موعد الأذان
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        handleAzanIntent(intent)

        audioPlayerManager = AudioPlayerManager(applicationContext)
        athanManager = AthanManager(applicationContext)
        wirdStorage = WirdStorage(applicationContext)
        bookmarkStorage = QuranBookmarkStorage(applicationContext)
        khatmahStorage = KhatmahStorage(applicationContext)
        notificationManager = SakinaNotificationManager(applicationContext)
        LocationPrayerManager.initialize(applicationContext)
        QuranFontSettings.init(applicationContext)
        com.example.utils.ThemeManager.init(applicationContext)
        com.example.data.sources.OfflineQuranManager.init(applicationContext)
        OngoingPrayerNotificationManager.startService(applicationContext)

        setContent {
            val isDark = com.example.utils.ThemeManager.isDark

            SakinaTheme(darkTheme = isDark) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SakinaApp(
                        audioPlayerManager = audioPlayerManager,
                        athanManager = athanManager,
                        wirdStorage = wirdStorage,
                        bookmarkStorage = bookmarkStorage,
                        khatmahStorage = khatmahStorage,
                        notificationManager = notificationManager,
                        initialShowAzan = showAzanScreenOnStart,
                        initialAzanPrayer = azanPrayerNameOnStart
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        OngoingPrayerNotificationManager.startService(applicationContext)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAzanIntent(intent)
    }

    private fun handleAzanIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("SHOW_AZAN_SCREEN", false) == true) {
            val prayer = intent.getStringExtra("PRAYER_NAME") ?: "الصلاة"
            azanPrayerNameOnStart = prayer
            showAzanScreenOnStart = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audioPlayerManager.isInitialized) {
            audioPlayerManager.release()
        }
        if (::athanManager.isInitialized) {
            athanManager.stopAthan()
        }
    }
}

@Composable
fun SakinaApp(
    audioPlayerManager: AudioPlayerManager,
    athanManager: AthanManager,
    wirdStorage: WirdStorage,
    bookmarkStorage: QuranBookmarkStorage,
    khatmahStorage: KhatmahStorage,
    notificationManager: SakinaNotificationManager,
    initialShowAzan: Boolean = false,
    initialAzanPrayer: String = "الصلاة"
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showAzanPlayer by remember(initialShowAzan) { mutableStateOf(initialShowAzan) }
    var azanPrayerName by remember(initialAzanPrayer) { mutableStateOf(initialAzanPrayer) }
    var currentTab by remember { mutableStateOf(SakinaTab.HOME) }
    var prayerInitialSection by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var azkarInitialTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var targetQuranPage by remember { mutableStateOf<Int?>(null) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var showKhatmahScreen by remember { mutableStateOf(false) }

    // One-click notification permission launcher at app startup
    var hasCheckedPermission by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        OngoingPrayerNotificationManager.startService(context)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationManager.sendTestNotification("welcome")
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!hasCheckedPermission) {
            hasCheckedPermission = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val status = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                if (status != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    // Back handling: If on Azan screen, Khatmah screen or secondary tab, return properly
    BackHandler(enabled = showAzanPlayer || showKhatmahScreen || currentTab != SakinaTab.HOME) {
        if (showAzanPlayer) {
            context.stopService(Intent(context, com.example.services.AzanService::class.java))
            showAzanPlayer = false
        } else if (showKhatmahScreen) {
            showKhatmahScreen = false
        } else {
            currentTab = SakinaTab.HOME
        }
    }

    var isQuranControlsHidden by remember { mutableStateOf(false) }

    if (showAzanPlayer) {
        AzanPlayerScreen(
            prayerName = azanPrayerName,
            onDismiss = { showAzanPlayer = false }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sakina_main_scaffold"),
        bottomBar = {
            if (!showKhatmahScreen && !(currentTab == SakinaTab.QURAN && isQuranControlsHidden)) {
                Column {
                    val playerState by audioPlayerManager.uiState.collectAsState()
                    val isPlaying = playerState.playbackState == PlaybackState.PLAYING
                    val isPaused = playerState.playbackState == PlaybackState.PAUSED
                    val isBuffering = playerState.playbackState == PlaybackState.BUFFERING

                    if (currentTab != SakinaTab.QURAN && (isPlaying || isPaused || isBuffering)) {
                        Surface(
                            color = Color(0xFF0D5338),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentTab = SakinaTab.QURAN }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
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
                                        tint = Color(0xFFD4AF37),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = playerState.currentSurah?.let { "سورة ${it.nameArabic}" } ?: "تلاوة القرآن الكريم",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${playerState.currentReciter.nameArabic} • تلاوة بالخلفية 192kbps",
                                            fontSize = 10.sp,
                                            color = Color(0xFFD4AF37)
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { audioPlayerManager.togglePlayPause() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { audioPlayerManager.stop() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "إيقاف",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    SakinaBottomBar(
                        currentTab = currentTab,
                        onTabSelected = {
                            if (it == SakinaTab.PRAYERS) {
                                prayerInitialSection = 0
                            }
                            if (it == SakinaTab.AZKAR) {
                                azkarInitialTab = 0
                            }
                            if (it == SakinaTab.QURAN) {
                                targetQuranPage = null // Open default/last read
                            } else {
                                isQuranControlsHidden = false
                            }
                            currentTab = it
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if ((currentTab == SakinaTab.QURAN && isQuranControlsHidden) || showKhatmahScreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
        ) {
            if (showKhatmahScreen) {
                KhatmahScreen(
                    khatmahStorage = khatmahStorage,
                    onNavigateBack = { showKhatmahScreen = false },
                    onOpenQuranPage = { page ->
                        showKhatmahScreen = false
                        targetQuranPage = page
                        currentTab = SakinaTab.QURAN
                    }
                )
            } else {
                when (currentTab) {
                    SakinaTab.HOME -> {
                        HomeScreen(
                            wirdStorage = wirdStorage,
                            bookmarkStorage = bookmarkStorage,
                            khatmahStorage = khatmahStorage,
                            onNavigateTab = {
                                prayerInitialSection = 0
                                if (it == SakinaTab.AZKAR) {
                                    azkarInitialTab = 0
                                }
                                if (it == SakinaTab.QURAN) {
                                    targetQuranPage = null
                                }
                                currentTab = it
                            },
                            onOpenQuranPage = { page ->
                                targetQuranPage = page
                                currentTab = SakinaTab.QURAN
                            },
                            onOpenTasbih = {
                                azkarInitialTab = 1
                                currentTab = SakinaTab.AZKAR
                            },
                            onOpenKhatmah = {
                                showKhatmahScreen = true
                            },
                            onOpenPrayerSettings = { showNotificationSettingsDialog = true },
                            onOpenQibla = {
                                prayerInitialSection = 1
                                currentTab = SakinaTab.PRAYERS
                            }
                        )
                    }
                    SakinaTab.QURAN -> {
                        QuranScreen(
                            audioPlayerManager = audioPlayerManager,
                            bookmarkStorage = bookmarkStorage,
                            khatmahStorage = khatmahStorage,
                            targetPage = targetQuranPage,
                            onOpenKhatmah = {
                                showKhatmahScreen = true
                            },
                            onControlsVisibilityChanged = { isVisible ->
                                isQuranControlsHidden = !isVisible
                            },
                            onNavigateHome = {
                                isQuranControlsHidden = false
                                currentTab = SakinaTab.HOME
                            }
                        )
                    }
                SakinaTab.AZKAR -> {
                    AzkarScreen(initialTab = azkarInitialTab)
                }
                SakinaTab.NAWAWI -> {
                    NawawiScreen()
                }
                SakinaTab.PRAYERS -> {
                    PrayerScreen(
                        athanManager = athanManager,
                        notificationManager = notificationManager,
                        onOpenNotificationSettings = { showNotificationSettingsDialog = true },
                        initialSection = prayerInitialSection
                    )
                }
                SakinaTab.DUAS -> {
                    DuasScreen()
                }
            }
        }
    }
}
}

    if (showNotificationSettingsDialog) {
        NotificationSettingsDialog(
            notificationManager = notificationManager,
            audioPlayerManager = audioPlayerManager,
            onDismiss = { showNotificationSettingsDialog = false }
        )
    }
}
