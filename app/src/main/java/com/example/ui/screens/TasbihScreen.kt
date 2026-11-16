package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch

data class TasbihDhikrPreset(
    val title: String,
    val recommendedTarget: Int,
    val virtue: String = ""
)

val defaultTasbihPresets = listOf(
    TasbihDhikrPreset("سبحان الله", 33, "تغرس لك نخلة في الجنة وتُحط بها الخطايا"),
    TasbihDhikrPreset("الحمد لله", 33, "أفضل الدعاء وتملأ ميزان العبد بالحسنات"),
    TasbihDhikrPreset("الله أكبر", 33, "أعظم الكلمات وأحبها إلى الله سبحانه"),
    TasbihDhikrPreset("لا إله إلا الله", 100, "خير ما قال النبيون وأفضل الذكر"),
    TasbihDhikrPreset("أستغفر الله وأتوب إليه", 100, "مغفرة للذنوب وسعة في الرزق وراحة للقلب"),
    TasbihDhikrPreset("لا حول ولا قوة إلا بالله", 100, "كنز عظيم من كنوز الجنة"),
    TasbihDhikrPreset("اللهم صلِّ على محمد", 100, "من صلى عليّ صلاة صلى الله عليه بها عشراً"),
    TasbihDhikrPreset("سبحان الله وبحمده", 100, "حُطت خطاياه وإن كانت مثل زبد البحر"),
    TasbihDhikrPreset("تسبيح حر (مفتوح)", 0, "ذكر الله المطلق في كل آن وحين")
)

/**
 * شاشة ومكون السبحة الإلكترونية الذكية التفاعلية (TasbihScreen)
 * تتيح التسبيح بالنقر على أي مكان بالشاشة، مع دعم اهتزاز اللمس
 * واهتزاز إيقاعي مميز عند بلوغ الأعداد المستهدفة (مثل 33 أو 99 أو 100).
 */
@Composable
fun TasbihScreen(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var count by remember { mutableIntStateOf(0) }
    var totalSessionCount by remember { mutableIntStateOf(0) }
    var roundsCount by remember { mutableIntStateOf(0) }
    var targetCount by remember { mutableIntStateOf(33) } // 33, 99, 100, 0 (open)
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var isVibrationEnabled by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Animation scale for the central Tasbih button on tap
    val scaleAnim = remember { Animatable(1f) }

    fun triggerTapHaptic() {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    fun triggerMilestoneHaptic() {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Pattern: strong double pulse signaling target reached (e.g. 33, 99, 100)
                val timings = longArrayOf(0, 120, 100, 220)
                val amplitudes = intArrayOf(0, 220, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 120, 100, 220), -1)
            }
        } catch (_: Exception) {}
    }

    fun incrementTasbih() {
        val nextCount = count + 1
        count = nextCount
        totalSessionCount += 1

        // Animate pulse
        scope.launch {
            scaleAnim.animateTo(0.93f, spring(stiffness = Spring.StiffnessHigh))
            scaleAnim.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
        }

        // Check if a milestone is reached (33, 66, 99, 100 or user target)
        val isTargetReached = targetCount > 0 && nextCount % targetCount == 0
        val isGenericMilestone = nextCount == 33 || nextCount == 66 || nextCount == 99 || nextCount == 100

        if (isTargetReached || isGenericMilestone) {
            triggerMilestoneHaptic()
            if (targetCount > 0 && nextCount >= targetCount) {
                roundsCount += 1
                count = 0 // Reset for next round
            }
        } else {
            triggerTapHaptic()
        }
    }

    fun undoTasbih() {
        if (count > 0) {
            count -= 1
            if (totalSessionCount > 0) totalSessionCount -= 1
            triggerTapHaptic()
        }
    }

    val currentPreset = defaultTasbihPresets.getOrElse(selectedPresetIndex) { defaultTasbihPresets[0] }

    val progress = if (targetCount > 0) {
        (count.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "tasbih_progress")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("tasbih_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Toolbar: Presets & Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)) {
                // Presets horizontal list
                Text(
                    text = "اختر الذكر:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultTasbihPresets.indices.toList()) { index ->
                        val preset = defaultTasbihPresets[index]
                        val isSelected = selectedPresetIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPresetIndex = index
                                if (preset.recommendedTarget > 0) {
                                    targetCount = preset.recommendedTarget
                                }
                            },
                            label = {
                                Text(
                                    text = preset.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("tasbih_preset_chip_$index")
                        )
                    }
                }

                // Target Selectors & Quick Settings Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Target choices
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الهدف:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        listOf(33, 99, 100, 0).forEach { tgt ->
                            val isSelected = targetCount == tgt
                            val label = if (tgt == 0) "حر" else tgt.toString()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) GoldAccent else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        targetCount = tgt
                                        count = 0
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("target_button_$tgt")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF1E3A2F) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Vibration toggle & Undo buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { undoTasbih() },
                            enabled = count > 0,
                            modifier = Modifier.size(36.dp).testTag("tasbih_undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "تراجع خطوة",
                                tint = if (count > 0) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { isVibrationEnabled = !isVibrationEnabled },
                            modifier = Modifier.size(36.dp).testTag("tasbih_vibrate_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Vibration,
                                contentDescription = "تشغيل/إيقاف الاهتزاز",
                                tint = if (isVibrationEnabled) EmeraldGreenPrimary else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.size(36.dp).testTag("tasbih_reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "تصفير العداد",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Selected Dhikr Presentation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = EmeraldGreenPrimary.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(GoldAccent.copy(alpha = 0.6f), EmeraldGreenPrimary.copy(alpha = 0.3f))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentPreset.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreenDark,
                    textAlign = TextAlign.Center
                )

                if (currentPreset.virtue.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✨ ${currentPreset.virtue}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Stats summary (Rounds and Total)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الدورات المكتملة: $roundsCount",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "إجمالي التسبيحات: $totalSessionCount",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldAccent
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Tap Area: Interactive Big Tasbih Button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    incrementTasbih()
                },
            contentAlignment = Alignment.Center
        ) {
            // Circular progress gauge & Dial
            val primaryColor = EmeraldGreenPrimary
            val accentColor = GoldAccent
            val trackColor = MaterialTheme.colorScheme.surfaceVariant

            Box(
                modifier = Modifier
                    .size(270.dp)
                    .scale(scaleAnim.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = this.center

                    // Background Track
                    drawCircle(
                        color = trackColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active Progress Arc
                    if (targetCount > 0) {
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
                }

                // Inner Tap Circle
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    EmeraldGreenPrimary,
                                    EmeraldGreenDark,
                                    Color(0xFF093322)
                                )
                            )
                        )
                        .border(3.dp, GoldAccent.copy(alpha = 0.8f), CircleShape)
                        .testTag("tasbih_tap_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 62.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (targetCount > 0) "الهدف: $targetCount" else "تسبيح حر",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldAccent
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TouchApp,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "المس للزيادة",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Tap Anywhere Helper Label
        Text(
            text = "اضغط في أي مكان على الشاشة لزيادة التسبيح • يدعم الاهتزاز عند 33 و 99",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        )
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(text = "تصفير العداد", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "هل ترغب في إعادة ضبط العداد الحالي والدورات إلى الصفر؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        count = 0
                        roundsCount = 0
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("نعم، تصفير")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
