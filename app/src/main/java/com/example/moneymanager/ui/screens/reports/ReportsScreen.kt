package com.example.moneymanager.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.theme.IosScheme
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.IosProgressBar
import com.example.moneymanager.ui.ios.IosSegmented
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.maskedCurrency
import com.example.moneymanager.ui.kinetic.KineticHeader
import com.example.moneymanager.ui.kinetic.Hairline
import com.example.moneymanager.ui.kinetic.LedgerAmount
import com.example.moneymanager.ui.kinetic.LedgerPill
import com.example.moneymanager.ui.kinetic.MonoLabel
import com.example.moneymanager.util.FormatUtils

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val personalSpend by viewModel.personalExpense.collectAsState()
    val householdSpend by viewModel.householdExpense.collectAsState()
    val categoryReports by viewModel.categoryReports.collectAsState()
    val dailyTrend by viewModel.dailySpendTrend.collectAsState()
    val heatmapDays by viewModel.monthlyCalendarHeatmap.collectAsState()
    val settlements by viewModel.householdSettlements.collectAsState()
    val scheme = currentScheme()

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var selectedHeatmapDay by remember { mutableStateOf<CalendarDayHeatmap?>(null) }
    val periods = ReportPeriod.values()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            KineticHeader(
                lead = "Audit",
                headline = "Reports",
                headlineSize = 28.sp
            )
        }

        item {
            IosSegmented(
                options = listOf("WEEK", "MONTH", "QTR", "YEAR", "ALL"),
                selected = periods.indexOf(selectedPeriod).coerceAtLeast(0),
                onSelect = { viewModel.selectedPeriod.value = periods[it] },
                scheme = scheme
            )
        }

        // ── Accumulated outflow metric (PRD §4.4) ───────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.card)
                    .padding(16.dp)
            ) {
                MonoLabel(text = "Accumulated outflow", color = scheme.secondaryLabel)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LedgerAmount(
                        text = FormatUtils.formatLedger(totalExpense),
                        fontSize = 40.sp,
                        color = scheme.label,
                        dimmedColor = scheme.secondaryLabel
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    LedgerPill(
                        text = "IN ${FormatUtils.formatLedger(totalIncome)}",
                        container = scheme.accent,
                        content = scheme.label
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Hairline()
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        MonoLabel(text = "Personal", color = scheme.secondaryLabel)
                        LedgerAmount(
                            text = FormatUtils.formatLedger(personalSpend),
                            fontSize = 17.sp,
                            color = scheme.label,
                            dimmedColor = scheme.secondaryLabel
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        MonoLabel(text = "Household", color = scheme.secondaryLabel)
                        LedgerAmount(
                            text = FormatUtils.formatLedger(householdSpend),
                            fontSize = 17.sp,
                            color = scheme.label,
                            dimmedColor = scheme.secondaryLabel
                        )
                    }
                }
            }
        }

        item {
            IosSegmented(
                options = listOf("Category", "Trend", "Heatmap"),
                selected = selectedTab,
                onSelect = { selectedTab = it },
                scheme = scheme
            )
        }

        when (selectedTab) {
            0 -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(scheme.card)
                            .padding(16.dp)
                    ) {
                        ScopeRow(
                            label = "Personal",
                            amount = personalSpend,
                            color = scheme.accent,
                            scheme = scheme
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScopeRow(
                            label = "Household",
                            amount = householdSpend,
                            color = Color(0xFFFF9500),
                            scheme = scheme
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val total = (personalSpend + householdSpend).coerceAtLeast(1.0)
                        val personalFraction =
                            (personalSpend / total).toFloat().coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(scheme.secondaryFill)
                        ) {
                            if (personalSpend > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = personalFraction.coerceAtLeast(0.01f))
                                        .height(10.dp)
                                        .background(scheme.accent)
                                )
                            }
                        }
                    }
                }

                if (categoryReports.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(scheme.card)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Spending by category",
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                color = scheme.label,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier.size(168.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IosDonutChart(
                                    categories = categoryReports,
                                    totalSpend = totalExpense,
                                    scheme = scheme
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "SPENT",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 11.sp,
                                        color = scheme.secondaryLabel
                                    )
                                    IosAmount(
                                        text = FormatUtils.formatLedger(totalExpense),
                                        color = scheme.label,
                                        fontSize = 17.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            var showAll by remember { mutableStateOf(false) }
                            val visible =
                                if (showAll) categoryReports else categoryReports.take(5)

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val palette = donutPalette(scheme)
                                visible.forEachIndexed { index, catReport ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(palette[index % palette.size])
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = catReport.category.name,
                                            modifier = Modifier.weight(1f),
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = scheme.label,
                                            maxLines = 1
                                        )
                                        IosAmount(
                                            text = FormatUtils.formatLedger(catReport.amount),
                                            color = scheme.label,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${(catReport.percentage * 100).toInt()}%",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 13.sp,
                                            color = scheme.secondaryLabel
                                        )
                                    }
                                }

                                if (categoryReports.size > 5) {
                                    TextButton(
                                        onClick = { showAll = !showAll },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(
                                            text = if (showAll) "Show top 5"
                                            else "Show all (${categoryReports.size})",
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = scheme.accent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (settlements.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(scheme.card)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Settle up",
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp,
                                color = scheme.label
                            )
                            settlements.forEach { balance ->
                                val isOwed = balance.netBalance > 0
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = balance.memberName,
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = scheme.label
                                        )
                                        Text(
                                            text = "Paid ${maskedCurrency(balance.totalPaid)} · Share ${maskedCurrency(balance.fairShare)}",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 13.sp,
                                            color = scheme.secondaryLabel
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        IosAmount(
                                            text = (if (isOwed) "+" else "−") +
                                                FormatUtils.formatLedger(
                                                    if (isOwed) balance.netBalance else -balance.netBalance
                                                ),
                                            color = if (isOwed) scheme.success else scheme.destructive,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (isOwed) "Gets back" else "Owes",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 11.sp,
                                            color = if (isOwed) scheme.success else scheme.destructive
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(scheme.card)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Daily spend",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = scheme.label
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        IosTrendBars(days = dailyTrend, scheme = scheme)
                    }
                }
            }
            else -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(scheme.card)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Spend calendar",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = scheme.label
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = scheme.secondaryLabel,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.height(190.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(heatmapDays) { day ->
                                val cellColor = when (day.intensityLevel) {
                                    0 -> scheme.secondaryFill
                                    1 -> scheme.accent.copy(alpha = 0.25f)
                                    2 -> scheme.accent.copy(alpha = 0.55f)
                                    3 -> Color(0xFFFF9500)
                                    else -> scheme.destructive
                                }
                                val onStrong = day.intensityLevel >= 3
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cellColor)
                                        .clickable { selectedHeatmapDay = day },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${day.dayNumber}",
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = if (day.intensityLevel > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (onStrong) Color.White else scheme.label
                                    )
                                }
                            }
                        }

                        if (selectedHeatmapDay != null) {
                            val d = selectedHeatmapDay!!
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(scheme.secondaryFill)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Day ${d.dayNumber} · ${maskedCurrency(d.spendAmount)}",
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = scheme.label
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScopeRow(label: String, amount: Double, color: Color, scheme: IosScheme) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = scheme.label
            )
        }
        LedgerAmount(
            text = FormatUtils.formatLedger(amount),
            fontSize = 15.sp,
            color = scheme.label,
            dimmedColor = scheme.secondaryLabel
        )
    }
}

// Data palette (Lens: charts keep their own palette, but Kinetic's is earthy —
// ochre/terracotta/olive family, no candy purples or sky blues).
private fun donutPalette(scheme: IosScheme): List<Color> = listOf(
    scheme.accent,          // ochre
    Color(0xFF3F7D4E),      // ledger green
    scheme.destructive,     // terracotta
    Color(0xFF7A8471),      // sage slate
    Color(0xFFB98B4A),      // brass
    scheme.maroon,          // deep maroon
    Color(0xFF5C6B54),      // olive
    scheme.secondaryLabel
)

@Composable
fun IosDonutChart(
    categories: List<CategorySpendReport>,
    totalSpend: Double,
    scheme: IosScheme
) {
    val colors = donutPalette(scheme)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 18.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        if (totalSpend <= 0) {
            drawCircle(
                color = colors.last(),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
        } else {
            categories.forEachIndexed { index, item ->
                val sweepAngle = ((item.amount / totalSpend) * 360f).toFloat()
                val color = colors[index % colors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle - 2f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                startAngle += sweepAngle
            }
        }
    }
}

/** Rounded daily bars with a top-3 peak list. Replaces the ASCII flame graph. */
@Composable
private fun IosTrendBars(days: List<DailySpendPoint>, scheme: IosScheme) {
    if (days.isEmpty() || days.all { it.amount <= 0 }) {
        Text(
            text = "No expenses in the selected period.",
            fontFamily = SpaceGrotesk,
            fontSize = 15.sp,
            color = scheme.secondaryLabel,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        return
    }

    val maxAmount = days.maxOf { it.amount }.coerceAtLeast(1.0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val ratio = (day.amount / maxAmount).toFloat().coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((8 + ratio * 112).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (ratio >= 0.98f) Color(0xFFFF9500) else scheme.accent
                    )
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    val top = days.sortedByDescending { it.amount }.take(3)
    Text(
        text = "Highest days",
        fontFamily = SpaceGrotesk,
        fontSize = 13.sp,
        color = scheme.secondaryLabel
    )
    Spacer(modifier = Modifier.height(6.dp))
    top.forEach { day ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Day ${day.dayNumber}",
                fontFamily = SpaceGrotesk,
                fontSize = 13.sp,
                color = scheme.secondaryLabel,
                modifier = Modifier.width(56.dp)
            )
            IosAmount(
                text = FormatUtils.formatLedger(day.amount),
                color = scheme.label,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            IosProgressBar(
                progress = (day.amount / maxAmount).toFloat(),
                overBudget = false,
                scheme = scheme,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
