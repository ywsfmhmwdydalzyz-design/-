package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreenPrimary
import com.example.ui.theme.GoldAccent

enum class SakinaTab(
    val titleArabic: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val testTag: String
) {
    HOME("الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    QURAN("القرآن", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "tab_quran"),
    AZKAR("الأذكار", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "tab_azkar"),
    NAWAWI("الأربعين", Icons.Filled.Book, Icons.Outlined.Book, "tab_nawawi"),
    PRAYERS("المواقيت", Icons.Filled.Schedule, Icons.Outlined.Schedule, "tab_prayers"),
    DUAS("الأدعية", Icons.Filled.VolunteerActivism, Icons.Outlined.VolunteerActivism, "tab_duas")
}

@Composable
fun SakinaBottomBar(
    currentTab: SakinaTab,
    onTabSelected: (SakinaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("sakina_bottom_navigation"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        SakinaTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.iconFilled else tab.iconOutlined,
                        contentDescription = tab.titleArabic,
                        tint = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = tab.titleArabic,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EmeraldGreenPrimary.copy(alpha = 0.15f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
