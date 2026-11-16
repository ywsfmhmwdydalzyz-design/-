package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.Font
import com.example.R

val KfgqpcUthmanicHafsFontFamily = FontFamily(
    Font(R.font.kfgqpc_uthmanic_hafs, FontWeight.Normal),
    Font(R.font.kfgqpc_uthmanic_hafs, FontWeight.Bold)
)

val QuranFontFamily = KfgqpcUthmanicHafsFontFamily

val AmiriFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold)
)

// Set of Material typography styles to start with
val Typography =
  Typography(
    bodyLarge =
      TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      )
  )

