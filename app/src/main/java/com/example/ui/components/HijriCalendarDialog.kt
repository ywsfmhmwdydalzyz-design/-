package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.models.HijriDate
import com.example.data.models.IslamicOccasion
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.utils.HijriCalendarHelper

@Composable
fun HijriCalendarDialog(
    dayOffset: Int,
    onDayOffsetChange: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val todayHijri = remember(dayOffset) { HijriCalendarHelper.getTodayHijri(dayOffset) }
    var currentMonth by remember { mutableIntStateOf(todayHijri.month) }
    var currentYear by remember { mutableIntStateOf(todayHijri.year) }
    var selectedDate by remember { mutableStateOf<HijriDate?>(todayHijri) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: تقويم الشهر, 1: المناسبات الإسلامية
    var showOffsetSelector by remember { mutableStateOf(false) }

    val monthInfo by remember(currentYear, currentMonth, dayOffset) {
        derivedStateOf {
            HijriCalendarHelper.getHijriMonthInfo(currentYear, currentMonth, dayOffset)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("hijri_calendar_dialog"),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreenPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "التقويم الهجري التفاعلي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark
                            )
                            Text(
                                text = "المطابق للتقويم الميلادي والمناسبات الشرعية",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showOffsetSelector = !showOffsetSelector },
                            modifier = Modifier.testTag("hijri_offset_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "تعديل الرؤية",
                                tint = if (dayOffset != 0) GoldAccent else EmeraldGreenPrimary
                            )
                        }

                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.testTag("hijri_calendar_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إغلاق",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // Moon sighting offset tuner bar
                AnimatedVisibility(visible = showOffsetSelector) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldAccent.copy(alpha = 0.08f))
                            .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ضبط رؤية الهلال (فارق الأيام):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreenDark
                            )
                            Text(
                                text = if (dayOffset == 0) "الافتراضي (0)" else "${if (dayOffset > 0) "+" else ""}$dayOffset يوم",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(-2, -1, 0, 1, 2).forEach { offset ->
                                FilterChip(
                                    selected = dayOffset == offset,
                                    onClick = { onDayOffsetChange(offset) },
                                    label = {
                                        Text(
                                            text = if (offset == 0) "0" else "${if (offset > 0) "+" else ""}$offset",
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: Monthly Calendar vs Occasions
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = EmeraldGreenPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "عرض الشهر",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) EmeraldGreenPrimary else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "المناسبات الإسلامية (${HijriCalendarHelper.ISLAMIC_OCCASIONS.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) EmeraldGreenPrimary else Color.Gray
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // TAB 0: Month Grid Calendar View
                    MonthCalendarContent(
                        monthInfo = monthInfo,
                        todayHijri = todayHijri,
                        selectedDate = selectedDate,
                        onSelectDate = { selectedDate = it },
                        onPrevMonth = {
                            if (currentMonth == 1) {
                                currentMonth = 12
                                currentYear -= 1
                            } else {
                                currentMonth -= 1
                            }
                        },
                        onNextMonth = {
                            if (currentMonth == 12) {
                                currentMonth = 1
                                currentYear += 1
                            } else {
                                currentMonth += 1
                            }
                        },
                        onGoToToday = {
                            currentMonth = todayHijri.month
                            currentYear = todayHijri.year
                            selectedDate = todayHijri
                        }
                    )
                } else {
                    // TAB 1: Islamic Occasions List View
                    IslamicOccasionsListContent(
                        todayHijri = todayHijri,
                        onSelectOccasion = { occasion ->
                            currentMonth = occasion.hijriMonth
                            selectedTab = 0
                            val targetDay = monthInfo.days.find { it.day == occasion.hijriDay }
                            if (targetDay != null) {
                                selectedDate = targetDay
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MonthCalendarContent(
    monthInfo: com.example.data.models.HijriMonthInfo,
    todayHijri: HijriDate,
    selectedDate: HijriDate?,
    onSelectDate: (HijriDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onGoToToday: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Month Selector Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = EmeraldGreenDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPrevMonth,
                    modifier = Modifier.testTag("hijri_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "الشهر السابق",
                        tint = GoldAccentLight
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${monthInfo.monthNameArabic} ${monthInfo.year} هـ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "شهر ${monthInfo.monthNumber} من السنة الهجرية (${monthInfo.daysCount} يوماً)",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldAccentLight.copy(alpha = 0.9f)
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("hijri_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الشهر القادم",
                        tint = GoldAccentLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // "اليوم" Return to today button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "انقر على أي يوم لمعرفة مناسباته والتاريخ الميلادي",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Button(
                onClick = onGoToToday,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("hijri_go_today")
            ) {
                Text("اليوم", fontSize = 12.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekday headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val weekDays = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
            weekDays.forEach { wDay ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = wDay,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (wDay == "جمعة") GoldAccent else EmeraldGreenDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar Grid (7 columns)
        val firstDay = monthInfo.days.firstOrNull()
        val firstDayOfWeek = if (firstDay != null) {
            // Find weekday index (0=Sunday..6=Saturday)
            val cal = HijriCalendarHelper.hijriToGregorian(firstDay.year, firstDay.month, firstDay.day)
            cal.get(java.util.Calendar.DAY_OF_WEEK) - 1
        } else 0

        val totalCells = firstDayOfWeek + monthInfo.daysCount
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - firstDayOfWeek + 1

                    if (dayNum in 1..monthInfo.daysCount) {
                        val dayItem = monthInfo.days[dayNum - 1]
                        val isToday = (dayItem.day == todayHijri.day && dayItem.month == todayHijri.month && dayItem.year == todayHijri.year)
                        val isSelected = selectedDate?.day == dayItem.day && selectedDate?.month == dayItem.month && selectedDate?.year == dayItem.year
                        val hasOccasion = dayItem.occasions.isNotEmpty()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> EmeraldGreenPrimary
                                        isToday -> EmeraldGreenPrimary.copy(alpha = 0.15f)
                                        hasOccasion -> GoldAccent.copy(alpha = 0.12f)
                                        dayItem.isWhiteDay -> Color(0xFFF0FDF4)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    }
                                )
                                .border(
                                    width = if (isToday) 2.dp else if (isSelected) 2.dp else if (hasOccasion) 1.5.dp else 0.5.dp,
                                    color = when {
                                        isSelected -> GoldAccent
                                        isToday -> EmeraldGreenPrimary
                                        hasOccasion -> GoldAccent
                                        else -> Color.LightGray.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectDate(dayItem) }
                                .testTag("hijri_day_${dayItem.day}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Hijri Day Number
                                Text(
                                    text = "${dayItem.day}",
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> EmeraldGreenDark
                                        hasOccasion -> Color(0xFFB45309)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                // Gregorian Day Number
                                Text(
                                    text = "${dayItem.gregorianDay}",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray
                                )

                                // Badges dots
                                if (hasOccasion || dayItem.isWhiteDay) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (hasOccasion) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) GoldAccentLight else Color(0xFFD97706))
                                            )
                                        }
                                        if (dayItem.isWhiteDay) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else EmeraldGreenPrimary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty cell
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day Details Card
        selectedDate?.let { date ->
            SelectedDayDetailCard(date = date)
        }
    }
}

@Composable
fun SelectedDayDetailCard(date: HijriDate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hijri_selected_day_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreenPrimary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = date.fullHijriFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreenDark
                    )
                    Text(
                        text = "الموافق: ${date.gregorianFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // White day or Sunnah tag
                if (date.isWhiteDay) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreenPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "الأيام البيض",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenPrimary
                        )
                    }
                } else if (date.isFastingSunnah) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "سنة الصيام",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            // Display occasions if any
            if (date.occasions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = GoldAccent.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                date.occasions.forEach { occasion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldAccent.copy(alpha = 0.1f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = occasion.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = occasion.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "الأعمال المستحبة: ${occasion.recommendedActs}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = EmeraldGreenPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicOccasionsListContent(
    todayHijri: HijriDate,
    onSelectOccasion: (IslamicOccasion) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("islamic_occasions_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "المناسبات والذكريات الإسلامية المباركة على مدار العام الهجري:",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(HijriCalendarHelper.ISLAMIC_OCCASIONS) { occasion ->
            val monthName = HijriCalendarHelper.HIJRI_MONTH_NAMES.getOrElse(occasion.hijriMonth - 1) { "" }
            val isCurrentMonth = todayHijri.month == occasion.hijriMonth
            val isTodayOccasion = isCurrentMonth && todayHijri.day == occasion.hijriDay

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectOccasion(occasion) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTodayOccasion) GoldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isTodayOccasion) 2.dp else 1.dp,
                    color = if (isTodayOccasion) GoldAccent else EmeraldGreenPrimary.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
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
                                    imageVector = if (isTodayOccasion) Icons.Filled.Star else Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isTodayOccasion) GoldAccent else EmeraldGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = occasion.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreenPrimary)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${occasion.hijriDay} $monthName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = occasion.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreenPrimary.copy(alpha = 0.08f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = occasion.recommendedActs,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldGreenDark
                        )
                    }
                }
            }
        }
    }
}
