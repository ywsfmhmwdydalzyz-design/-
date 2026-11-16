package com.example.ui.components

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.models.Reciter
import com.example.data.sources.QuranDataSource
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.utils.AudioPlayerManager
import com.example.utils.NotificationPreferences
import com.example.utils.OngoingPrayerNotificationManager
import com.example.utils.QuranFontSettings
import com.example.utils.SakinaNotificationManager

@Composable
fun NotificationSettingsDialog(
    notificationManager: SakinaNotificationManager,
    audioPlayerManager: AudioPlayerManager? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val settings by notificationManager.settings.collectAsState()
    val fontSizeSp by QuranFontSettings.fontSizeSp.collectAsState()
    val playerState = audioPlayerManager?.uiState?.collectAsState()?.value
    val selectedReciter = playerState?.currentReciter ?: QuranDataSource.reciters[0]

    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            notificationManager.sendTestNotification("welcome")
        }
    }

    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .testTag("notification_settings_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
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
                            .background(EmeraldGreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "إعدادات سكينة الشاملة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = EmeraldGreenDark
                        )
                        Text(
                            text = "الإشعارات، القراء، وحجم الخط",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "إغلاق")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Permission Warning Banner if notification is not granted
                if (!hasPermission) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ يلزم تفعيل إذن الإشعارات",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "لتلقي مواقيت الأذان الدقيقة، والأدعية والأذكار بدون انقطاع.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("request_notification_permission_button")
                            ) {
                                Text("تفعيل الإشعارات الآن بنقرة واحدة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Toast/Feedback banner if a test was sent
                if (successMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldGreenPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = successMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldGreenDark
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ==========================================
                // 0. مظهر التطبيق والوضع الليلي (Theme & Night Mode)
                // ==========================================
                val currentThemeMode by com.example.utils.ThemeManager.themeMode.collectAsState()
                NotificationSectionTitle(title = "مظهر التطبيق والوضع الليلي (Theme & Night Mode)")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(GoldAccent.copy(alpha = 0.4f), Color.Transparent))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🎨 مظهر التطبيق:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentThemeMode == com.example.utils.AppThemeMode.LIGHT) "النهاري (افتراضي)" else if (currentThemeMode == com.example.utils.AppThemeMode.DARK) "الليلي" else "النظام",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // زر التبديل السريع بين الوضعين
                            val isDark = com.example.utils.ThemeManager.isDark
                            IconButton(
                                onClick = { com.example.utils.ThemeManager.toggleTheme() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreenPrimary.copy(alpha = 0.12f))
                                    .testTag("settings_theme_quick_toggle")
                            ) {
                                Text(
                                    text = if (isDark) "☀️" else "🌙",
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Three mode chips: Light, Dark, System
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(
                                Triple(com.example.utils.AppThemeMode.LIGHT, "☀️ النهاري (افتراضي)", "theme_chip_light"),
                                Triple(com.example.utils.AppThemeMode.DARK, "🌙 الليلي", "theme_chip_dark"),
                                Triple(com.example.utils.AppThemeMode.SYSTEM, "⚙️ النظام", "theme_chip_system")
                            )

                            modes.forEach { (mode, label, tag) ->
                                val isSelected = currentThemeMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { com.example.utils.ThemeManager.setThemeMode(mode) },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreenPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag(tag)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentThemeMode.subtitleArabic,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 1. الإشعار الثابت والمستمر (Ongoing Notification)
                // ==========================================
                NotificationSectionTitle(title = "الإشعار الثابت والمستمر (Ongoing Notification)")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(GoldAccent.copy(alpha = 0.4f), Color.Transparent))
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "تثبيت إشعار مواقيت الصلاة في الأعلى",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = EmeraldGreenDark
                                    )
                                    Text(
                                        text = "إشعار دائم لا يُحذف بالسحب يعرض المحافظة، التاريخين الهجري والميلادي، الصلاة القادمة، وعداد تنازلي دقيق.",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Switch(
                                checked = settings.ongoingNotification,
                                onCheckedChange = {
                                    notificationManager.updateSettings(settings.copy(ongoingNotification = it))
                                    if (it) {
                                        OngoingPrayerNotificationManager.startService(context)
                                        successMessage = "تم تفعيل الإشعار الثابت وخدمة العداد المباشر بنجاح"
                                    } else {
                                        OngoingPrayerNotificationManager.stopService(context)
                                        successMessage = "تم إيقاف الإشعار الثابت"
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldGreenPrimary
                                )
                            )
                        }

                        // بطاقة استثناء الحفظ العميق للبطارية (Battery Optimization Bypass)
                        var isBatteryIgnored by remember {
                            mutableStateOf(com.example.utils.BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            color = if (isBatteryIgnored) EmeraldGreenPrimary.copy(alpha = 0.08f) else Color(0xFFFFF3CD),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isBatteryIgnored) EmeraldGreenPrimary.copy(alpha = 0.3f) else Color(0xFFFFEEBA))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Text(if (isBatteryIgnored) "⚡" else "🔋", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "استثناء الحفظ العميق للبطارية",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isBatteryIgnored) EmeraldGreenPrimary else Color(0xFF856404)
                                            )
                                            Text(
                                                text = if (isBatteryIgnored) "مفعّل ✓ يستمر العداد التنازلي ثانية بثانية بدون توقف" else "موصى به لضمان عدم توقف عداد الصلاة عند قفل الشاشة",
                                                fontSize = 10.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                    if (!isBatteryIgnored) {
                                        Button(
                                            onClick = {
                                                com.example.utils.BatteryOptimizationHelper.requestIgnoreBatteryOptimization(context)
                                                isBatteryIgnored = com.example.utils.BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("استثناء الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (settings.ongoingNotification) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        OngoingPrayerNotificationManager.startService(context)
                                        successMessage = "تم تحديث الإشعار الثابت والخدمة في شريط الإشعارات"
                                    }
                                ) {
                                    Text("تحديث الإشعار الآن 🔄", fontSize = 11.sp, color = EmeraldGreenPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 2. إشعارات الأذان الدقيقة (بدون أي تكرار أو تقديم 15 دقيقة)
                // ==========================================
                NotificationSectionTitle(title = "مواقيت الأذان والصلوات (دقة 100%)")

                NotificationToggleCard(
                    title = "إطلاق الأذان في موعد الصلاة المضبوط بدقة",
                    subtitle = "ينطلق الأذان مرة واحدة فقط في وقت الصلاة 100% (تم إلغاء منبه الـ 15 دقيقة المسبق نهائياً)",
                    icon = Icons.Filled.Mosque,
                    iconTint = EmeraldGreenPrimary,
                    isChecked = settings.exactPrayerAlerts,
                    onCheckedChange = {
                        notificationManager.updateSettings(settings.copy(exactPrayerAlerts = it))
                    },
                    onTestClick = {
                        notificationManager.sendTestNotification("exact_prayer")
                        successMessage = "تم إرسال إشعار الأذان الدقيق"
                    },
                    testTag = "toggle_exact_prayer"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 3. إشعارات الأدعية والأذكار المتجددة
                // ==========================================
                NotificationSectionTitle(title = "إشعارات الأدعية والأذكار المتجددة")

                NotificationToggleCard(
                    title = "إشعارات الأدعية الدورية المتجددة 🤲",
                    subtitle = "تنبيهات يومية متجددة بأدعية نبوية وقرآنية مباركة في أوقات متفرقة",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    iconTint = GoldAccent,
                    isChecked = settings.periodicDuas,
                    onCheckedChange = {
                        notificationManager.updateSettings(settings.copy(periodicDuas = it))
                    },
                    onTestClick = {
                        notificationManager.sendTestNotification("dua")
                        successMessage = "تم إرسال دعاء مبارك متجدد الآن"
                    },
                    testTag = "toggle_periodic_duas"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Prophet Blessing Periodic Toggle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_prophet_prayer"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(GoldAccent.copy(alpha = 0.3f), Color.Transparent))
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("ﷺ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldGreenDark)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "الصلاة على النبي ﷺ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = EmeraldGreenDark
                                    )
                                    Text(
                                        text = "ﷺ أكثر من الصلاة والسلام على نبينا محمد",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Switch(
                                checked = settings.prophetReminders,
                                onCheckedChange = {
                                    notificationManager.updateSettings(settings.copy(prophetReminders = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EmeraldGreenPrimary
                                )
                            )
                        }

                        if (settings.prophetReminders) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "تكرار التذكير:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EmeraldGreenDark
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = settings.prophetIntervalHours == 1,
                                        onClick = {
                                            notificationManager.updateSettings(settings.copy(prophetIntervalHours = 1))
                                        },
                                        label = { Text("كل ساعة", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldGreenPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )

                                    FilterChip(
                                        selected = settings.prophetIntervalHours == 2,
                                        onClick = {
                                            notificationManager.updateSettings(settings.copy(prophetIntervalHours = 2))
                                        },
                                        label = { Text("كل ساعتين", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldGreenPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )

                                    TextButton(
                                        onClick = {
                                            notificationManager.sendTestNotification("prophet")
                                            successMessage = "تم إرسال تذكير: الصلاة على النبي ﷺ"
                                        }
                                    ) {
                                        Text("اختبار", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Morning Azkar (07:00 AM)
                NotificationToggleCard(
                    title = "أذكار الصباح (07:00 ص)",
                    subtitle = "صباح الخير.. لا تنسَ أذكار الصباح ☀️",
                    icon = Icons.Filled.WbSunny,
                    iconTint = Color(0xFFD97706),
                    isChecked = settings.morningAzkar,
                    onCheckedChange = {
                        notificationManager.updateSettings(settings.copy(morningAzkar = it))
                    },
                    onTestClick = {
                        notificationManager.sendTestNotification("morning")
                        successMessage = "تم إرسال إشعار: أذكار الصباح"
                    },
                    testTag = "toggle_morning_azkar"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Evening Azkar (05:00 PM)
                NotificationToggleCard(
                    title = "أذكار المساء (05:00 م)",
                    subtitle = "أذكار المساء حصنك حصين 🌙",
                    icon = Icons.Filled.WbTwilight,
                    iconTint = Color(0xFF4F46E5),
                    isChecked = settings.eveningAzkar,
                    onCheckedChange = {
                        notificationManager.updateSettings(settings.copy(eveningAzkar = it))
                    },
                    onTestClick = {
                        notificationManager.sendTestNotification("evening")
                        successMessage = "تم إرسال إشعار: أذكار المساء"
                    },
                    testTag = "toggle_evening_azkar"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bedtime Azkar & Surah Al-Mulk (10:30 PM)
                NotificationToggleCard(
                    title = "سورة الملك وأذكار النوم",
                    subtitle = "استغفر الله العظيم وتذكر قراءة سورة الملك وأذكار النوم ✨",
                    icon = Icons.Filled.Bedtime,
                    iconTint = Color(0xFF6366F1),
                    isChecked = settings.sleepAzkar,
                    onCheckedChange = {
                        notificationManager.updateSettings(settings.copy(sleepAzkar = it))
                    },
                    onTestClick = {
                        notificationManager.sendTestNotification("sleep")
                        successMessage = "تم إرسال إشعار: سورة الملك وأذكار النوم"
                    },
                    testTag = "toggle_sleep_azkar"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Friday Sunan Reminder
                NotificationToggleCard(
                    title = "سنن يوم الجمعة المباركة",
                    subtitle = "تذكير بقراءة سورة الكهف، والصلاة على النبي ﷺ 📖",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    iconTint = EmeraldGreenPrimary,
                    isChecked = settings.fridaySunan,
                    onCheckedChange = {
                        notificationManager.updateSettings(settings.copy(fridaySunan = it))
                    },
                    onTestClick = {
                        notificationManager.sendTestNotification("friday")
                        successMessage = "تم إرسال إشعار: سنن يوم الجمعة"
                    },
                    testTag = "toggle_friday_sunan"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Daily Wird Reminder (08:30 PM)
                NotificationToggleCard(
                    title = "تذكير الورد اليومي (08:30 م)",
                    subtitle = "هل قرأت وردك اليومي من القرآن والأذكار اليوم؟ 📖✨",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    iconTint = GoldAccent,
                    isChecked = settings.dailyWirdReminder,
                    onCheckedChange = {
                        notificationManager.updateSettings(settings.copy(dailyWirdReminder = it))
                    },
                    onTestClick = {
                        notificationManager.sendTestNotification("daily_wird")
                        successMessage = "تم إرسال تذكير: الورد اليومي من القرآن والأذكار"
                    },
                    testTag = "toggle_daily_wird_reminder"
                )

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 4. اختيار القارئ المعتمد (Approved Reciters)
                // ==========================================
                NotificationSectionTitle(title = "اختيار القارئ المعتمد للتلاوة")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "اختر القارئ المفضل لتلاوة القرآن الكريم:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        QuranDataSource.reciters.forEach { reciter ->
                            val isSelected = selectedReciter.id == reciter.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        audioPlayerManager?.selectReciter(reciter)
                                        successMessage = "تم اختيار القارئ: ${reciter.nameArabic}"
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) EmeraldGreenPrimary.copy(alpha = 0.12f) else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreenPrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) EmeraldGreenPrimary else Color.LightGray.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else Color.DarkGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = reciter.nameArabic,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 13.sp,
                                                color = if (isSelected) EmeraldGreenDark else Color.DarkGray
                                            )
                                            Text(
                                                text = reciter.style,
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = EmeraldGreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 5. إعدادات حجم الخط (Font Size)
                // ==========================================
                NotificationSectionTitle(title = "إعدادات حجم الخط (Font Size)")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.FormatSize,
                                    contentDescription = null,
                                    tint = EmeraldGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "الحجم الحالي: ${fontSizeSp.toInt()} sp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenDark
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalButton(
                                    onClick = { QuranFontSettings.decreaseFont() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    enabled = fontSizeSp > QuranFontSettings.MIN_FONT_SIZE
                                ) {
                                    Text("A- تصغير", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { QuranFontSettings.increaseFont() },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    enabled = fontSizeSp < QuranFontSettings.MAX_FONT_SIZE
                                ) {
                                    Text("A+ تكبير", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Slider(
                            value = fontSizeSp,
                            onValueChange = { QuranFontSettings.setFontSize(it) },
                            valueRange = QuranFontSettings.MIN_FONT_SIZE..QuranFontSettings.MAX_FONT_SIZE,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldGreenPrimary,
                                activeTrackColor = EmeraldGreenPrimary,
                                inactiveTrackColor = Color.LightGray
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // معاينة حية للنص
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ • أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                                fontSize = fontSizeSp.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 6. مشاركة التطبيق ورابط التحميل (Share App & Download Link)
                // ==========================================
                NotificationSectionTitle(title = "مشاركة التطبيق ورابط التحميل 📱")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_app_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(GoldAccent.copy(alpha = 0.6f), EmeraldGreenPrimary.copy(alpha = 0.4f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreenPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = null,
                                    tint = EmeraldGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تطبيق سكينة الإسلامي (يعمل ١٠٠٪ أوفلاين)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = EmeraldGreenDark
                                )
                                Text(
                                    text = "انشر تؤجر.. شارك التطبيق مع الأهل والأصدقاء لقراءة القرآن ومعرفة مواقيت الصلاة وسماع الأذان بدون إنترنت",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Box showing download link
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
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
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
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
                                    .testTag("share_app_action_button")
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
                                onClick = {
                                    com.example.utils.AppShareHelper.copyLink(context)
                                    successMessage = "تم نسخ رابط تحميل التطبيق إلى الحافظة بنجاح 📋"
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_download_link_button")
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
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ وإغلاق", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    notificationManager.sendTestNotification("general")
                    successMessage = "تم إرسال إشعار تجريبي عام الآن!"
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إشعار تجريبي", fontSize = 12.sp, color = EmeraldGreenDark)
            }
        }
    )
}

@Composable
private fun NotificationSectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = EmeraldGreenDark,
        modifier = Modifier.padding(bottom = 6.dp, top = 2.dp)
    )
}

@Composable
private fun NotificationToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTestClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = EmeraldGreenDark
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }

                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldGreenPrimary
                    )
                )
            }

            if (isChecked) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onTestClick) {
                        Text("إرسال تجربة", fontSize = 11.sp, color = EmeraldGreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
