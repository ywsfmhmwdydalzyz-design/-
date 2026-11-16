package com.example.ui.screens

import android.content.Context
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorderGold
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.SoftGreenCard
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextDarkSecondary
import com.example.ui.theme.WhiteSurface
import com.example.utils.LocationPrayerManager
import com.example.utils.QiblaCalculator
import com.example.utils.QiblaSensorManager
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaCompassScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val sensorManager = remember { QiblaSensorManager(context) }

    DisposableEffect(Unit) {
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    val locationData by LocationPrayerManager.locationData.collectAsState()
    val rawHeading by sensorManager.heading.collectAsState()
    val pitch by sensorManager.pitch.collectAsState()
    val sensorAccuracy by sensorManager.sensorAccuracy.collectAsState()
    val isSensorAvailable by sensorManager.isSensorAvailable.collectAsState()
    val isSimulated by sensorManager.isSimulatedMode.collectAsState()

    var showCityDropdown by remember { mutableStateOf(false) }
    var showCalibrationTip by remember { mutableStateOf(false) }
    var showSimulationPanel by remember { mutableStateOf(false) }

    // Astronomical calculation of Qibla bearing and distance to Makkah
    val qiblaBearing = remember(locationData) {
        QiblaCalculator.calculateQiblaBearing(locationData.latitude, locationData.longitude)
    }
    val distanceKm = remember(locationData) {
        QiblaCalculator.calculateDistanceToMakkahKm(locationData.latitude, locationData.longitude)
    }
    val directionArabic = remember(qiblaBearing) {
        QiblaCalculator.getDirectionArabic(qiblaBearing)
    }

    // Relative difference between current heading and Qibla bearing
    // When relativeDiff is near 0° (or 360°), phone is pointing at Makkah
    val relativeDiff = ((qiblaBearing - rawHeading + 360f) % 360f)
    val isAligned = abs(relativeDiff) <= 4f || abs(relativeDiff - 360f) <= 4f

    // Trigger haptic feedback when aligned
    LaunchedEffect(isAligned) {
        if (isAligned) {
            sensorManager.triggerQiblaAlignedHaptic()
        }
    }

    // Smooth needle animation
    val animatedHeading by animateFloatAsState(
        targetValue = rawHeading,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "compass_heading_anim"
    )

    // Glowing animation when aligned
    val infiniteTransition = rememberInfiniteTransition(label = "aligned_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("qibla_compass_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qibla_location_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(EmeraldGreenPrimary.copy(alpha = 0.4f), GoldAccent.copy(alpha = 0.4f)))
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
                            .background(EmeraldGreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "موقع حساب اتجاه القبلة",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDarkSecondary
                        )
                        Text(
                            text = "${locationData.cityName} • ${locationData.countryName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenDark
                        )
                    }
                }

                Box {
                    OutlinedButton(
                        onClick = { showCityDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_change_qibla_city")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("تغيير المدينة", fontSize = 11.sp, color = EmeraldGreenPrimary)
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = EmeraldGreenPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = showCityDropdown,
                        onDismissRequest = { showCityDropdown = false }
                    ) {
                        LocationPrayerManager.PRESET_CITIES.forEach { city ->
                            DropdownMenuItem(
                                text = { Text("${city.cityName} (${city.countryName})", fontSize = 13.sp) },
                                onClick = {
                                    LocationPrayerManager.setManualCity(city, context)
                                    showCityDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Alignment Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isAligned) {
                        Brush.horizontalGradient(
                            listOf(
                                EmeraldGreenDark,
                                EmeraldGreenPrimary,
                                EmeraldGreenDark
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                )
                .border(
                    width = if (isAligned) 2.dp else 1.dp,
                    color = if (isAligned) GoldAccent.copy(alpha = glowAlpha) else CardBorderGold,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 14.dp, horizontal = 16.dp)
                .testTag("qibla_alignment_banner"),
            contentAlignment = Alignment.Center
        ) {
            if (isAligned) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = GoldAccentLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "أنت الآن باتجاه القبلة الشريفة مباشرة 🕋",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            } else {
                val diffDegrees = if (relativeDiff > 180f) (360f - relativeDiff).toInt() else relativeDiff.toInt()
                val turnDir = if (relativeDiff > 180f) "يساراً" else "يميناً"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Explore,
                        contentDescription = null,
                        tint = EmeraldGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "أدر هاتفك $turnDir بمقدار $diffDegrees° لمواجهة الكعبة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = EmeraldGreenDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Compass Dial Canvas
        Box(
            modifier = Modifier
                .size(310.dp)
                .clip(CircleShape)
                .background(
                    if (isAligned) {
                        Brush.radialGradient(
                            listOf(
                                GoldAccent.copy(alpha = 0.25f * glowAlpha),
                                EmeraldGreenPrimary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    }
                )
                .border(
                    width = if (isAligned) 3.dp else 1.5.dp,
                    brush = if (isAligned) {
                        Brush.sweepGradient(
                            listOf(GoldAccent, EmeraldGreenLight, GoldAccent)
                        )
                    } else {
                        Brush.sweepGradient(
                            listOf(EmeraldGreenPrimary.copy(alpha = 0.6f), GoldAccent.copy(alpha = 0.5f), EmeraldGreenPrimary.copy(alpha = 0.6f))
                        )
                    },
                    shape = CircleShape
                )
                .testTag("interactive_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Main Compass Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension / 2f) - 6.dp.toPx()

                // Draw outer decorative ring
                drawCircle(
                    color = if (isAligned) GoldAccent.copy(alpha = 0.3f) else EmeraldGreenPrimary.copy(alpha = 0.15f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Draw secondary inner ring
                drawCircle(
                    color = GoldAccent.copy(alpha = 0.2f),
                    radius = radius * 0.85f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Draw 360 degree ticks (rotating with phone heading so dial reflects actual earth frame)
                val rotationAngle = -animatedHeading
                for (degree in 0 until 360 step 5) {
                    val angleRad = Math.toRadians((degree + rotationAngle - 90).toDouble())
                    val isMajor = degree % 30 == 0
                    val isCardinal = degree % 90 == 0
                    val tickLen = when {
                        isCardinal -> 14.dp.toPx()
                        isMajor -> 9.dp.toPx()
                        else -> 5.dp.toPx()
                    }
                    val tickColor = when {
                        isCardinal -> EmeraldGreenPrimary
                        isMajor -> GoldAccent
                        else -> Color.Gray.copy(alpha = 0.4f)
                    }
                    val strokeW = if (isCardinal) 2.5f.dp.toPx() else if (isMajor) 1.5f.dp.toPx() else 1f.dp.toPx()

                    val startX = center.x + (radius - tickLen) * cos(angleRad).toFloat()
                    val startY = center.y + (radius - tickLen) * sin(angleRad).toFloat()
                    val endX = center.x + radius * cos(angleRad).toFloat()
                    val endY = center.y + radius * sin(angleRad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }

                // Draw cardinal letters on the rotating rim (N, E, S, W in Arabic)
                val cardinalPoints = listOf(
                    Pair(0, "ش"),   // North
                    Pair(90, "ق"),  // East
                    Pair(180, "ج"), // South
                    Pair(270, "غ")  // West
                )
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#0D6E54")
                    textSize = 14.sp.toPx()
                    isFakeBoldText = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                for ((deg, label) in cardinalPoints) {
                    val rad = Math.toRadians((deg + rotationAngle - 90).toDouble())
                    val textDist = radius * 0.72f
                    val x = center.x + textDist * cos(rad).toFloat()
                    val y = center.y + textDist * sin(rad).toFloat() + 5.dp.toPx()
                    drawContext.canvas.nativeCanvas.drawText(label, x, y, paint)
                }

                // Draw Holy Kaaba indicator badge on the rotating rim at exact Qibla bearing
                val qiblaIndicatorAngle = qiblaBearing + rotationAngle - 90
                val qiblaRad = Math.toRadians(qiblaIndicatorAngle.toDouble())
                val kaabaMarkerRadius = radius * 0.88f
                val kaabaX = center.x + kaabaMarkerRadius * cos(qiblaRad).toFloat()
                val kaabaY = center.y + kaabaMarkerRadius * sin(qiblaRad).toFloat()

                // Golden indicator beam towards center
                drawLine(
                    color = GoldAccent,
                    start = Offset(kaabaX, kaabaY),
                    end = Offset(
                        center.x + (radius * 0.55f) * cos(qiblaRad).toFloat(),
                        center.y + (radius * 0.55f) * sin(qiblaRad).toFloat()
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Golden circular badge around Kaaba
                drawCircle(
                    color = GoldAccent,
                    radius = 14.dp.toPx(),
                    center = Offset(kaabaX, kaabaY),
                    style = Fill
                )
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = Offset(kaabaX, kaabaY),
                    style = Fill
                )

                // Draw Kaaba icon text
                val kaabaPaint = android.graphics.Paint().apply {
                    textSize = 15.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText("🕋", kaabaX, kaabaY + 5.dp.toPx(), kaabaPaint)

                // Draw the dynamic Qibla pointer needle (pointing straight to Makkah from device perspective)
                val needleAngleRad = Math.toRadians((qiblaIndicatorAngle).toDouble())
                val needleLength = radius * 0.62f
                val needleWidth = 14.dp.toPx()

                // North/Kaaba pointer Path (Emerald Green & Gold)
                val tipX = center.x + needleLength * cos(needleAngleRad).toFloat()
                val tipY = center.y + needleLength * sin(needleAngleRad).toFloat()

                val perpAngleRad = needleAngleRad + Math.PI / 2.0
                val baseLeftX = center.x + (needleWidth / 2f) * cos(perpAngleRad).toFloat()
                val baseLeftY = center.y + (needleWidth / 2f) * sin(perpAngleRad).toFloat()
                val baseRightX = center.x - (needleWidth / 2f) * cos(perpAngleRad).toFloat()
                val baseRightY = center.y - (needleWidth / 2f) * sin(perpAngleRad).toFloat()

                val needlePath = Path().apply {
                    moveTo(tipX, tipY)
                    lineTo(baseLeftX, baseLeftY)
                    lineTo(center.x, center.y)
                    close()
                }
                drawPath(
                    path = needlePath,
                    color = if (isAligned) EmeraldGreenLight else EmeraldGreenPrimary
                )

                val needlePathRight = Path().apply {
                    moveTo(tipX, tipY)
                    lineTo(baseRightX, baseRightY)
                    lineTo(center.x, center.y)
                    close()
                }
                drawPath(
                    path = needlePathRight,
                    color = if (isAligned) GoldAccent else EmeraldGreenDark
                )

                // Counter pointer Path (Silver/Slate)
                val oppAngleRad = needleAngleRad + Math.PI
                val oppTipX = center.x + (needleLength * 0.45f) * cos(oppAngleRad).toFloat()
                val oppTipY = center.y + (needleLength * 0.45f) * sin(oppAngleRad).toFloat()

                val counterPath = Path().apply {
                    moveTo(oppTipX, oppTipY)
                    lineTo(baseLeftX, baseLeftY)
                    lineTo(center.x, center.y)
                    close()
                }
                drawPath(
                    path = counterPath,
                    color = Color.LightGray
                )
                val counterPathRight = Path().apply {
                    moveTo(oppTipX, oppTipY)
                    lineTo(baseRightX, baseRightY)
                    lineTo(center.x, center.y)
                    close()
                }
                drawPath(
                    path = counterPathRight,
                    color = Color.Gray
                )

                // Center Gold Medallion
                drawCircle(
                    color = GoldAccentDark,
                    radius = 9.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = GoldAccentLight,
                    radius = 6.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = EmeraldGreenDark,
                    radius = 3.dp.toPx(),
                    center = center
                )
            }

            // Top alignment indicator notch
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isAligned) EmeraldGreenLight else GoldAccent)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Numerical Metrics Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "زاوية القبلة",
                value = "${qiblaBearing.toInt()}°",
                subtitle = directionArabic,
                modifier = Modifier.weight(1f),
                testTag = "metric_qibla_bearing"
            )
            MetricCard(
                title = "اتجاه الهاتف",
                value = "${rawHeading.toInt()}°",
                subtitle = if (isAligned) "مطابق للقبلة ✓" else "مائل",
                isHighlighted = isAligned,
                modifier = Modifier.weight(1f),
                testTag = "metric_device_heading"
            )
            val formattedDistance = remember(distanceKm) {
                NumberFormat.getNumberInstance(Locale.getDefault()).format(distanceKm.toInt())
            }
            MetricCard(
                title = "المسافة للكعبة",
                value = "$formattedDistance",
                subtitle = "كيلومتر",
                modifier = Modifier.weight(1f),
                testTag = "metric_makkah_distance"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phone Flat Orientation Advice (pitch detection)
        if (abs(pitch) > 35f) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GoldContainer),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, GoldAccentDark)))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = GoldAccentDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💡 لدقة أفضل: ضع الهاتف مستوياً تماماً على راحة يدك بعيداً عن الأجهزة الإلكترونية أو المعادن.",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F),
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Sensor Status & Calibration guidance
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sensor_status_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldGreenPrimary.copy(alpha = 0.2f), CardBorderGold)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Sensors,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مستشعرات التوجيه (مقياس التسارع والمغناطيسية)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val accuracyLabel = when (sensorAccuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "دقة ممتازة ✓"
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "دقة جيدة"
                        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "دقة منخفضة"
                        else -> "غير معاير"
                    }
                    val accuracyColor = when (sensorAccuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> EmeraldGreenPrimary
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> GoldAccent
                        else -> Color(0xFFDC2626)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accuracyColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSimulated) "وضع المحاكاة" else accuracyLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accuracyColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSensorAvailable) "مستشعرا Accelerometer و Magnetometer نشطان بالوقت الفعلي." else "المستشعرات غير متوفرة بالجهاز، تم تفعيل وضع المحاكاة.",
                        fontSize = 11.sp,
                        color = TextDarkSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { showCalibrationTip = !showCalibrationTip },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_calibration_guide")
                        ) {
                            Icon(Icons.Filled.CompassCalibration, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldGreenPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("كيفية المعايرة", fontSize = 10.sp, color = EmeraldGreenPrimary)
                        }

                        Button(
                            onClick = { showSimulationPanel = !showSimulationPanel },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (showSimulationPanel) GoldAccent else EmeraldGreenPrimary.copy(alpha = 0.15f)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_toggle_simulation_panel")
                        ) {
                            Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (showSimulationPanel) Color.Black else EmeraldGreenPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showSimulationPanel) "إخفاء التحكم" else "تحكم يدوي", fontSize = 10.sp, color = if (showSimulationPanel) Color.Black else EmeraldGreenPrimary)
                        }
                    }
                }

                AnimatedVisibility(visible = showCalibrationTip) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftGreenCard)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "طريقة معايرة بوصلة الهاتف:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = EmeraldGreenDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "امسك هاتفك وحركه في الهواء برسم الرقم ثمانية بالإنجليزي (∞ / 8) مرتين إلى ثلاث مرات، لإعادة توجيه المستشعر المغناطيسي وإزالة التشويش الكهرومغناطيسي.",
                            fontSize = 10.sp,
                            color = TextDarkSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Interactive Simulation Control Slider (Ideal for emulator testing or fine manual verification)
                AnimatedVisibility(visible = showSimulationPanel) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, CardBorderGold, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "تدوير الهاتف يدوياً (للتجربة والتحقق)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreenDark
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("تفعيل يدوي", fontSize = 10.sp, color = TextDarkSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Switch(
                                    checked = isSimulated,
                                    onCheckedChange = { sensorManager.toggleSimulatedMode(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = EmeraldGreenPrimary)
                                )
                            }
                        }

                        if (isSimulated) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Slider(
                                value = rawHeading,
                                onValueChange = { sensorManager.setSimulatedHeading(it) },
                                valueRange = 0f..360f,
                                colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = EmeraldGreenPrimary),
                                modifier = Modifier.testTag("simulation_heading_slider")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("0° (شمال)", fontSize = 9.sp, color = Color.Gray)
                                Button(
                                    onClick = { sensorManager.setSimulatedHeading(qiblaBearing) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("محاذاة مباشرة للقبلة 🎯", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                Text("360°", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spiritual Dua Card when facing Qibla
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qibla_dua_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SoftGreenCard.copy(alpha = 0.6f)),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent.copy(alpha = 0.3f), Color.Transparent)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "دعاء التوجه والاستفتاح",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreenDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "«وَجَّهْتُ وَجْهِيَ لِلَّذِي فَطَرَ السَّمَاوَاتِ وَالْأَرْضَ حَنِيفًا وَمَا أَنَا مِنَ الْمُشْرِكِينَ»",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreenDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    testTag: String = ""
) {
    Card(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) EmeraldGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isHighlighted) {
            CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, EmeraldGreenPrimary)))
        } else {
            CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CardBorderGold, Color.Transparent)))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = TextDarkSecondary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isHighlighted) EmeraldGreenPrimary else EmeraldGreenDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = if (isHighlighted) GoldAccentDark else TextDarkSecondary
            )
        }
    }
}
