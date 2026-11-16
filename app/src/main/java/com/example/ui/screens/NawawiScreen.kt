package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.NawawiHadith
import com.example.data.sources.NawawiDataSource
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight

data class NawawiCategory(val id: String, val titleArabic: String)

private val nawawiCategories = listOf(
    NawawiCategory("all", "الكل (42)"),
    NawawiCategory("1-10", "الأحاديث 1 - 10"),
    NawawiCategory("11-20", "الأحاديث 11 - 20"),
    NawawiCategory("21-30", "الأحاديث 21 - 30"),
    NawawiCategory("31-42", "الأحاديث 31 - 42")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NawawiScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("all") }
    var selectedHadith by remember { mutableStateOf<NawawiHadith?>(null) }

    val filteredHadiths = remember(searchQuery, selectedCategoryId) {
        val base = when (selectedCategoryId) {
            "1-10" -> NawawiDataSource.hadiths.filter { it.id in 1..10 }
            "11-20" -> NawawiDataSource.hadiths.filter { it.id in 11..20 }
            "21-30" -> NawawiDataSource.hadiths.filter { it.id in 21..30 }
            "31-42" -> NawawiDataSource.hadiths.filter { it.id in 31..42 }
            else -> NawawiDataSource.hadiths
        }
        if (searchQuery.isBlank()) {
            base
        } else {
            val q = searchQuery.trim()
            base.filter {
                it.title.contains(q, ignoreCase = true) ||
                        it.matn.contains(q, ignoreCase = true) ||
                        it.narrator.contains(q, ignoreCase = true) ||
                        it.id.toString() == q
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("nawawi_screen_content")
    ) {
        // Header Banner & Search
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الأربعون النووية الشاملة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isDark = com.example.utils.ThemeManager.isDark

                        IconButton(
                            onClick = { com.example.utils.ThemeManager.toggleTheme() },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .testTag("nawawi_theme_toggle_button")
                        ) {
                            Text(
                                text = if (isDark) "☀️" else "🌙",
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(text = "٤٢ حديثاً 100%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Instant Search Field - مرتفع للأعلى وملاصق مباشرة تحت العنوان بهوامش مضغوطة وأنيقة
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث في عنوان أو نص الحديث...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Clear, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(15.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = EmeraldGreenPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nawawi_search_field")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Category Chips Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    nawawiCategories.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(text = cat.titleArabic, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("nawawi_category_${cat.id}")
                        )
                    }
                }
            }
        }

        // Hadiths List - تبدأ مباشرة بعد الهيدر بمساحة واضحة دون أي تداخل
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("nawawi_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredHadiths) { hadith ->
                HadithSummaryCard(
                    hadith = hadith,
                    onClick = { selectedHadith = hadith }
                )
            }
        }
    }

    // Detail Bottom Sheet for Hadith
    selectedHadith?.let { hadith ->
        HadithDetailBottomSheet(
            hadith = hadith,
            onDismiss = { selectedHadith = null }
        )
    }
}

@Composable
fun HadithSummaryCard(
    hadith: NawawiHadith,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("hadith_item_${hadith.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent.copy(alpha = 0.4f), Color.Transparent)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(EmeraldGreenPrimary.copy(alpha = 0.12f))
                    .border(1.dp, GoldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${hadith.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EmeraldGreenPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hadith.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hadith.narrator,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "قراءة ←",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldAccent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithDetailBottomSheet(
    hadith: NawawiHadith,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedFeedback by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("nawawi_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hadith.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = hadith.narrator,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            val textToCopy = "${hadith.title}\n${hadith.narrator}\n\n${hadith.matn}\n\n${hadith.reference}\n\n[الشرح]: ${hadith.explanation}"
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            copiedFeedback = true
                        },
                        modifier = Modifier.testTag("copy_hadith_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "نسخ الحديث",
                            tint = if (copiedFeedback) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_hadith_modal_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "إغلاق")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Matn with Tashkeel Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = hadith.matn,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reference
            Text(
                text = "التوثيق والتخريج: ${hadith.reference}",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldGreenDark,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation Section
            Text(
                text = "الشرح الميسر:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(14.dp)
            ) {
                Text(
                    text = hadith.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Benefits Section
            Text(
                text = "من الفوائد المستفادة:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                hadith.benefits.forEachIndexed { index, benefit ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "• ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreenPrimary
                        )
                        Text(
                            text = benefit,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
