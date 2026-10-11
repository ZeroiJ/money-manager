package com.example.moneymanager.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

/**
 * iOS Liquid Glass Lite tokens (see spec §3).
 * Light = warm system grouped colors; Dark = true-black grouped colors.
 * Gradient lists drive [ui.ios.AmbientBackground]; cards always stay solid
 * so text contrast never depends on the animated background.
 */
data class IosScheme(
    val background: Color,
    val card: Color,
    val secondaryFill: Color,
    val label: Color,
    val secondaryLabel: Color,
    val accent: Color,
    val destructive: Color,
    val success: Color,
    val separator: Color,
    val gradient: List<Color>
)

object Ios {
    val CardRadius: Dp = 12.dp
    val SheetRadius: Dp = 16.dp

    val Light = IosScheme(
        background = Color(0xFFF2F2F7),
        card = Color(0xFFFFFFFF),
        secondaryFill = Color(0xFFE5E5EA),
        label = Color(0xFF000000),
        secondaryLabel = Color(0xFF6E6E73),
        accent = Color(0xFF007AFF),
        destructive = Color(0xFFFF3B30),
        success = Color(0xFF34C759),
        separator = Color(0x61C6C6C8),
        gradient = listOf(Color(0xFFFFF6EA), Color(0xFFFFDFB5), Color(0xFFFF9500))
    )

    val Dark = IosScheme(
        background = Color(0xFF000000),
        card = Color(0xFF1C1C1E),
        secondaryFill = Color(0xFF2C2C2E),
        label = Color(0xFFFFFFFF),
        secondaryLabel = Color(0xFFAEAEB2),
        accent = Color(0xFF0A84FF),
        destructive = Color(0xFFFF453A),
        success = Color(0xFF30D158),
        separator = Color(0xFF38383A),
        gradient = listOf(Color(0xFF060B24), Color(0xFF101C4E), Color(0xFF0A84FF))
    )
}

/** WCAG 2.x contrast ratio of opaque [fg] on opaque [bg]. */
fun contrastRatio(fg: Color, bg: Color): Double {
    fun linear(c: Float): Double {
        val s = c.toDouble()
        return if (s <= 0.04045) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
    }
    fun luminance(c: Color) =
        0.2126 * linear(c.red) + 0.7152 * linear(c.green) + 0.0722 * linear(c.blue)
    val (hi, lo) = listOf(luminance(fg), luminance(bg)).sortedDescending()
    return (hi + 0.05) / (lo + 0.05)
}
