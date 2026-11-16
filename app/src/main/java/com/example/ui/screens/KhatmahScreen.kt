package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.KhatmahPlan
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.utils.KhatmahStorage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun KhatmahScreen(
    khatmahStorage: KhatmahStorage,
    onNavigateBack: (() -> Unit)? = null,
    onOpenQuranPage: (Int) -> Unit
) {
    val plan by khatmahStorage.khatmahState.collectAsState()

    var showEditTargetDialog by remember { mutableStateOf(false) }
    var showEditPageDialog by remember { mutableStateOf(false) }
    var showNewKhatmahDialog by remember { mutableStateOf(false) }
    var showDuaaSheet by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = (plan.currentPage.toFloat() / plan.totalPages.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "khatmah_progress"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "متابع ختمة القرآن الكريم",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("khatmah_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDuaaSheet = true },
                        modifier = Modifier.testTag("khatmah_duaa_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HistoryEdu,
                            contentDescription = "دعاء ختم القرآن",
                            tint = GoldAccent
                        )
                    }
                    IconButton(
                        onClick = { showNewKhatmahDialog = true },
                        modifier = Modifier.testTag("khatmah_new_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "بدء ختمة جديدة"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // 1. Hero Progress & Estimated Completion Card
            item {
                KhatmahHeroCard(
                    plan = plan,
                    animatedProgress = animatedProgress,
                    onOpenQuran = { onOpenQuranPage(plan.currentPage) }
                )
            }

            // 2. Today's Reading Goal & Quick Log
            item {
                KhatmahDailyGoalCard(
                    plan = plan,
                    onAddPages = { khatmahStorage.addPagesRead(it) },
                    onEditPage = { showEditPageDialog = true },
                    onEditTarget = { showEditTargetDialog = true }
                )
            }

            // 3. Calculator & Target Presets
            item {
                KhatmahTargetPresetsCard(
                    currentDailyPages = plan.dailyPages,
                    onSelectDailyPages = { khatmahStorage.setDailyPages(it) },
                    onCustomTarget = { showEditTargetDialog = true }
                )
            }

            // 4. Quran 30 Juz Visual Progress Map
            item {
                KhatmahJuzGridCard(
                    currentPage = plan.currentPage,
                    onSelectJuz = { startPage -> onOpenQuranPage(startPage) }
                )
            }

            // 5. Celebration & Duaa Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDuaaSheet = true }
                        .testTag("khatmah_duaa_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = GoldAccent.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(GoldAccent, EmeraldGreenPrimary)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFF1E3A2F),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "دعاء ختم القرآن الكريم",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "اقرأ الدعاء المبارك المأثور عند إتمام الختمة",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialog: Adjust Daily Target Pages
    if (showEditTargetDialog) {
        KhatmahDailyPagesDialog(
            currentPages = plan.dailyPages,
            onDismiss = { showEditTargetDialog = false },
            onConfirm = { newTarget ->
                khatmahStorage.setDailyPages(newTarget)
                showEditTargetDialog = false
            }
        )
    }

    // Dialog: Directly Jump/Set Current Page
    if (showEditPageDialog) {
        KhatmahSetPageDialog(
            currentPage = plan.currentPage,
            onDismiss = { showEditPageDialog = false },
            onConfirm = { newPage ->
                khatmahStorage.setCurrentPage(newPage)
                showEditPageDialog = false
            }
        )
    }

    // Dialog: Start New Khatmah
    if (showNewKhatmahDialog) {
        KhatmahNewDialog(
            currentCompletedCount = plan.completedCount,
            onDismiss = { showNewKhatmahDialog = false },
            onConfirm = { title, dailyPages, startPage ->
                khatmahStorage.startNewKhatmah(title, dailyPages, startPage)
                showNewKhatmahDialog = false
            }
        )
    }

    // BottomSheet: Duaa Khatm Al-Quran
    if (showDuaaSheet) {
        DuaaKhatmBottomSheet(onDismiss = { showDuaaSheet = false })
    }
}

/**
 * بطاقة التقدم الرئيسية مع تاريخ الانتهاء المتوقع
 */
@Composable
fun KhatmahHeroCard(
    plan: KhatmahPlan,
    animatedProgress: Float,
    onOpenQuran: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("khatmah_hero_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(EmeraldGreenPrimary, GoldAccent)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Progress Gauge
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                val primaryColor = EmeraldGreenPrimary
                val accentColor = GoldAccent
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = this.center

                    // Track
                    drawCircle(
                        color = trackColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Progress Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, accentColor, primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = plan.formattedPercentage,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreenDark
                    )
                    Text(
                        text = "نسبة الإنجاز",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الصفحة ${plan.currentPage} من ${plan.totalPages}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Current Juz & Hizb details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الجزء الحالي",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "الجزء ${plan.currentJuz}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreenPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الحزب",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "الحزب ${plan.currentHizb}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreenPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "المتبقي",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${plan.remainingPages} صفحة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Estimated Completion Date Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = EmeraldGreenPrimary.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreenPrimary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تاريخ الختم المتوقع بإذن الله:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = plan.getFormattedEstimatedDate(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenDark
                        )
                        if (plan.remainingPages > 0) {
                            Text(
                                text = "متبقي ${plan.daysRemaining} يوماً بمعدل ${plan.dailyPages} صفحة يومياً",
                                fontSize = 11.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Continue Reading in Quran CTA
            Button(
                onClick = onOpenQuran,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("khatmah_open_quran_cta")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "متابعة القراءة من المصحف (صفحة ${plan.currentPage})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * بطاقة ورد اليوم والتسجيل السريع للصفحات
 */
@Composable
fun KhatmahDailyGoalCard(
    plan: KhatmahPlan,
    onAddPages: (Int) -> Unit,
    onEditPage: () -> Unit,
    onEditTarget: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("khatmah_daily_goal_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ورد اليوم المستهدف",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    OutlinedButton(
                        onClick = onEditTarget,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("khatmah_edit_target_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "تعديل الهدف", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = onEditPage,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("khatmah_edit_page_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "تحديد صفحة", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val todayProgress = (plan.pagesReadToday.toFloat() / maxOf(1, plan.dailyPages).toFloat()).coerceIn(0f, 1f)
            val todayPercentage = (todayProgress * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قرأت اليوم: ${plan.pagesReadToday} من ${plan.dailyPages} صفحة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$todayPercentage%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (plan.pagesReadToday >= plan.dailyPages) EmeraldGreenPrimary else GoldAccent
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { todayProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (plan.pagesReadToday >= plan.dailyPages) EmeraldGreenPrimary else GoldAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "إضافة سريعة للصفحات المقروءة:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick add buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    1 to "+1 صفحة",
                    5 to "+5 صفحات",
                    10 to "+10 صفحات",
                    20 to "+20 (جزء)"
                ).forEach { (pages, label) ->
                    FilledTonalButton(
                        onClick = { onAddPages(pages) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("khatmah_add_${pages}_btn"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * خيارات وسرعات الختمة الجاهزة مع حاسبة حية
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KhatmahTargetPresetsCard(
    currentDailyPages: Int,
    onSelectDailyPages: (Int) -> Unit,
    onCustomTarget: () -> Unit
) {
    val presets = listOf(
        Triple(4, "4 صفحات", "صفحة دبر كل صلاة (ختم كل 5 أشهر)"),
        Triple(10, "10 صفحات", "نصف جزء يومياً (ختم كل شهرين)"),
        Triple(20, "20 صفحة", "جزء كامل يومياً (ختمة شهرية مباركة)"),
        Triple(40, "40 صفحة", "جزئين يومياً (ختم كل 15 يوماً)"),
        Triple(60, "60 صفحة", "3 أجزاء يومياً (ختم كل 10 أيام)")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("khatmah_presets_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "حدد وردك اليومي (حاسبة سرعة الختم):",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "اختر المعدل الذي يناسب وقتك لحساب تاريخ الختمة فوراً",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { (pages, title, desc) ->
                    val isSelected = currentDailyPages == pages
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) EmeraldGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) EmeraldGreenPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDailyPages(pages) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) EmeraldGreenDark else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldGreenPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = desc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "ختمة كل ${(604 / pages)} يوم",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onCustomTarget,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "✍️ تحديد عدد صفحات مخصص آخر...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreenPrimary
                )
            }
        }
    }
}

/**
 * خريطة الأجزاء الـ 30 في الختمة
 */
@Composable
fun KhatmahJuzGridCard(
    currentPage: Int,
    onSelectJuz: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("khatmah_juz_grid_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "خريطة الأجزاء (30 جزءاً)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "الأخضر: مكتمل • الذهبي: جاري • اضغط للذهاب للجزء",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of 30 Juz
            val currentJuz = (((currentPage - 1) / 20) + 1).coerceIn(1, 30)

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(160.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items((1..30).toList()) { juz ->
                    val isDone = juz < currentJuz
                    val isCurrent = juz == currentJuz
                    val juzStartPage = ((juz - 1) * 20) + 1

                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isDone -> EmeraldGreenPrimary
                                    isCurrent -> GoldAccent
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable { onSelectJuz(juzStartPage) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "جـ $juz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isDone -> Color.White
                                isCurrent -> Color(0xFF1E3A2F)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * نافذة تحديد عدد الصفحات اليومية
 */
@Composable
fun KhatmahDailyPagesDialog(
    currentPages: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(currentPages.toString()) }
    val parsed = textValue.toIntOrNull() ?: currentPages

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "تعديل الورد اليومي", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "حدد عدد الصفحات التي ترغب بقراءتها كل يوم، وسيقوم التطبيق بحساب تاريخ الانتهاء تلقائياً:",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { if (it.length <= 3) textValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("عدد الصفحات يومياً") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (parsed > 0) {
                    val days = kotlin.math.ceil(604.0 / parsed).toInt()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "⚡ ستختم القرآن كاملاً في حوالي $days يوماً بإذن الله.",
                        fontSize = 12.sp,
                        color = EmeraldGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPages = textValue.toIntOrNull()?.coerceIn(1, 604) ?: currentPages
                    onConfirm(finalPages)
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

/**
 * نافذة تحديد رقم الصفحة الحالية
 */
@Composable
fun KhatmahSetPageDialog(
    currentPage: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(currentPage.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "تحديد الصفحة الحالية", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "أدخل رقم الصفحة التي وصلت إليها في قراءتك (من 1 إلى 604):",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { if (it.length <= 3) textValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("رقم الصفحة (1 - 604)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newPage = textValue.toIntOrNull()?.coerceIn(1, 604) ?: currentPage
                    onConfirm(newPage)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * نافذة بدء ختمة جديدة
 */
@Composable
fun KhatmahNewDialog(
    currentCompletedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit
) {
    var title by remember { mutableStateOf("ختمة مباركة") }
    var dailyPagesText by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "بدء ختمة قرآنية جديدة", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "ما شاء الله، لديك $currentCompletedCount ختمات سابقة مكتملة. هل ترغب ببدء ختمة جديدة تبدأ من الفاتحة؟",
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الختمة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dailyPagesText,
                    onValueChange = { dailyPagesText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("الورد اليومي المستهدف (صفحة/يوم)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pages = dailyPagesText.toIntOrNull()?.coerceIn(1, 604) ?: 20
                    onConfirm(title.ifBlank { "ختمة مباركة" }, pages, 1)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Text("بدء الختمة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * نافذة دعاء ختم القرآن الكريم
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaaKhatmBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "دعاء ختم القرآن الكريم",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreenDark
                )
                TextButton(onClick = onDismiss) {
                    Text("إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                item {
                    Text(
                        text = """
بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ

اللَّهُمَّ ارْحَمْنِي بِالقُرْآنِ وَاجْعَلْهُ لِي إِمَاماً وَنُوراً وَهُدًى وَرَحْمَةً.

اللَّهُمَّ ذَكِّرْنِي مِنْهُ مَا نَسِيتُ وَعَلِّمْنِي مِنْهُ مَا جَهِلْتُ وَارْزُقْنِي تِلاَوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ وَاجْعَلْهُ لِي حُجَّةً يَا رَبَّ العَالَمِينَ.

اللَّهُمَّ أَصْلِحْ لِي دِينِي الَّذِي هُوَ عِصْمَةُ أَمْرِي، وَأَصْلِحْ لِي دُنْيَايَ الَّتِي فِيهَا مَعَاشِي، وَأَصْلِحْ لِي آخِرَتِي الَّتِي فِيهَا مَعَادِي، وَاجْعَلِ الحَيَاةَ زِيَادَةً لِي فِي كُلِّ خَيْرٍ وَاجْعَلِ المَوْتَ رَاحَةً لِي مِنْ كُلِّ شَرٍّ.

اللَّهُمَّ اجْعَلْ خَيْرَ عُمْرِي آخِرَهُ وَخَيْرَ عَمَلِي خَوَاتِمَهُ وَخَيْرَ أَيَّامِي يَوْمَ أَلْقَاكَ فِيهِ.

اللَّهُمَّ إِنِّي أَسْأَلُكَ عِيشَةً هَنِيَّةً وَمِيتَةً سَوِيَّةً وَمَرَدّاً غَيْرَ مُخْزٍ وَلاَ فَاضِحٍ.

اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَ المَسْأَلَةِ وَخَيْرَ الدُّعَاءِ وَخَيْرَ النَّجَاحِ وَخَيْرَ العِلْمِ وَخَيْرَ العَمَلِ وَخَيْرَ الثَّوَابِ وَخَيْرَ الحَيَاةِ وَخَيْرَ المَمَاتِ وَثَبِّتْنِي وَثَقِّلْ مَوَازِينِي وَحَقِّقْ إِيمَانِي وَارْفَعْ دَرَجَتِي وَتَقَبَّلْ صَلاَتِي وَاغْفِرْ خَطِيئَاتِي وَأَسْأَلُكَ العُلاَ مِنَ الجَنَّةِ.

اللَّهُمَّ لَا تَدَعْ لَنَا ذَنْباً إِلَّا غَفَرْتَهُ وَلاَ هَمّاً إِلَّا فَرَّجْتَهُ وَلاَ دَيْناً إِلَّا قَضَيْتَهُ وَلاَ حَاجَةً مِنْ حَوَائِجِ الدُّنْيَا وَالآخِرَةِ إِلَّا قَضَيْتَهَا يَا أَرْحَمَ الرَّاحِمِينَ.

رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ، وَصَلَّى اللَّهُ عَلَى سَيِّدِنَا وَنَبِيِّنَا مُحَمَّدٍ وَعَلَى آلِهِ وَأَصْحَابِهِ الأَخْيَارِ وَسَلَّمَ تَسْلِيماً كَثِيراً.
                        """.trimIndent(),
                        fontSize = 16.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                }
            }
        }
    }
}
