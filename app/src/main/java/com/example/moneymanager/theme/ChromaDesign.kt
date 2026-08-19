package com.example.moneymanager.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Chroma Hard-Edge Precision Shadow Modifier.
 * Subtle, crisp offset shadow reflecting Chroma's retro-mac desktop window aesthetic.
 */
fun Modifier.chromaShadow(
    offset: Dp = 3.dp,
    shadowColor: Color = ChromaBlack,
    cornerRadius: Dp = 4.dp
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val offsetPx = offset.toPx()

    if (offsetPx > 0) {
        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(offsetPx, offsetPx),
            size = size,
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )
    }
}

/**
 * Chroma Window Card:
 * Inspired by Chroma's retro Mac OS / NeXT / Terminal window design.
 * Features an optional top header bar with terminal title and status indicator.
 */
@Composable
fun ChromaCard(
    modifier: Modifier = Modifier,
    windowTitle: String? = null,
    statusIndicator: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = 1.5.dp,
    shadowOffset: Dp = 3.dp,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    cornerRadius: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .chromaShadow(
                offset = shadowOffset,
                shadowColor = shadowColor,
                cornerRadius = cornerRadius
            )
            .clip(shape)
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Window Header Bar if title is provided
            if (windowTitle != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChromaStone200)
                        .border(
                            width = 0.5.dp,
                            color = borderColor.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Retro window square dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ChromaBlack)
                                .border(0.5.dp, ChromaStone400)
                        )
                        Text(
                            text = windowTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = ChromaBlack
                        )
                    }

                    if (statusIndicator != null) {
                        Text(
                            text = statusIndicator,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            ),
                            color = ChromaStone600
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

/**
 * Chroma Monospace Tag/Badge (e.g. `[ PERSONAL ]`, `[ 15M+ ]`)
 */
@Composable
fun ChromaBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ChromaStone100,
    textColor: Color = ChromaBlack,
    borderColor: Color = ChromaBlack,
    borderWidth: Dp = 1.dp
) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            ),
            color = textColor
        )
    }
}

/**
 * Chroma Action Button:
 * Precision button with monospace font, crisp outline, and subtle drop shadow.
 */
@Composable
fun ChromaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ChromaBlack,
    textColor: Color = ChromaWhite,
    borderColor: Color = ChromaBlack,
    shadowOffset: Dp = 3.dp,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(4.dp)

    Box(
        modifier = modifier
            .chromaShadow(
                offset = if (enabled) shadowOffset else 0.dp,
                shadowColor = borderColor,
                cornerRadius = 4.dp
            )
            .clip(shape)
            .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f))
            .border(width = 1.5.dp, color = borderColor, shape = shape)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
        )
    }
}
