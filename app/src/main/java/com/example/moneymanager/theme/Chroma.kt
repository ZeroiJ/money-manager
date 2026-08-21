package com.example.moneymanager.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Chroma {

    object color {
        val primary = ChromaPrimary
        val onPrimary = ChromaWhite
        val primaryContainer = ChromaOrange
        val onPrimaryContainer = ChromaWhite
        val secondary = ChromaSecondary
        val onSecondary = ChromaBlack
        val secondaryContainer = ChromaStone100
        val onSecondaryContainer = ChromaBlack
        val tertiary = ChromaGreen
        val onTertiary = ChromaWhite
        val background = ChromaSurface
        val onBackground = ChromaOnSurface
        val surface = ChromaSurface
        val onSurface = ChromaOnSurface
        val surfaceVariant = ChromaStone200
        val onSurfaceVariant = ChromaStone600
        val surfaceContainer = ChromaStone200
        val outline = ChromaBlack
        val outlineVariant = ChromaStone400
        val error = ChromaRed
        val onError = ChromaWhite
    }

    object type {
        val displayLarge = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 48.sp
        )
        val displayMedium = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 36.sp
        )
        val displaySmall = TextStyle(
            fontFamily = PlexMono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp,
            lineHeight = 36.sp
        )
        val headlineLarge = TextStyle(
            fontFamily = PlexMono,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp
        )
        val headlineMedium = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        )
        val headlineSmall = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 28.sp
        )
        val titleLarge = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp
        )
        val titleMedium = TextStyle(
            fontFamily = PlexMono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        val titleSmall = TextStyle(
            fontFamily = PlexMono,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        val bodyLarge = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        val bodyMedium = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        val bodySmall = TextStyle(
            fontFamily = Inter,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        val labelLarge = TextStyle(
            fontFamily = PlexMono,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        val labelMedium = TextStyle(
            fontFamily = PlexMono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        val labelSmall = TextStyle(
            fontFamily = PlexMono,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 15.sp
        )
    }
}
