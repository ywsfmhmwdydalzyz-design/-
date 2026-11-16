package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.SoundEffectConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ZikrCategory
import com.example.data.models.ZikrItem
import com.example.data.sources.AzkarDataSource
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun AzkarScreen(
    initialTab: Int = 0,
    initialCategory: ZikrCategory? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    var activeMainTab by remember { mutableIntStateOf(initialTab) }
    var selectedCategory by remember(initialCategory) {
        mutableStateOf(initialCategory ?: ZikrCategory.MORNING)
    }

    // سجل عدد التكرارات لكل ذكر (تفاعلي ومحلي 100%)
    val clickCounts = remember { mutableStateMapOf<String, Int>() }

    val currentList = remember(selectedCategory) {
        AzkarDataSource.getAzkar(selectedCategory)
    }

    // حساب عدد الأذكار المنجزة في التصنيف الحالي
    val completedCount = currentList.count { item ->
        val current = clickCounts[item.id] ?: 0
        current >= item.targetCount
    }

    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // تفعيل الاهتزاز والصوت الخفيف عند الضغط أو إتمام العداد
    fun triggerFeedback(isCompletedNow: Boolean) {
        try {
            if (isCompletedNow) {
                // اهتزاز مزدوج احتفالي لطيف عند إتمام كامل العداد
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 50, 60, 90)
                    val amplitudes = intArrayOf(0, 180, 0, 240)
                    vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(100)
                }
                view.playSoundEffect(SoundEffectConstants.CLICK)
            } else {
                // اهتزاز خفيف جداً وصوت نقرة هادئة مع كل ضغطة عداد
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(20)
                }
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }
        } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("azkar_screen_content")
    ) {
        // التبويب الرئيسي: الأذكار اليومية vs السبحة الإلكترونية
        TabRow(
            selectedTabIndex = activeMainTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldGreenPrimary,
            indicator = { tabPositions ->
                if (activeMainTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeMainTab]),
                        color = GoldAccent,
                        height = 3.dp
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("azkar_main_tabs")
        ) {
            Tab(
                selected = activeMainTab == 0,
                onClick = { activeMainTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "الأذكار الشاملة",
                            fontWeight = if (activeMainTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                },
                selectedContentColor = EmeraldGreenPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Tab(
                selected = activeMainTab == 1,
                onClick = { activeMainTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "السبحة الإلكترونية",
                            fontWeight = if (activeMainTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                },
                selectedContentColor = EmeraldGreenPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (activeMainTab == 1) {
            TasbihScreen(modifier = Modifier.fillMaxSize())
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // شريط تصنيفات الأذكار الأفقي الكامل (6 تصنيفات أساسية + التسبيح)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ZikrCategory.values().forEach { category ->
                                val isSelected = selectedCategory == category
                                val count = AzkarDataSource.getAzkar(category).size
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = {
                                        Text(
                                            text = "${category.titleArabic} ($count)",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldGreenPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // شريط الإنجاز التفاعلي للقسم
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
                                        text = "أنجزت $completedCount من ${currentList.size} أذكار",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val pct = if (currentList.isNotEmpty()) (completedCount * 100 / currentList.size) else 0
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        if (currentList.isNotEmpty()) completedCount.toFloat() / currentList.size.toFloat() else 0f
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = EmeraldGreenPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // زر إعادة ضبط القسم الحالي بالكامل
                            IconButton(
                                onClick = {
                                    currentList.forEach { item -> clickCounts[item.id] = 0 }
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .testTag("reset_azkar_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "إعادة ضبط الأذكار",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // قائمة بطاقات الأذكار بتصميمها العربي المريح وتفاعلها السلس
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentList, key = { it.id }) { item ->
                        val currentClicks = clickCounts[item.id] ?: 0
                        val remaining = maxOf(0, item.targetCount - currentClicks)
                        val isCompleted = currentClicks >= item.targetCount

                        AzkarItemCard(
                            item = item,
                            currentClicks = currentClicks,
                            remaining = remaining,
                            isCompleted = isCompleted,
                            onClickCount = {
                                if (!isCompleted) {
                                    val nextClicks = currentClicks + 1
                                    clickCounts[item.id] = nextClicks
                                    val reachedEnd = nextClicks >= item.targetCount
                                    triggerFeedback(reachedEnd)
                                }
                            },
                            onResetItem = {
                                clickCounts[item.id] = 0
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

/**
 * بطاقة الذكر المصممة بخط عربي مريح وعداد تفاعلي سلس
 */
@Composable
fun AzkarItemCard(
    item: ZikrItem,
    currentClicks: Int,
    remaining: Int,
    isCompleted: Boolean,
    onClickCount: () -> Unit,
    onResetItem: () -> Unit
) {
    val animatedBgColor by animateColorAsState(
        targetValue = if (isCompleted) EmeraldGreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        label = "azkar_card_bg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isCompleted, onClick = onClickCount)
            .testTag("azkar_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = animatedBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                listOf(
                    if (isCompleted) EmeraldGreenPrimary.copy(alpha = 0.8f) else GoldAccent.copy(alpha = 0.35f),
                    if (isCompleted) EmeraldGreenDark.copy(alpha = 0.5f) else Color.Transparent
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // شريط علوي صغير للبطاقة: المصدر وزر إعادة الذكر إذا كان مكتملاً
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.source,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (isCompleted) {
                    IconButton(
                        onClick = onResetItem,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay,
                            contentDescription = "إعادة هذا الذكر",
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // متن الذكر بخط أميري عثماني واضح ومريح للعين
            Text(
                text = item.text,
                fontFamily = AmiriFontFamily,
                fontSize = 18.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            // الفضل أو الثواب إن وُجد
            if (item.rewardOrFadl.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✨ الفضل: ${item.rewardOrFadl}",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // زر العداد التفاعلي السلس
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // مؤشر التقدم البسيط للذكر
                if (item.targetCount > 1) {
                    val itemProgress = (currentClicks.toFloat() / item.targetCount.toFloat()).coerceIn(0f, 1f)
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        LinearProgressIndicator(
                            progress = { itemProgress },
                            color = if (isCompleted) EmeraldGreenPrimary else GoldAccent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = "${currentClicks} / ${item.targetCount}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // زر العداد الذي ينقص تدريجياً
                Surface(
                    onClick = onClickCount,
                    enabled = !isCompleted,
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCompleted) EmeraldGreenPrimary else EmeraldGreenPrimary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCompleted) EmeraldGreenPrimary else EmeraldGreenPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("counter_button_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "تم الإنجاز",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تم بحمد الله ✓",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.TouchApp,
                                contentDescription = "اضغط للعد",
                                tint = EmeraldGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "متبقي $remaining",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
