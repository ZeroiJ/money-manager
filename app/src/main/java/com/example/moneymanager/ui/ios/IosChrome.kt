package com.example.moneymanager.ui.ios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun currentScheme(dark: Boolean = isSystemInDarkTheme()): IosScheme =
    if (dark) Ios.Dark else Ios.Light

data class IosTabItem(val title: String, val icon: ImageVector)

/**
 * Kinetic Ledger tab bar (PRD §3): full-width dark slab, numbered mono
 * labels `01 HOME`, ochre active state, hairline top. Owns bottom+horizontal
 * insets (content must NOT also pad bottom — see NavGraph).
 */
@Composable
fun IosTabBar(
    selected: Int,
    items: List<IosTabItem>,
    onSelect: (Int) -> Unit,
    scheme: IosScheme = currentScheme()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(scheme.slab)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
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
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selected
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(0.dp))
                        .background(if (isSelected) scheme.accent else Color.Transparent)
                        .clickable { onSelect(index) }
                        .defaultMinSize(minWidth = 72.dp, minHeight = 52.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) scheme.slab else scheme.onSlabSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.title,
                        fontFamily = PaperMono,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isSelected) scheme.slab else scheme.onSlabSecondary,
                        maxLines = 1
                    )
                }
            }
        }
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
                .clip(RoundedCornerShape(Ios.CardRadius))
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
                    .clip(RoundedCornerShape(8.dp))
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
    scheme: IosScheme = currentScheme()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Ios.CardRadius))
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
