package com.example.moneymanager.ui.ascii

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanager.theme.Chroma
import com.example.moneymanager.theme.ChromaBlack
import com.example.moneymanager.theme.ChromaButton
import com.example.moneymanager.theme.ChromaOrange
import com.example.moneymanager.theme.ChromaRed
import com.example.moneymanager.theme.ChromaStone300
import com.example.moneymanager.theme.ChromaStone400
import com.example.moneymanager.theme.ChromaWhite
import com.example.moneymanager.theme.PlexMono

/**
 * reducto+ASCII design contract — Wave 1.
 *
 * Flat, terminal-grade building blocks shared across all six screens.
 * "reducto" = strip visual weight (no chromaShadow in this file — borders and
 * ink only). Every glyph used here ("─", "█", "·", "│", "┌"…) exists in IBM Plex
 * Mono or falls back cleanly to the system mono font.
 */
object Ascii {

    /** Hairline used for flat borders. */
    val hairline: Color = ChromaStone400

    /** Strong ink border for interactive elements. */
    val hairlineStrong: Color = ChromaBlack

    /** Shared ₹ hero figure — pure box-drawing characters, 11 chars wide. */
    val rupeeArt: List<String> = listOf(
        " ██████████",
        " ██ ██ ██ █",
        " ██████████",
        " ██ ██ ██ █",
        " ██████████",
        " ██ ██ ██ █",
        " ██ ██ ██ █",
        " ██ ██ ██ █"
    )

    /** Minimal boot-log figure for empty states. */
    val emptyBoxArt: List<String> = listOf(
        " ┌────────┐",
        " │  empty │",
        " │  $ _   │",
        " └────────┘"
    )

    /** Horizontal box-drawing rule. */
    const val HR: String = "─"

    /** Vertical box-drawing pipe. */
    const val VT: String = "│"
}

/**
 * Flat horizontal ASCII rule. Replaces heavy offset shadows with a crisp
 * terminal hairline.
 */
@Composable
fun AsciiDivider(
    modifier: Modifier = Modifier,
    color: Color = Ascii.hairline,
    thickness: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .drawBehind {
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = thickness.toPx()
                )
            }
    )
}

/**
 * Section header in terminal comment syntax: `// CATEGORY_LIMITS`.
 * Optional rule line above for grouped hierarchy.
 */
@Composable
fun AsciiSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = ChromaBlack,
    showRule: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (showRule) {
            AsciiDivider(color = Ascii.hairline)
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            text = "// ${text.uppercase()}",
            style = Chroma.type.labelSmall.copy(
                fontFamily = PlexMono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * ASCII-art hero figure (default: the ₹ mark built from block characters).
 * Rendered line-by-line in monospace — no images, no canvas.
 */
@Composable
fun AsciiHeroFigure(
    lines: List<String> = Ascii.rupeeArt,
    modifier: Modifier = Modifier,
    color: Color = ChromaBlack,
    artSize: TextUnit = 12.sp,
    artLineHeight: TextUnit = 12.sp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        lines.forEach { line ->
            Text(
                text = line,
                fontFamily = PlexMono,
                fontWeight = FontWeight.Bold,
                fontSize = artSize,
                lineHeight = artLineHeight,
                color = color,
                softWrap = false,
                maxLines = 1
            )
        }
    }
}

/**
 * Terminal-style empty state: flat bordered box, prompt-prefixed title,
 * optional subtitle and action.
 */
@Composable
fun AsciiEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    accent: Color = ChromaBlack,
    borderColor: Color = Ascii.hairline,
    iconLines: List<String> = Ascii.emptyBoxArt
) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Chroma.color.surface)
            .border(1.dp, borderColor, shape)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsciiHeroFigure(
                lines = iconLines,
                color = ChromaStone400,
                artSize = 8.sp,
                artLineHeight = 8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "> ${title.uppercase()}",
                style = Chroma.type.titleSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold
                ),
                color = accent
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = Chroma.type.bodySmall,
                    color = Chroma.color.onSurfaceVariant
                )
            }
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ChromaButton(
                    text = actionLabel,
                    onClick = onAction,
                    backgroundColor = ChromaBlack,
                    textColor = ChromaWhite,
                    shadowOffset = 1.dp
                )
            }
        }
    }
}

/**
 * Block-glyph progress bar: `[████████··] 42%`.
 * Pure text — no Canvas, no shadow. Turns red when [overBudget].
 */
@Composable
fun AsciiMoodBar(
    progress: Float,
    modifier: Modifier = Modifier,
    segments: Int = 10,
    barColor: Color = ChromaOrange,
    overBudget: Boolean = false,
    label: String? = null
) {
    val clamped = progress.coerceIn(0f, 1f)
    val filled = (clamped * segments).toInt().coerceIn(0, segments)
    val effectiveBar = if (overBudget) ChromaRed else barColor
    val blockStyle = Chroma.type.labelSmall.copy(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
    )

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "[",
            style = blockStyle,
            color = Chroma.color.onSurfaceVariant
        )
        Text(
            text = "█".repeat(filled) + "·".repeat(segments - filled),
            style = blockStyle,
            color = effectiveBar
        )
        Text(
            text = "]",
            style = blockStyle,
            color = Chroma.color.onSurfaceVariant
        )
        if (label != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = blockStyle,
                color = if (overBudget) ChromaRed else Chroma.color.onSurfaceVariant
            )
        }
    }
}

/**
 * Flat segmented selector chip — the shared replacement for the old
 * chromaShadow Box chips. Selected chip fills with [selectedColor] and inverts
 * the text to [selectedTextColor].
 */
@Composable
fun AsciiSelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = ChromaBlack,
    selectedTextColor: Color = ChromaWhite,
    borderColor: Color = Ascii.hairlineStrong,
    textColor: Color = ChromaBlack
) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) selectedColor else Chroma.color.surface)
            .border(
                width = 1.dp,
                color = if (selected) selectedColor else borderColor,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = Chroma.type.labelSmall.copy(
                fontFamily = PlexMono,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
            ),
            color = if (selected) selectedTextColor else textColor,
            maxLines = 1
        )
    }
}