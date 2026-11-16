package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.services.AzanService
import com.example.services.AzanStateHolder
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.delay

/**
 * شاشة تنبيه الأذان الفاخرة (Cinematic Luxury Adhan Alert Screen)
 * - خلفية سينمائية عميقة بتدرجات الزمرد الداكن وزخارف الأرابيسك الهندسية
 * - بطاقة زجاجية شفافة فخمة (Frosted Glass Card) بإطار ذهبي هادئ مشع
 * - خط عثماني ونَسخي جليل بوهج ذهبي أبيض ملكي (Glow Effect)
 * - أيقونة هلال ومئذنة مذهبة ثلاثية الأبعاد بلمعة ناعمة نابضة
 * - أزرار تفاعلية مستديرة وعصرية بأبعاد وظلال لمسية ناعمة
 */
@Composable
fun AzanPlayerScreen(
    prayerName: String = "صلاة الفجر",
    onDismiss: () -> Unit = {},
    onOpenApp: () -> Unit = {}
) {
    val context = LocalContext.current

    val isPlaying by AzanStateHolder.isPlaying.collectAsState()
    val isCompleted by AzanStateHolder.isCompleted.collectAsState()
    val progress by AzanStateHolder.playbackProgress.collectAsState()
    val remainingSeconds by AzanStateHolder.remainingSeconds.collectAsState()

    var hasEverPlayed by remember { mutableStateOf(false) }
    var showDuaAfterAzan by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            hasEverPlayed = true
        } else if (hasEverPlayed) {
            delay(500)
            onDismiss()
        }
    }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            delay(500)
            onDismiss()
        }
    }

    // تأثير النبض الهادئ للأيقونة والإضاءة الذهبية
    val infiniteTransition = rememberInfiniteTransition(label = "azan_luxury_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblem_scale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_glow"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTimeLeft = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020E08))
            .testTag("azan_full_screen_view"),
        contentAlignment = Alignment.Center
    ) {
        // 1. صورة الخلفية السينمائية الفاخرة للجامع والمحراب
        Image(
            painter = painterResource(id = R.drawable.img_azan_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.38f
        )

        // 2. طبقة تدرج لوني سينمائي عميق ومريح للعين (Deep Emerald Gradient)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF202110A),
                            Color(0xCC041F14),
                            Color(0xEE02130C),
                            Color(0xFA010804)
                        )
                    )
                )
        )

        // 3. زخارف أرابيسك إسلامية دقيقة وشفافة في الخلفية (Ambient Islamic Arabesque Lattice)
        IslamicGeometricBackdrop(modifier = Modifier.fillMaxSize())

        // 4. هالة الإضاءة الذهبية الدافئة خلف محتوى التنبيه
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(pulseScale)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x40FFD54F).copy(alpha = haloAlpha * 0.45f),
                            Color(0x20C5A059).copy(alpha = haloAlpha * 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 5. المحتوى الرئيسي داخل بطاقة زجاجية فخمة قابلة للتمرير لشاشات الهواتف المختلفة
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            // ==========================================
            // بطاقة الزجاج الشفافة الفاخرة (Frosted Glass Card)
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color(0xFFD4AF37),
                        ambientColor = Color(0xFF041F14)
                    ),
                shape = RoundedCornerShape(32.dp),
                color = Color(0x38051E16),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFE599).copy(alpha = 0.85f),
                            Color(0xFFC5A059).copy(alpha = 0.35f),
                            Color(0xFFFFDF88).copy(alpha = 0.70f)
                        )
                    )
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x4D0D3B2C),
                                    Color(0x59041A12),
                                    Color(0x4D07261B)
                                )
                            )
                        )
                        .padding(horizontal = 22.dp, vertical = 28.dp)
                ) {
                    // أيقونة الهلال والمئذنة المذهبة ثلاثية الأبعاد مع هالة براقة
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .scale(pulseScale),
                        contentAlignment = Alignment.Center
                    ) {
                        // هالة متوهجة خلف الأيقونة
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0x66FFDF88),
                                            Color(0x22C5A059),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // حلقة ذهبية مضيئة للأيقونة
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color(0xFFFFE8A3),
                                            Color(0xFFB38728),
                                            Color(0xFFFBF5B7),
                                            Color(0xFFDAA520),
                                            Color(0xFFFFE8A3)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .background(Color(0xFF02130C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_golden_crescent),
                                contentDescription = "هلال الأذان المذهب",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // عبارة "الله أكبر .. الله أكبر" بتأثير وهج ذهبي أبيض جليل (Glow Effect)
                    Box(contentAlignment = Alignment.Center) {
                        // طبقة الوهج الذهبي المشع في الخلف
                        Text(
                            text = "اللهُ أَكْبَر .. اللهُ أَكْبَر",
                            style = TextStyle(
                                fontFamily = AmiriFontFamily,
                                fontSize = 33.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFDF88).copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                shadow = Shadow(
                                    color = Color(0xFFFFDF88),
                                    blurRadius = 32f,
                                    offset = Offset.Zero
                                )
                            )
                        )

                        // النص الرئيسي الصافي بلون ذهبي أبيض فخم
                        Text(
                            text = "اللهُ أَكْبَر .. اللهُ أَكْبَر",
                            style = TextStyle(
                                fontFamily = AmiriFontFamily,
                                fontSize = 33.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFFDF5),
                                textAlign = TextAlign.Center,
                                shadow = Shadow(
                                    color = Color(0xFFFFE8A3).copy(alpha = 0.9f),
                                    blurRadius = 14f,
                                    offset = Offset.Zero
                                )
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // شارة موعد الصلاة الأنيقة والمضيئة
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0x33000000),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0x33FFDF88),
                                    Color(0x88FFDF88),
                                    Color(0x33FFDF88)
                                )
                            )
                        ),
                        modifier = Modifier.shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0xFFC5A059)
                        )
                    ) {
                        Text(
                            text = "حان الآن وقت $prayerName",
                            style = TextStyle(
                                fontFamily = AmiriFontFamily,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFE8A3),
                                textAlign = TextAlign.Center,
                                shadow = Shadow(
                                    color = Color(0x88FFDF88),
                                    blurRadius = 10f,
                                    offset = Offset.Zero
                                )
                            ),
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // صوت الأذان
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = GoldAccent.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "صوت الأذان: الشيخ إسلام صبحي 🎙️",
                            fontFamily = AmiriFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // بطاقة العداد التنازلي وشريط التقدم المضيء
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x2E000000),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "المتبقي: $formattedTimeLeft",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }

                                Text(
                                    text = "إغلاق تلقائي عند انتهاء الأذان",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // شريط التقدم بتدرج ذهبي براق
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = GoldAccent,
                                trackColor = Color.White.copy(alpha = 0.12f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // بطاقة دعاء ما بعد الأذان (قابلة للفتح والإغلاق)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x1FFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 0.8.dp,
                            color = GoldAccent.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = GoldAccent)
                            ) {
                                showDuaAfterAzan = !showDuaAfterAzan
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "دعاء ما بعد الأذان النبوي 🤲",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }
                                Icon(
                                    imageVector = if (showDuaAfterAzan) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showDuaAfterAzan,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = "«اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ، وَالصَّلَاةِ الْقَائِمَةِ، آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ، وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ»",
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 14.sp,
                                        lineHeight = 22.sp,
                                        color = Color(0xFFFFFBEA),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "حلت له شفاعتي يوم القيامة (رواه البخاري)",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ==========================================
            // الأزرار والتفاعل الحديثة (Modern Rounded Buttons)
            // ==========================================

            // 1. زر "إيقاف صوت الأذان" الحديث والمستدير مع تدرج ياقوتي وظل عميق
            Button(
                onClick = {
                    val stopIntent = Intent(context, AzanService::class.java).apply {
                        action = AzanService.ACTION_STOP_AZAN
                    }
                    context.startService(stopIntent)
                    AzanStateHolder.markCompleted()
                    onDismiss()
                },
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Color(0xFFE53935),
                        ambientColor = Color(0xFFB71C1C)
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF8A80).copy(alpha = 0.65f),
                                Color(0xFFFF5252).copy(alpha = 0.35f),
                                Color(0xFFFF8A80).copy(alpha = 0.65f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .testTag("stop_azan_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFD32F2F),
                                    Color(0xFFC62828),
                                    Color(0xFF9E0B0B)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = "إيقاف الأذان",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "إيقاف صوت الأذان",
                            fontFamily = AmiriFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. زر "فتح تطبيق سكينة" الزجاجي المستدير بلمسة ذهبية هادئة
            OutlinedButton(
                onClick = onOpenApp,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    containerColor = Color(0x280D4835)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            GoldAccent.copy(alpha = 0.7f),
                            EmeraldGreenPrimary.copy(alpha = 0.5f),
                            GoldAccent.copy(alpha = 0.7f)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(52.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0xFFC5A059)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "فتح تطبيق سكينة 📖",
                        fontFamily = AmiriFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFDF0)
                    )
                }
            }
        }
    }
}

/**
 * زخارف هندسية إسلامية رقيقة ترسم بـ Canvas في الخلفية لإعطاء طابع أرابيسك عميق وأصيل
 */
@Composable
private fun IslamicGeometricBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeColor = Color(0xFFD4AF37).copy(alpha = 0.045f)
        val stroke = Stroke(width = 1.2f)

        val w = size.width
        val h = size.height

        // رسم أقواس محراب إسلامية متداخلة في أعلى الشاشة
        val archPath = Path().apply {
            moveTo(0f, h * 0.22f)
            cubicTo(
                w * 0.25f, h * 0.12f,
                w * 0.35f, h * 0.02f,
                w * 0.5f, 0f
            )
            cubicTo(
                w * 0.65f, h * 0.02f,
                w * 0.75f, h * 0.12f,
                w, h * 0.22f
            )
        }
        drawPath(archPath, color = strokeColor, style = stroke)

        val innerArch = Path().apply {
            moveTo(w * 0.08f, h * 0.24f)
            cubicTo(
                w * 0.30f, h * 0.15f,
                w * 0.38f, h * 0.06f,
                w * 0.5f, h * 0.04f
            )
            cubicTo(
                w * 0.62f, h * 0.06f,
                w * 0.70f, h * 0.15f,
                w * 0.92f, h * 0.24f
            )
        }
        drawPath(innerArch, color = strokeColor, style = stroke)

        // دوائر هندسية محيطية خفيفة
        drawCircle(
            color = strokeColor,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.28f),
            style = stroke
        )
        drawCircle(
            color = strokeColor,
            radius = w * 0.58f,
            center = Offset(w * 0.5f, h * 0.28f),
            style = stroke
        )
    }
}

