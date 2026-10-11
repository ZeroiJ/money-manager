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
import com.example.moneymanager.theme.Geist
import com.example.moneymanager.theme.Ios
import com.example.moneymanager.theme.IosScheme
import com.example.moneymanager.theme.PaperMono

@Composable
fun currentScheme(dark: Boolean = isSystemInDarkTheme()): IosScheme =
    if (dark) Ios.Dark else Ios.Light

data class IosTabItem(val title: String, val icon: ImageVector)

/**
 * Floating frosted tab bar. True backdrop blur needs OS support, so frost is
 * approximated with a translucent card + top separator (see spec §3 blur rule).
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
            .background(scheme.card.copy(alpha = 0.85f))
            // Own the bottom + horizontal insets: sits clear of the gesture
            // bar and cutouts. (Content must NOT also pad bottom — see NavGraph.)
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
                .background(scheme.separator)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selected
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) scheme.accent.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable { onSelect(index) }
                        .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) scheme.accent else scheme.secondaryLabel,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        fontFamily = Geist,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 11.sp,
                        color = if (isSelected) scheme.accent else scheme.secondaryLabel
                    )
                }
            }
        }
    }
}

/** iOS Settings-style section: small header + one rounded card of rows. */
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
                fontFamily = Geist,
                fontSize = 13.sp,
                color = scheme.secondaryLabel,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
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
                fontFamily = Geist,
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
                fontFamily = Geist,
                fontSize = 20.sp,
                color = scheme.secondaryLabel,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
