package com.example.moneymanager.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

/**
 * Kinetic Ledger tokens (PRD §5, see docs/superpowers/plans/2026-10-13-kinetic-ledger-redesign.md).
 *
 * Light = sage canvas + paper panels + dark-olive slabs, ochre accent,
 * terracotta alerts. Dark = Phase 3 Obsidian + safety amber.
 * Field names are kept from the earlier iOS scheme so call sites survive;
 * semantics are documented per field.
 */
data class IosScheme(
    /** Page substrate: sage (light) / obsidian (dark). Content sits on this. */
    val background: Color,
    /** Paper panel: sheets, dialogs, quiet cards — ink text lives here. */
    val card: Color,
    /** Raised/secondary fill: chips, inactive controls, hairline-adjacent tiles. */
    val secondaryFill: Color,
    /** Primary ink on [background] and [card]. */
    val label: Color,
    /** Muted ink: metadata, timestamps, mono micro-labels. */
    val secondaryLabel: Color,
    /** Accent = ochre (actions, hero figures, add targets, active states). */
    val accent: Color,
    /** Alert = terracotta (overruns, destructive confirmations). */
    val destructive: Color,
    val success: Color,
    /** Brutalist hairline — dark ink at low alpha on light, light on dark. */
    val separator: Color,
    /** Ambient texture quads (frozen mesh + grain; never a decorative glow). */
    val gradient: List<Color>,
    /** Dark slab: transaction rows, category tiles, tab bar. */
    val slab: Color,
    /** Second slab tone: alternating rows / stacked tiles. */
    val slabAlt: Color,
    /** Primary text on slabs. */
    val onSlab: Color,
    /** Muted text on slabs (category labels, timestamps). */
    val onSlabSecondary: Color,
    /** Deep maroon: over-budget allocation card. */
    val maroon: Color,
    /** Paper: brightest surface (hero panels). */
    val paper: Color
)

object Kinetic {
    val CardRadius: Dp = 0.dp      // brutalist: squares, hairlines, no soft cards
    val SheetRadius: Dp = 14.dp    // sheets keep a small grabber radius

    val Light = IosScheme(
        background = Color(0xFFB5BFAE),   // sage canvas
        card = Color(0xFFF3FCEA),         // paper panel
        secondaryFill = Color(0xFFC1CBB9), // raised sage
        label = Color(0xFF1E241C),         // ink
        secondaryLabel = Color(0xFF56604F),
        accent = Color(0xFFD9C852),        // ochre mustard
        destructive = Color(0xFFD76449),   // terracotta coral
        success = Color(0xFF3F7D4E),
        separator = Color(0x59263021),
        gradient = listOf(
            Color(0xFFB5BFAE), Color(0xFFC1CBB9), Color(0xFFA8B2A0)
        ),
        slab = Color(0xFF1E241C),
        slabAlt = Color(0xFF2C362A),
        onSlab = Color(0xFFF3FCEA),
        onSlabSecondary = Color(0xFFA7B09C),
        maroon = Color(0xFF4A1510),
        paper = Color(0xFFF3FCEA)
    )

    val Dark = IosScheme(
        background = Color(0xFF0A0F0D),   // obsidian
        card = Color(0xFF141A17),
        secondaryFill = Color(0xFF1E241C),
        label = Color(0xFFF3FCEA),
        secondaryLabel = Color(0xFF8A9484),
        accent = Color(0xFFEFBF3A),        // safety amber
        destructive = Color(0xFFE0765C),   // terracotta lifted for dark
        success = Color(0xFF63B377),
        separator = Color(0x4D7E8A78),
        gradient = listOf(
            Color(0xFF0A0F0D), Color(0xFF101613), Color(0xFF1A2320)
        ),
        slab = Color(0xFF161C19),
        slabAlt = Color(0xFF1E2622),
        onSlab = Color(0xFFF3FCEA),
        onSlabSecondary = Color(0xFF8A9484),
        maroon = Color(0xFF3A100C),
        paper = Color(0xFF141A17)
    )
}

/** Back-compat alias: older call sites reference `Ios`. */
val Ios: Kinetic get() = Kinetic

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
