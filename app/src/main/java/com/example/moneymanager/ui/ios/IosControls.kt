package com.example.moneymanager.ui.ios

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanager.theme.Geist
import com.example.moneymanager.theme.Ios
import com.example.moneymanager.theme.IosScheme
import com.example.moneymanager.theme.PaperMono

/** Segmented picker (period, scope, type filters). 44dp minimum height. */
@Composable
fun IosSegmented(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scheme: IosScheme = currentScheme()
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(scheme.secondaryFill)
            .padding(2.dp)
            .defaultMinSize(minHeight = 44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) scheme.card else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontFamily = Geist,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (isSelected) scheme.label else scheme.secondaryLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Settings-style toggle row with system switch. */
@Composable
fun IosSwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    scheme: IosScheme = currentScheme()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                    fontFamily = Geist,
                    fontSize = 13.sp,
                    color = scheme.secondaryLabel
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = scheme.success,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = scheme.secondaryFill,
                uncheckedThumbColor = Color.White
            )
        )
    }
}

/** Budget progress bar. Fill clamps at full width; overspend shows destructive. */
@Composable
fun IosProgressBar(
    progress: Float,
    overBudget: Boolean,
    modifier: Modifier = Modifier,
    scheme: IosScheme = currentScheme()
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(scheme.secondaryFill)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (overBudget) scheme.destructive else scheme.success)
        )
    }
}

/** Amount figure in Paper Mono semibold — the single style for all ₹ numbers. */
@Composable
fun IosAmount(
    text: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 17.sp
) {
    Text(
        text = text,
        fontFamily = PaperMono,
        fontWeight = FontWeight.SemiBold,
        fontSize = fontSize,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Rounded 16dp modal sheet with drag handle over a dimmed scrim. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    scheme: IosScheme = currentScheme(),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = Ios.SheetRadius, topEnd = Ios.SheetRadius),
        containerColor = scheme.card,
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            content = content
        )
    }
}
