package com.example.moneymanager.ui.ios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.theme.Ios
import com.example.moneymanager.theme.IosScheme
import com.example.moneymanager.theme.PaperMono

/**
 * The app is LIGHT-MODE ONLY (Sage/Paper Kinetic Ledger). The Obsidian dark
 * variant still exists in [Ios.Dark] but is never selected — the rich palette
 * reads best on the light canvas. Ignore the system dark setting entirely.
 */
@Composable
fun currentScheme(dark: Boolean = false): IosScheme =
    if (dark) Ios.Dark else Ios.Light

/**
 * Commit rail (v1.19.0): replaces the 3-cell tab bar. Full-width dark slab
 * with hairline top; a 2/3 ochre `+ EXPENSE ↗` commit key plus a 1/3
 * HOME/REPORTS flip switch. Tabs navigate, never act — the key jumps to
 * the Expense tab. Draws edge-to-edge into the gesture zone (each column's
 * color runs to the physical bottom edge); only horizontal insets are
 * padded. Content must NOT also pad bottom — see NavGraph.
 */
@Composable
fun IosCommitRail(
    selectedHome: Boolean,
    selectedReports: Boolean,
    onSelectHome: () -> Unit,
    onSelectExpense: () -> Unit,
    onSelectReports: () -> Unit,
    scheme: IosScheme = currentScheme()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.slab)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(scheme.label.copy(alpha = 0.55f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            // Expense commit key — always ochre, always one tap away.
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .background(scheme.accent)
                    .clickable { onSelectExpense() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ EXPENSE ↗",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = com.example.moneymanager.ui.kinetic.KineticInk,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(scheme.onSlab.copy(alpha = 0.2f))
            )
            // HOME / REPORTS flip switch.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                FlipCell(
                    label = "HOME",
                    active = selectedHome,
                    onClick = onSelectHome,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(scheme.onSlab.copy(alpha = 0.2f))
                )
                FlipCell(
                    label = "REPORTS",
                    active = selectedReports,
                    onClick = onSelectReports,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        // Edge extension: carry each column's color down through the system
        // gesture zone to the physical bottom edge — no dead gap, no
        // misalignment. Non-tappable; the 96dp row above stays clear of
        // gesture conflicts.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    WindowInsets.safeDrawing.asPaddingValues()
                        .calculateBottomPadding()
                )
        ) {
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .background(scheme.accent)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(scheme.onSlab.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(scheme.slab)
            )
        }
    }
}

@Composable
private fun FlipCell(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scheme: IosScheme = currentScheme()
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (active) scheme.card else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = PaperMono,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp,
            color = if (active) scheme.label else scheme.onSlabSecondary,
            maxLines = 1
        )
    }
}

/** Settings-style section: mono micro-header + one square block of rows. */
@Composable
fun IosGroupedList(
    header: String?,
    scheme: IosScheme = currentScheme(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (header != null) {
            Text(
                text = header.uppercase(),
                fontFamily = PaperMono,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = scheme.secondaryLabel,
                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RectangleShape)
                .background(scheme.card)
                .padding(horizontal = 16.dp),
            content = content
        )
    }
}

/**
 * 56dp grouped row. Subtitle/amount ellipsize instead of pushing trailing
 * content off-screen at large font scales.
 */
@Composable
fun IosRow(
    title: String,
    subtitle: String?,
    icon: ImageVector?,
    iconTint: Color,
    scheme: IosScheme = currentScheme(),
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val click = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .then(click)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RectangleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                color = scheme.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontFamily = PaperMono,
                    fontSize = 13.sp,
                    color = scheme.secondaryLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        trailing()
        if (showChevron) {
            Text(
                text = "›",
                fontFamily = SpaceGrotesk,
                fontSize = 20.sp,
                color = scheme.secondaryLabel,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Feed failure card: tells what failed and why, with a retry action.
 * Shown INSTEAD of the list (which may be stale/empty) — never silently blank.
 */
@Composable
fun LoadErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    scheme: IosScheme = currentScheme()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            .background(scheme.card)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Couldn't load",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = scheme.destructive
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            fontFamily = SpaceGrotesk,
            fontSize = 14.sp,
            color = scheme.secondaryLabel
        )
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.TextButton(onClick = onRetry) {
            Text(
                text = "Try again",
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = scheme.accent
            )
        }
    }
}
