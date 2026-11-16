package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.services.AzanService
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.mutableIntStateOf
import com.example.utils.LocationPrayerManager
import com.example.utils.LocationData
import com.example.utils.QiblaCalculator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.models.PrayerTimeInfo
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.utils.AthanManager
import com.example.utils.PrayerTimeCalculator
import kotlinx.coroutines.delay

@Composable
fun PrayerScreen(
    athanManager: AthanManager,
    notificationManager: com.example.utils.SakinaNotificationManager? = null,
    onOpenNotificationSettings: () -> Unit = {},
    initialSection: Int = 0
) {
    val context = LocalContext.current
    var currentSection by remember(initialSection) { mutableIntStateOf(initialSection) }

    BackHandler(enabled = currentSection != 0) {
        currentSection = 0
    }

    val isAutoAthanEnabled by athanManager.isAutoAthanEnabled.collectAsState()
    val selectedVoiceId by athanManager.selectedVoiceId.collectAsState()
    val isPlayingAthan by athanManager.isPlayingAthan.collectAsState()
    val locationData by LocationPrayerManager.locationData.collectAsState()

    var showVoiceDropdown by remember { mutableStateOf(false) }
    var showCityDropdown by remember { mutableStateOf(false) }
    var locationStatusMessage by remember { mutableStateOf<String?>(null) }
    var showAzanPlayerScreen by remember { mutableStateOf(false) }
    var currentAzanPrayerName by remember { mutableStateOf("صلاة الفجر") }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            LocationPrayerManager.requestDeviceLocation(context) { success, msg ->
                locationStatusMessage = msg
            }
        } else {
            locationStatusMessage = "يرجى منح إذن الوصول إلى الموقع لحساب المواقيت بدقة"
        }
    }

    // Check notification permission state
    var hasNotificationPermission by remember {
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
        hasNotificationPermission = isGranted
        if (isGranted) {
            athanManager.sendReminderNotification("تم تفعيل إشعارات سكينة", "ستصلك الآن تنبيهات مواقيت الصلاة والأذكار في موعدها.")
        }
    }

    // Today's calculated prayer times based on precise coordinates
    val todayPrayers = remember(locationData) {
        PrayerTimeCalculator.calculateTodayPrayers(locationData.latitude, locationData.longitude)
    }

    // Live countdown based on user location
    var countdown by remember(locationData) {
        mutableStateOf(PrayerTimeCalculator.getNextPrayerCountdown(locationData.latitude, locationData.longitude))
    }
    val selectedVoice = remember(selectedVoiceId) {
        PrayerTimeCalculator.muadhinVoices.find { it.id == selectedVoiceId } ?: PrayerTimeCalculator.muadhinVoices[0]
    }

    LaunchedEffect(locationData, isAutoAthanEnabled) {
        var lastAthanPrayer = ""
        while (true) {
            val next = PrayerTimeCalculator.getNextPrayerCountdown(locationData.latitude, locationData.longitude)
            countdown = next
            // Auto Athan: Trigger audio playback immediately upon entering prayer time
            if (isAutoAthanEnabled && !isPlayingAthan && next.hoursRemaining == 0 && next.minutesRemaining == 0 && next.secondsRemaining <= 1) {
                if (lastAthanPrayer != next.prayerName) {
                    lastAthanPrayer = next.prayerName
                    athanManager.playAthan(selectedVoice)
                    athanManager.sendPrayerNotification(next.prayerName)
                }
            }
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_screen_root")
    ) {
        // Top Section Selector: مواقيت الصلاة | بوصلة القبلة
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
                .testTag("prayer_section_segmented_tabs"),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tab 1: مواقيت الصلاة
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (currentSection == 0) EmeraldGreenPrimary else Color.Transparent)
                    .clickable { currentSection = 0 }
                    .padding(vertical = 10.dp)
                    .testTag("subtab_prayers"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = if (currentSection == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مواقيت الصلاة والأذان",
                        fontSize = 12.sp,
                        fontWeight = if (currentSection == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentSection == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Tab 2: بوصلة القبلة
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (currentSection == 1) EmeraldGreenPrimary else Color.Transparent)
                    .clickable { currentSection = 1 }
                    .padding(vertical = 10.dp)
                    .testTag("subtab_qibla"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Explore,
                        contentDescription = null,
                        tint = if (currentSection == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "بوصلة القبلة التفاعلية 🕋",
                        fontSize = 12.sp,
                        fontWeight = if (currentSection == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (currentSection == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (currentSection == 1) {
            QiblaCompassScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("prayer_screen_content")
            ) {
        // Location Geolocation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("prayer_location_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldGreenPrimary.copy(alpha = 0.5f), GoldAccent.copy(alpha = 0.4f))))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
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
                                .background(EmeraldGreenPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Place,
                                contentDescription = null,
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "موقع حساب المواقيت",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${locationData.cityName} ${if (locationData.countryName.isNotEmpty()) "• ${locationData.countryName}" else ""}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Automatic GPS Location Button
                    Button(
                        onClick = {
                            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (fine || coarse) {
                                LocationPrayerManager.requestDeviceLocation(context) { success, msg ->
                                    locationStatusMessage = msg
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_request_gps_location")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "موقعي الحالي", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                // Preset cities trigger
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box {
                        OutlinedButton(
                            onClick = { showCityDropdown = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("select_city_dropdown_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("اختيار مدينة كبرى أخرى", fontSize = 12.sp, color = EmeraldGreenPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = EmeraldGreenPrimary, modifier = Modifier.size(16.dp))
                            }
                        }

                        DropdownMenu(
                            expanded = showCityDropdown,
                            onDismissRequest = { showCityDropdown = false }
                        ) {
                            LocationPrayerManager.PRESET_CITIES.forEach { cityPreset ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${cityPreset.cityName} (${cityPreset.countryName})", fontSize = 13.sp)
                                    },
                                    onClick = {
                                        LocationPrayerManager.setManualCity(cityPreset, context)
                                        showCityDropdown = false
                                        locationStatusMessage = "تم ضبط المواقيت حسب توقيت: ${cityPreset.cityName}"
                                    }
                                )
                            }
                        }
                    }

                    if (locationData.isAuto) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldGreenPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("تلقائي (GPS) ✓", fontSize = 10.sp, color = EmeraldGreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                locationStatusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = msg, fontSize = 11.sp, color = EmeraldGreenDark, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Date & Next Prayer Top Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(EmeraldGreenDark, EmeraldGreenPrimary)
                    )
                )
                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "سكينة الإسلامي",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccentLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = todayPrayers.dateFormatted,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = todayPrayers.hijriDate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccentLight
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "الصلاة القادمة: ${countdown.prayerName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "متبقي ${countdown.remainingFormatted}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldAccentLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Qibla Direction Banner
        val qiblaBearing = remember(locationData) {
            QiblaCalculator.calculateQiblaBearing(locationData.latitude, locationData.longitude)
        }
        val qiblaDirectionArabic = remember(qiblaBearing) {
            QiblaCalculator.getDirectionArabic(qiblaBearing)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { currentSection = 1 }
                .testTag("quick_qibla_banner_card"),
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Explore,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "اتجاه القبلة الشريفة 🕋",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = EmeraldGreenDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldGreenPrimary.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${qiblaBearing.toInt()}° $qiblaDirectionArabic",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreenPrimary
                                )
                            }
                        }
                        Text(
                            text = "اضغط لفتح البوصلة التفاعلية وتحديد القبلة بالمستشعر",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = { currentSection = 1 },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_open_qibla_compass")
                ) {
                    Text("فتح البوصلة", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auto-Athan & Hardware Notifications Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("athan_settings_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, EmeraldGreenPrimary.copy(alpha = 0.4f))))
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
                                .background(GoldAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.Campaign, contentDescription = null, tint = GoldAccent)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "المؤذن التلقائي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isAutoAthanEnabled) "يعمل في الخلفية عند دخول الوقت" else "معطل حالياً",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isAutoAthanEnabled,
                        onCheckedChange = { athanManager.setAutoAthanEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldGreenPrimary
                        ),
                        modifier = Modifier.testTag("auto_athan_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Muadhin Voice Selector Dropdown
                Text(
                    text = "صوت المؤذن المعتمد:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { showVoiceDropdown = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("muadhin_voice_dropdown_trigger")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = selectedVoice.nameArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = selectedVoice.location,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = GoldAccent)
                    }

                    DropdownMenu(
                        expanded = showVoiceDropdown,
                        onDismissRequest = { showVoiceDropdown = false }
                    ) {
                        PrayerTimeCalculator.muadhinVoices.forEach { voice ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = voice.nameArabic, fontWeight = FontWeight.Bold)
                                        Text(text = voice.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    athanManager.selectVoice(voice.id)
                                    showVoiceDropdown = false
                                },
                                modifier = Modifier.testTag("voice_item_${voice.id}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test Athan Play & Stop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (isPlayingAthan) {
                                athanManager.stopAthan()
                            } else {
                                athanManager.playAthan(selectedVoice)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayingAthan) Color(0xFFDC2626) else EmeraldGreenPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_athan_button")
                    ) {
                        Icon(
                            imageVector = if (isPlayingAthan) Icons.Filled.Stop else Icons.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isPlayingAthan) "إيقاف الأذان" else "تجربة الأذان")
                    }

                    Button(
                        onClick = {
                            currentAzanPrayerName = countdown.prayerName
                            val azanIntent = Intent(context, AzanService::class.java).apply {
                                putExtra("PRAYER_NAME", countdown.prayerName)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(azanIntent)
                            } else {
                                context.startService(azanIntent)
                            }
                            showAzanPlayerScreen = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_azan_service_screen")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = null,
                            tint = Color(0xFF1E3A2F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "شاشة الآذان والخدمة", color = Color(0xFF1E3A2F), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Native Push Notifications Status & Trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = null,
                                    tint = EmeraldGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "إشعارات النظام الأهلية (Push Notifications)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (hasNotificationPermission) "إذن الإشعارات مفعّل: ستتلقى التنبيهات في شريط الإشعارات مباشرة." else "يلزم منح إذن الإشعارات لتصلك تنبيهات الصلاة.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                    modifier = Modifier.testTag("request_notification_permission_button")
                                ) {
                                    Text("تفعيل الإذن", fontSize = 11.sp, color = Color.Black)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    athanManager.sendPrayerNotification(countdown.prayerName)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("send_test_prayer_notification_button")
                            ) {
                                Text("إرسال إشعار تجريبي", fontSize = 11.sp)
                            }

                            Button(
                                onClick = onOpenNotificationSettings,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                                modifier = Modifier.testTag("open_notification_settings_button")
                            ) {
                                Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إعدادات التنبيهات والأذكار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prayer Times Grid
        Text(
            text = "مواقيت صلوات اليوم",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val prayers = listOf(
                todayPrayers.fajr,
                todayPrayers.sunrise,
                todayPrayers.dhuhr,
                todayPrayers.asr,
                todayPrayers.maghrib,
                todayPrayers.isha
            )

            prayers.forEach { p ->
                PrayerTimeCard(prayer = p)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAzanPlayerScreen) {
        Dialog(
            onDismissRequest = {
                context.stopService(Intent(context, AzanService::class.java))
                showAzanPlayerScreen = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AzanPlayerScreen(
                prayerName = currentAzanPrayerName,
                onDismiss = {
                    showAzanPlayerScreen = false
                }
            )
        }
    }
}
}

@Composable
fun PrayerTimeCard(prayer: PrayerTimeInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_${prayer.nameEnglish.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (prayer.isNext) EmeraldGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (prayer.isNext) 2.dp else 1.dp),
        border = if (prayer.isNext) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, EmeraldGreenPrimary))) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (prayer.isNext) GoldAccent else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = if (prayer.isNext) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = prayer.nameArabic,
                        fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (prayer.isNext) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = prayer.nameEnglish,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (prayer.isNext) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "القادمة", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = prayer.formattedTime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (prayer.isNext) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
