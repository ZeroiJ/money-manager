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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Neo-Brutalist Hard Drop Shadow Modifier.
 * Draws a solid black (or custom color) offset rectangle with zero blur behind the component.
 */
fun Modifier.neoShadow(
    offset: Dp = 4.dp,
    shadowColor: Color = Color.Black,
    cornerRadius: Dp = 6.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = Color.Black
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val offsetPx = offset.toPx()

    // Draw Hard Drop Shadow
    if (offsetPx > 0) {
        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(offsetPx, offsetPx),
            size = size,
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )
    }
}

@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = 2.5.dp,
    shadowOffset: Dp = 4.dp,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    cornerRadius: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .neoShadow(
                offset = shadowOffset,
                shadowColor = shadowColor,
                cornerRadius = cornerRadius,
                borderWidth = borderWidth,
                borderColor = borderColor
            )
            .clip(shape)
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun NeoBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoYellow,
    textColor: Color = NeoBlack,
    borderColor: Color = NeoBlack,
    borderWidth: Dp = 1.5.dp
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoYellow,
    textColor: Color = NeoBlack,
    borderColor: Color = NeoBlack,
    shadowOffset: Dp = 4.dp,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .neoShadow(
                offset = if (enabled) shadowOffset else 0.dp,
                shadowColor = borderColor,
                cornerRadius = 6.dp
            )
            .clip(shape)
            .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f))
            .border(width = 2.5.dp, color = borderColor, shape = shape)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f)
        )
    }
}
