package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.utils.ThemeManager

// Dark theme styling: Charcoal #121212 background, #1E1E1E surfaces, high contrast soothing text
private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF1E382B),
    onPrimaryContainer = GoldAccentLight,
    secondary = EmeraldGreenLight,
    onSecondary = Color(0xFF121212),
    secondaryContainer = Color(0xFF163224),
    onSecondaryContainer = Color(0xFFE2F7ED),
    tertiary = GoldAccentLight,
    background = Color(0xFF121212), // Dark Slate/Charcoal #121212
    onBackground = Color(0xFFF1F5F9), // Pure soothing high-contrast light text
    surface = Color(0xFF1E1E1E),     // Dark surface #1E1E1E
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF262626), // Card & container backgrounds in dark mode
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0x40D4AF37),
    outlineVariant = Color(0xFF383838)
)

// Radiant, cheerful and comfortable light scheme
private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldGreenContainer,
    onPrimaryContainer = OnEmeraldGreenContainer,
    secondary = GoldAccentDark,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = GoldAccentDark,
    tertiary = GoldAccent,
    background = CreamPaperBackground, // #F8F9FA White Creamy Paper
    onBackground = TextDarkPrimary,
    surface = WhiteSurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = SoftGreenCard, // Soft Light Green for containers/windows
    onSurfaceVariant = TextDarkSecondary,
    outline = CardBorderGold,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun SakinaTheme(
    darkTheme: Boolean = ThemeManager.isDark,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
