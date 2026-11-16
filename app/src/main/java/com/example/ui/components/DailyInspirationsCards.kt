package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.models.DailyAyah
import com.example.data.models.DailyHadith
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight

/**
 * بطاقة "آية اليوم" بتصميم إسلامي وزخارف مريحة للعين تتغير تلقائياً كل يوم
 */
@Composable
fun AyahOfTheDayWidget(
    ayah: DailyAyah,
    onOpenQuranPage: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    var showReflection by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_of_the_day_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFBF8F0) // لون دافئ مريح للعين يشبه ورق المصحف
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    GoldAccent.copy(alpha = 0.7f),
                    EmeraldGreenPrimary.copy(alpha = 0.4f),
                    GoldAccent.copy(alpha = 0.7f)
                )
            ),
            width = 1.2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // شريط العنوان مع زخرفة إسلامية وشارة اليوم
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "آية اليوم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenDark
                        )
                        Text(
                            text = "تتجدد تلقائياً كل يوم • ${ayah.theme}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // شارة اسم السورة ورقم الآية
                Surface(
                    color = GoldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(GoldAccent, GoldAccentDark)),
                        width = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${ayah.surahName} [${ayah.ayahNumber}]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccentDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // فاصل زخرفي ناعم
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GoldAccent.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // نص الآية الكريمة داخل إطار إسلامي أنيق ومريح للقراءة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(0.8.dp, GoldAccent.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "﴿ ${ayah.ayahText} ﴾",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B382B),
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // وقفة تدبر وتأمل قابلة للتوسيع
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showReflection = !showReflection }
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "وقفة تدبرية مع الآية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreenPrimary
                    )
                }

                Icon(
                    imageVector = if (showReflection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = EmeraldGreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = showReflection) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldGreenPrimary.copy(alpha = 0.06f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = ayah.reflection,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFF264639)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // أزرار التفاعل (فتح في المصحف، نسخ، مشاركة)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // زر الانتقال للمصحف
                if (onOpenQuranPage != null) {
                    FilledTonalButton(
                        onClick = { onOpenQuranPage(ayah.pageNumber) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = EmeraldGreenPrimary.copy(alpha = 0.12f),
                            contentColor = EmeraldGreenDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "صفحة ${ayah.pageNumber} بالمصحف",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // زر نسخ
                    IconButton(
                        onClick = {
                            val clip = ClipData.newPlainText(
                                "آية اليوم",
                                "﴿ ${ayah.ayahText} ﴾ [${ayah.surahName}: ${ayah.ayahNumber}]\nتطبيق سكينة"
                            )
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الآية الكريمة بنجاح 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // زر مشاركة
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "✨ آية اليوم:\n\n﴿ ${ayah.ayahText} ﴾\n[${ayah.surahName}: ${ayah.ayahNumber}]\n\nوقفة تدبرية: ${ayah.reflection}\n\n- من تطبيق سكينة"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة آية اليوم"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * بطاقة "حديث اليوم" النبوي بتصميم إسلامي فاخر يتغير تلقائياً كل يوم
 */
@Composable
fun HadithOfTheDayWidget(
    hadith: DailyHadith,
    onOpenNawawi: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showBenefit by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hadith_of_the_day_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F9F6) // لون أخضر عاجي ناعم هادئ للعين
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    EmeraldGreenPrimary.copy(alpha = 0.6f),
                    GoldAccent.copy(alpha = 0.5f),
                    EmeraldGreenPrimary.copy(alpha = 0.6f)
                )
            ),
            width = 1.2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // شريط العنوان
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FormatQuote,
                            contentDescription = null,
                            tint = GoldAccentDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "حديث اليوم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenDark
                        )
                        Text(
                            text = "عن رسول الله ﷺ • يتجدد يومياً",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // شارة عنوان الحديث
                Surface(
                    color = EmeraldGreenPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(EmeraldGreenPrimary, EmeraldGreenLight)),
                        width = 1.dp
                    )
                ) {
                    Text(
                        text = hadith.title,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // فاصل زخرفي
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, EmeraldGreenPrimary.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // الراوي
            Text(
                text = hadith.narrator,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4A6B5D),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // متن الحديث الشريف
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.7f))
                    .border(0.8.dp, EmeraldGreenPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = hadith.matn,
                    fontSize = 15.sp,
                    lineHeight = 27.sp,
                    color = Color(0xFF1E3A2F),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // الفائدة المستفادة قابلة للتوسيع
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showBenefit = !showBenefit }
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الفائدة المستفادة من الحديث",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccentDark
                    )
                }

                Icon(
                    imageVector = if (showBenefit) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = GoldAccentDark,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = showBenefit) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldAccent.copy(alpha = 0.08f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = hadith.benefit,
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFF4A3E1B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // أسفل البطاقة (التخريج وأزرار المشاركة والنسخ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // التخريج
                Text(
                    text = hadith.reference,
                    fontSize = 11.sp,
                    color = EmeraldGreenDark,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onOpenNawawi != null) {
                        TextButton(
                            onClick = onOpenNawawi,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "الأحاديث النبوية ←",
                                fontSize = 11.sp,
                                color = EmeraldGreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // زر نسخ
                    IconButton(
                        onClick = {
                            val clip = ClipData.newPlainText(
                                "حديث اليوم",
                                "${hadith.narrator}\n${hadith.matn}\n[${hadith.reference}]\nتطبيق سكينة"
                            )
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الحديث الشريف بنجاح 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // زر مشاركة
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "✨ حديث اليوم:\n\n${hadith.narrator}\n\n${hadith.matn}\n\n[${hadith.reference}]\n\nالفائدة: ${hadith.benefit}\n\n- من تطبيق سكينة"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة حديث اليوم"))
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
