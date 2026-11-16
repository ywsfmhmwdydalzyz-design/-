package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WorkspacePremium
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.example.utils.KhatmahStorage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.WirdUnit
import com.example.data.sources.DailyHadithDataSource
import com.example.ui.components.AyahOfTheDayWidget
import com.example.ui.components.HadithOfTheDayWidget
import com.example.ui.components.HijriCalendarDialog
import com.example.ui.components.HijriDashboardWidget
import com.example.ui.components.SakinaTab
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.utils.PrayerTimeCalculator
import com.example.utils.WirdStorage
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    wirdStorage: WirdStorage,
    bookmarkStorage: com.example.utils.QuranBookmarkStorage? = null,
    khatmahStorage: KhatmahStorage? = null,
    onNavigateTab: (SakinaTab) -> Unit,
    onOpenQuranPage: (Int) -> Unit = {},
    onOpenTasbih: () -> Unit = {},
    onOpenKhatmah: () -> Unit = {},
    onOpenPrayerSettings: () -> Unit,
    onOpenQibla: () -> Unit = {}
) {
    val context = LocalContext.current
    val wirdState by wirdStorage.wirdState.collectAsState()
    val todayAyah = remember { DailyHadithDataSource.getTodayAyah() }
    val todayHadith = remember { DailyHadithDataSource.getTodayHadith() }
    val todaySakina = remember { DailyHadithDataSource.getTodaySakinaMessage() }

    val locationData by com.example.utils.LocationPrayerManager.locationData.collectAsState()

    // Live real-time countdown for next prayer based on user coordinates
    var countdown by remember(locationData) {
        mutableStateOf(PrayerTimeCalculator.getNextPrayerCountdown(locationData.latitude, locationData.longitude))
    }
    LaunchedEffect(locationData) {
        while (true) {
            countdown = PrayerTimeCalculator.getNextPrayerCountdown(locationData.latitude, locationData.longitude)
            delay(1000L)
        }
    }

    // Dialog state for "ضبط الورد"
    var showWirdConfigDialog by remember { mutableStateOf(false) }

    // Interactive Hijri Calendar Dialog state
    var showHijriCalendarDialog by remember { mutableStateOf(false) }
    var hijriDayOffset by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("home_screen_content")
    ) {
        // App Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(EmeraldGreenDark, EmeraldGreenPrimary)
                    )
                )
                .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "سكينة الإسلامي",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccentLight
                        )
                    }
                    Text(
                        text = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "📍 حسب توقيت: ${locationData.cityName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoldAccentLight.copy(alpha = 0.9f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isDark = com.example.utils.ThemeManager.isDark

                    // زر تبديل الوضع الليلي (أيقونة الشمس / القمر)
                    IconButton(
                        onClick = { com.example.utils.ThemeManager.toggleTheme() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("home_theme_toggle_button")
                    ) {
                        Text(
                            text = if (isDark) "☀️" else "🌙",
                            fontSize = 18.sp
                        )
                    }

                    // زر مشاركة التطبيق المباشر (أيقونة المشاركة)
                    IconButton(
                        onClick = { com.example.utils.AppShareHelper.shareApp(context) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("home_header_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "مشاركة التطبيق 📱",
                            tint = GoldAccentLight
                        )
                    }

                    IconButton(
                        onClick = onOpenPrayerSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .testTag("home_notification_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "إعدادات الأذان والتنبيهات والمظهر والمشاركة",
                            tint = GoldAccentLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. عنصر الصلاة القادمة Next Prayer Widget
        NextPrayerWidget(
            prayerName = countdown.prayerName,
            countdownFormatted = countdown.remainingFormatted,
            hours = countdown.hoursRemaining,
            minutes = countdown.minutesRemaining,
            seconds = countdown.secondsRemaining,
            onClick = { onNavigateTab(SakinaTab.PRAYERS) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // التقويم الهجري التفاعلي والمناسبات الإسلامية Interactive Hijri Calendar Widget
        HijriDashboardWidget(
            dayOffset = hijriDayOffset,
            onOpenCalendar = { showHijriCalendarDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // متابع ختمة القرآن الكريم وتاريخ الانتهاء
        if (khatmahStorage != null) {
            val khatmahPlan by khatmahStorage.khatmahState.collectAsState()
            HomeKhatmahTrackerWidget(
                plan = khatmahPlan,
                onOpenKhatmah = onOpenKhatmah,
                onOpenQuran = { onOpenQuranPage(khatmahPlan.currentPage) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. متبع الورد اليومي الذكي Smart Daily Wird Tracker
        SmartWirdTrackerWidget(
            wirdState = wirdState,
            onIncrement = { wirdStorage.increment() },
            onDecrement = { wirdStorage.decrement() },
            onConfigure = { showWirdConfigDialog = true },
            onReadQuran = { onNavigateTab(SakinaTab.QURAN) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // بطاقة استئناف قراءة القرآن الكريم والعلامات المرجعية (Last Read / Bookmarks)
        if (bookmarkStorage != null) {
            val lastPage by bookmarkStorage.lastReadPage.collectAsState()
            val bookmarksList by bookmarkStorage.bookmarks.collectAsState()
            val pageInfo = remember(lastPage) { com.example.data.sources.QuranDataSource.getPageData(lastPage) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenQuranPage(lastPage) }
                    .testTag("home_last_read_quran_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, EmeraldGreenPrimary)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "متابعة قراءة المصحف الشريف",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (bookmarksList.any { it.pageNumber == lastPage }) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("🔖", fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = "صفحة $lastPage • سورة ${pageInfo.surah.nameArabic} (الجزء ${pageInfo.juzNumber})",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Button(
                        onClick = { onOpenQuranPage(lastPage) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("متابعة 📖", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. آية اليوم بتصميم إسلامي جذاب تتغير يومياً تلقائياً بدون إنترنت
        AyahOfTheDayWidget(
            ayah = todayAyah,
            onOpenQuranPage = onOpenQuranPage
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. حديث اليوم النبوي الشريف بتصميم إسلامي فاخر يتغير يومياً بدون إنترنت
        HadithOfTheDayWidget(
            hadith = todayHadith,
            onOpenNawawi = { onNavigateTab(SakinaTab.NAWAWI) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. رسالة سكينة لك اليوم Sakina Motivational Card
        SakinaMessageWidget(sakina = todaySakina)

        Spacer(modifier = Modifier.height(16.dp))

        // Featured Interactive Qibla Compass Card
        val qiblaBearingHome = remember(locationData) {
            com.example.utils.QiblaCalculator.calculateQiblaBearing(locationData.latitude, locationData.longitude)
        }
        val qiblaDirArabicHome = remember(qiblaBearingHome) {
            com.example.utils.QiblaCalculator.getDirectionArabic(qiblaBearingHome)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenQibla() }
                .testTag("home_qibla_compass_banner"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(GoldAccent, EmeraldGreenPrimary.copy(alpha = 0.5f)))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(GoldAccent.copy(alpha = 0.25f), EmeraldGreenPrimary.copy(alpha = 0.15f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "بوصلة القبلة التفاعلية 🕋",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldGreenPrimary.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${qiblaBearingHome.toInt()}° $qiblaDirArabicHome",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "تحديد اتجاه الكعبة بالمستشعر المغناطيسي مع اهتزاز دقيق عند المحاذاة",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }

                Button(
                    onClick = { onOpenQibla() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("home_btn_open_qibla")
                ) {
                    Text("البوصلة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation Grid
        Text(
            text = "الوصول السريع",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "المصحف الشريف",
                    icon = Icons.Filled.MenuBook,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(SakinaTab.QURAN) },
                    testTag = "quick_quran"
                )
                QuickActionItem(
                    title = "الأذكار اليومية",
                    icon = Icons.Filled.Favorite,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(SakinaTab.AZKAR) },
                    testTag = "quick_azkar"
                )
                QuickActionItem(
                    title = "التقويم الهجري",
                    icon = Icons.Filled.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = { showHijriCalendarDialog = true },
                    testTag = "quick_hijri"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "مواقيت الصلاة",
                    icon = Icons.Filled.Schedule,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(SakinaTab.PRAYERS) },
                    testTag = "quick_prayers"
                )
                QuickActionItem(
                    title = "بوصلة القبلة",
                    icon = Icons.Filled.Explore,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenQibla() },
                    testTag = "quick_qibla"
                )
                QuickActionItem(
                    title = "مكتبة الأدعية",
                    icon = Icons.Filled.VolunteerActivism,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(SakinaTab.DUAS) },
                    testTag = "quick_duas"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    title = "الأربعين النووية",
                    icon = Icons.Filled.Book,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab(SakinaTab.NAWAWI) },
                    testTag = "quick_nawawi"
                )
                QuickActionItem(
                    title = "السبحة الإلكترونية",
                    icon = Icons.Filled.Fingerprint,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenTasbih() },
                    testTag = "quick_tasbih"
                )
                QuickActionItem(
                    title = "ختمة القرآن",
                    icon = Icons.Filled.WorkspacePremium,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenKhatmah() },
                    testTag = "quick_khatmah"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart Notification Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenPrayerSettings() }
                .testTag("home_smart_notifications_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = EmeraldGreenPrimary.copy(alpha = 0.08f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(GoldAccent.copy(alpha = 0.5f), Color.Transparent))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "نظام الإشعارات والتذكيرات الذكية",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "مواقيت الأذان الدقيقة، أذكار الصباح والمساء، وتذكير الورد اليومي",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }

                Button(
                    onClick = onOpenPrayerSettings,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    modifier = Modifier.testTag("btn_open_notification_settings")
                ) {
                    Text("ضبط", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // بطاقة مشاركة التطبيق ورابط التحميل المباشر
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_share_app_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(GoldAccent, EmeraldGreenPrimary))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مشاركة التطبيق 📱",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "انشر تؤجر.. شارك تطبيق سكينة مع الأهل والأصدقاء لتحميله واستخدامه بدون إنترنت",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = com.example.utils.AppShareHelper.PUBLIC_APP_URL,
                        fontSize = 11.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { com.example.utils.AppShareHelper.shareApp(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_share_app_action_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة التطبيق 📱", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { com.example.utils.AppShareHelper.copyLink(context) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_copy_download_link_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            tint = EmeraldGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ الرابط 📋", fontSize = 12.sp, color = EmeraldGreenDark)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // "ضبط الورد" Dialog
    if (showWirdConfigDialog) {
        WirdConfigDialog(
            currentTarget = wirdState.target,
            currentUnit = wirdState.unit,
            onDismiss = { showWirdConfigDialog = false },
            onSave = { newTarget, newUnit ->
                wirdStorage.setTarget(newTarget, newUnit)
                showWirdConfigDialog = false
            }
        )
    }

    // Interactive Hijri Calendar Dialog
    if (showHijriCalendarDialog) {
        HijriCalendarDialog(
            dayOffset = hijriDayOffset,
            onDayOffsetChange = { hijriDayOffset = it },
            onDismissRequest = { showHijriCalendarDialog = false }
        )
    }
}

@Composable
fun NextPrayerWidget(
    prayerName: String,
    countdownFormatted: String,
    hours: Int,
    minutes: Int,
    seconds: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("next_prayer_widget"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, EmeraldGreenPrimary)))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الصلاة القادمة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = prayerName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Countdown badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldAccent.copy(alpha = 0.15f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "متبقي حتى الأذان",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Timer Blocks (Hours, Minutes, Seconds)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeBlock(label = "ساعة", value = String.format("%02d", hours))
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                TimeBlock(label = "دقيقة", value = String.format("%02d", minutes))
                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                TimeBlock(label = "ثانية", value = String.format("%02d", seconds))
            }
        }
    }
}

@Composable
fun TimeBlock(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HomeKhatmahTrackerWidget(
    plan: com.example.data.models.KhatmahPlan,
    onOpenKhatmah: () -> Unit,
    onOpenQuran: () -> Unit
) {
    val progress = (plan.currentPage.toFloat() / plan.totalPages.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "home_khatmah_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenKhatmah)
            .testTag("home_khatmah_tracker_widget"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(EmeraldGreenPrimary, GoldAccent))
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ختمة القرآن الكريم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "الصفحة ${plan.currentPage} من ${plan.totalPages} (الجزء ${plan.currentJuz})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldGreenPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = plan.formattedPercentage,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = EmeraldGreenPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الختم المتوقع: ${plan.getFormattedEstimatedDate()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "متبقي ${plan.daysRemaining} يوماً",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }
        }
    }
}

@Composable
fun SmartWirdTrackerWidget(
    wirdState: com.example.data.models.WirdState,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onConfigure: () -> Unit,
    onReadQuran: () -> Unit
) {
    val progress = (wirdState.current.toFloat() / maxOf(1, wirdState.target).toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400),
        label = "wird_progress"
    )
    val percentage = (progress * 100).toInt()
    val isCompleted = wirdState.current >= wirdState.target

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_wird_tracker"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) EmeraldGreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(if (isCompleted) EmeraldGreenPrimary else GoldAccent, EmeraldGreenPrimary.copy(alpha = 0.5f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مُتَبِّع الورد اليومي الذكي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🎉 مكتمل", fontSize = 11.sp, color = EmeraldGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "الهدف: ${wirdState.target} ${wirdState.unit.labelArabic} يومياً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onConfigure,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("button_configure_wird")
                ) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "ضبط الورد", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar and Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "المنجز: ${wirdState.current} من ${wirdState.target} ${wirdState.unit.labelArabic}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$percentage%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) EmeraldGreenPrimary else GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (isCompleted) EmeraldGreenPrimary else GoldAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Action Controls: +1 and -1 buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrement,
                        enabled = wirdState.current > 0,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("wird_decrement_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Remove, contentDescription = "خصم", modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreenPrimary)
                            .testTag("wird_increment_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "إضافة", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SakinaMessageWidget(sakina: com.example.data.models.SakinaMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sakina_daily_message_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent.copy(alpha = 0.6f), EmeraldGreenPrimary.copy(alpha = 0.3f))))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "رسالة سكينة لك اليوم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sakina.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = sakina.message,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sakina.verseOrQuote,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldGreenPrimary
            )
        }
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreenPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = EmeraldGreenPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WirdConfigDialog(
    currentTarget: Int,
    currentUnit: WirdUnit,
    onDismiss: () -> Unit,
    onSave: (Int, WirdUnit) -> Unit
) {
    var targetInput by remember { mutableStateOf(currentTarget.toString()) }
    var selectedUnit by remember { mutableStateOf(currentUnit) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "ضبط الورد اليومي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        },
        text = {
            Column {
                Text(text = "اختر وحدة القراءة وهدفك اليومي:", fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WirdUnit.values().forEach { unit ->
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = { selectedUnit = unit },
                            label = { Text(text = unit.labelArabic, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("الكمية المستهدفة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = targetInput.toIntOrNull() ?: 10
                    onSave(count, selectedUnit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
