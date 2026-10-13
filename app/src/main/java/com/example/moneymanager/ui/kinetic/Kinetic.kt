package com.example.moneymanager.ui.kinetic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanager.theme.IosScheme
import com.example.moneymanager.theme.PaperMono
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.ui.ios.currentScheme

/**
 * Kinetic Ledger component kit (PRD §5).
 * Brutalist/utilitarian: hairlines instead of radii, mono metadata,
 * barcode tick meters, greyed single-decimal figures.
 */

/** Fixed ink for type ON ochre/terracotta — never theme-dependent (contrast). */
val KineticInk = Color(0xFF1E241C)

/** Uppercase monospace micro-label — the technical metadata voice. */
@Composable
fun MonoLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.sp
) {
    Text(
        text = text.uppercase(),
        fontFamily = PaperMono,
        fontWeight = FontWeight.Medium,
        fontSize = fontSize,
        letterSpacing = 1.sp,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Single-decimal tabular amount (PRD format rule): integer part solid,
 * decimal part (".0") greyed — unifies numeric scale across the app.
 * Privacy mask renders bullets instead (GlobalHideAmounts contract).
 */
@Composable
fun LedgerAmount(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 17.sp,
    color: Color,
    dimmedColor: Color,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    if (com.example.moneymanager.ui.ios.LocalHideAmounts.current) {
        Text(
            text = "••••••",
            fontFamily = PaperMono,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = color,
            maxLines = 1,
            modifier = modifier
        )
        return
    }
    val dotIndex = text.indexOfLast { it == '.' }
    val annotated = buildAnnotatedString {
        if (dotIndex > 0) {
            append(text.substring(0, dotIndex + 1))
            withStyle(SpanStyle(color = dimmedColor)) {
                append(text.substring(dotIndex + 1))
            }
        } else {
            append(text)
        }
    }
    Text(
        text = annotated,
        fontFamily = PaperMono,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Vertical tick-line barcode meter (PRD §5 texture): encodes [fraction]
 * (0..1) as filled ticks over [total] slots. Purely representational —
 * paired with a numeric label, never the only signal.
 */
@Composable
fun BarcodeMeter(
    fraction: Float,
    modifier: Modifier = Modifier,
    total: Int = 24,
    color: Color,
    inactiveColor: Color = color.copy(alpha = 0.25f),
    strokeWidthPx: Float = 3f,
    gapPx: Float = 7f
) {
    Canvas(modifier = modifier) {
        val filled = (fraction.coerceIn(0f, 1f) * total).toInt()
        // Distribute ticks across the FULL width (PRD pinstripe bar).
        val step = if (total > 1) size.width / (total - 1) else size.width
        val tickH = size.height
        for (i in 0 until total) {
            val x = i * step
            val c = if (i < filled) color else inactiveColor
            // Slight height jitter keeps the barcode feeling printed, not CSS.
            val h = tickH * (0.62f + 0.38f * (((i * 37) % 13) / 13f))
            drawLine(
                color = c,
                start = Offset(x, (tickH - h) / 2f),
                end = Offset(x, (tickH + h) / 2f),
                strokeWidth = strokeWidthPx
            )
        }
    }
}

/**
 * Two-weight editorial header (reference screens): muted lead line +
 * ink headline. Space Grotesk both lines — no serif in Kinetic Ledger.
 */
@Composable
fun KineticHeader(
    lead: String,
    headline: String,
    modifier: Modifier = Modifier,
    scheme: IosScheme = currentScheme(),
    headlineSize: TextUnit = 30.sp
) {
    Column(modifier = modifier) {
        Text(
            text = lead,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = headlineSize * 0.72f,
            color = scheme.secondaryLabel
        )
        Text(
            text = headline,
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = headlineSize,
            color = scheme.label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Full-bleed slab block: dark olive tile with hairline separation —
 * the transaction row / category tile / tab bar surface.
 */
@Composable
fun Slab(
    modifier: Modifier = Modifier,
    scheme: IosScheme = currentScheme(),
    alt: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(if (alt) scheme.slabAlt else scheme.slab)
    ) { content() }
}

/** Hairline divider — Kinetic replacement for card outlines. */
@Composable
fun Hairline(modifier: Modifier = Modifier, scheme: IosScheme = currentScheme()) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(scheme.separator)
    )
}

/**
 * Primary commit button (PRD §4.3): full-width ink block, square,
 * ochre text marker `■ … ↗`. Press-shrink for tactility.
 */
@Composable
fun KineticButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scheme: IosScheme = currentScheme(),
    container: Color = scheme.label,
    content: Color = if (com.example.moneymanager.theme.contrastRatio(scheme.accent, scheme.label) >= 4.5)
        scheme.accent else scheme.background,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (enabled) container else container.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "■  $label  ↗",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 0.5.sp,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Inline status pill (EFF %, BUFFER, pacing deltas). */
@Composable
fun LedgerPill(
    text: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.background(container).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = PaperMono,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp,
            color = content,
            maxLines = 1
        )
    }
}

/** Small labeled metric pair used under heroes (BUDGET / LEFT / USED / RESERVE). */
@Composable
fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    scheme: IosScheme = currentScheme(),
    valueColor: Color = scheme.label,
    dimmedColor: Color = scheme.secondaryLabel
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        MonoLabel(text = label, color = scheme.secondaryLabel)
        Spacer(modifier = Modifier.weight(1f))
        LedgerAmount(
            text = value,
            fontSize = 22.sp,
            color = valueColor,
            dimmedColor = dimmedColor
        )
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxWidth().height(1.dp).background(scheme.separator)
    )
}
