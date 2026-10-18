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
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.maskedCurrency
import com.example.moneymanager.ui.kinetic.Hairline
import com.example.moneymanager.ui.kinetic.LedgerAmount
import com.example.moneymanager.ui.kinetic.LedgerPill
import com.example.moneymanager.ui.kinetic.LedgerSegmented
import com.example.moneymanager.ui.kinetic.MonoLabel
import com.example.moneymanager.util.FormatUtils

/**
 * Reports on dark green (v1.21.0): one continuous slab surface, paper ink,
 * hairline dividers, monochrome paper-step data palette. No cards, no
 * candy colors. Same metrics and logic as before.
 */
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
    val periods = ReportPeriod.values()

    // Single-ink voice on the slab: paper type, dim metadata, faint rules.
    val ink = scheme.onSlab
    val dim = scheme.onSlabSecondary
    val faint = scheme.onSlab.copy(alpha = 0.2f)
    val deep = scheme.slab

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var selectedHeatmapDay by remember { mutableStateOf<CalendarDayHeatmap?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(deep),
        contentPadding = PaddingValues(top = 44.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Audit",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color = dim
                )
                Text(
                    text = "Reports",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = ink
                )
            }
        }

        item {
            LedgerSegmented(
                options = listOf("WEEK", "MONTH", "QTR", "YEAR", "ALL"),
                selected = periods.indexOf(selectedPeriod).coerceAtLeast(0),
                onSelect = { viewModel.selectedPeriod.value = periods[it] },
                modifier = Modifier.padding(horizontal = 16.dp),
                ink = ink,
                paper = deep
            )
        }

        // ── Accumulated outflow hero ──────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                MonoLabel(text = "Accumulated outflow", color = dim)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LedgerAmount(
                        text = FormatUtils.formatLedger(totalExpense),
                        fontSize = 40.sp,
                        color = ink,
                        dimmedColor = dim
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    LedgerPill(
                        text = "IN ${FormatUtils.formatLedger(totalIncome)}",
                        container = ink,
                        content = deep
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        MonoLabel(text = "Personal", color = dim)
                        LedgerAmount(
                            text = FormatUtils.formatLedger(personalSpend),
                            fontSize = 17.sp,
                            color = ink,
                            dimmedColor = dim
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        MonoLabel(text = "Household", color = dim)
                        LedgerAmount(
                            text = FormatUtils.formatLedger(householdSpend),
                            fontSize = 17.sp,
                            color = ink,
                            dimmedColor = dim
                        )
                    }
                }
            }
        }

        item { Hairline(color = faint) }

        item {
            LedgerSegmented(
                options = listOf("Category", "Trend", "Heatmap"),
                selected = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.padding(horizontal = 16.dp),
                ink = ink,
                paper = deep
            )
        }

        when (selectedTab) {
            0 -> {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ScopeRow(
                            label = "Personal",
                            amount = personalSpend,
                            dot = ink,
                            ink = ink,
                            dim = dim
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScopeRow(
                            label = "Household",
                            amount = householdSpend,
                            dot = ink.copy(alpha = 0.45f),
                            ink = ink,
                            dim = dim
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val total = (personalSpend + householdSpend).coerceAtLeast(1.0)
                        val personalFraction =
                            (personalSpend / total).toFloat().coerceIn(0f, 1f)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = personalFraction.coerceAtLeast(0.01f))
                                    .height(10.dp)
                                    .background(ink)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .background(faint)
                            )
                        }
                    }
                }

                if (categoryReports.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MonoLabel(
                                text = "Spending by category",
                                color = dim,
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
                                    ring = ink,
                                    faint = faint
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "SPENT",
                                        fontFamily = SpaceGrotesk,
                                        fontSize = 11.sp,
                                        color = dim
                                    )
                                    IosAmount(
                                        text = FormatUtils.formatLedger(totalExpense),
                                        color = ink,
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
                                val palette = monoPalette(ink)
                                visible.forEachIndexed { index, catReport ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(palette[index % palette.size])
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = catReport.category.name,
                                            modifier = Modifier.weight(1f),
                                            fontFamily = SpaceGrotesk,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = ink,
                                            maxLines = 1
                                        )
                                        IosAmount(
                                            text = FormatUtils.formatLedger(catReport.amount),
                                            color = ink,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${(catReport.percentage * 100).toInt()}%",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 13.sp,
                                            color = dim
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
                                            color = ink
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
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MonoLabel(text = "Settle up", color = dim)
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
                                            color = ink
                                        )
                                        Text(
                                            text = "Paid ${maskedCurrency(balance.totalPaid)} · Share ${maskedCurrency(balance.fairShare)}",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 13.sp,
                                            color = dim
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        IosAmount(
                                            text = (if (isOwed) "+" else "−") +
                                                FormatUtils.formatLedger(
                                                    if (isOwed) balance.netBalance else -balance.netBalance
                                                ),
                                            color = ink,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = if (isOwed) "Gets back" else "Owes",
                                            fontFamily = SpaceGrotesk,
                                            fontSize = 11.sp,
                                            color = dim
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
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MonoLabel(text = "Daily spend", color = dim)
                        Spacer(modifier = Modifier.height(10.dp))
                        IosTrendBars(days = dailyTrend, ink = ink, dim = dim, faint = faint)
                    }
                }
            }
            else -> {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MonoLabel(text = "Spend calendar", color = dim)
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
                                    color = dim,
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
                                    0 -> ink.copy(alpha = 0.08f)
                                    1 -> ink.copy(alpha = 0.30f)
                                    2 -> ink.copy(alpha = 0.55f)
                                    3 -> ink.copy(alpha = 0.80f)
                                    else -> ink
                                }
                                val strongText = day.intensityLevel >= 3
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(cellColor)
                                        .clickable { selectedHeatmapDay = day },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${day.dayNumber}",
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = if (day.intensityLevel > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = if (strongText) deep else ink
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
                                    .background(ink.copy(alpha = 0.10f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Day ${d.dayNumber} · ${maskedCurrency(d.spendAmount)}",
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = ink
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
private fun ScopeRow(
    label: String,
    amount: Double,
    dot: Color,
    ink: Color,
    dim: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(dot)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = ink
            )
        }
        LedgerAmount(
            text = FormatUtils.formatLedger(amount),
            fontSize = 15.sp,
            color = ink,
            dimmedColor = dim
        )
    }
}

// Monochrome paper-step palette: rank reads as brightness, not hue.
private fun monoPalette(ink: Color): List<Color> = listOf(
    ink,
    ink.copy(alpha = 0.65f),
    ink.copy(alpha = 0.40f),
    ink.copy(alpha = 0.25f)
)

@Composable
fun IosDonutChart(
    categories: List<CategorySpendReport>,
    totalSpend: Double,
    ring: Color,
    faint: Color
) {
    val colors = monoPalette(ring)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 18.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        if (totalSpend <= 0) {
            drawCircle(
                color = faint,
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

/** Paper bars with a top-3 peak list — peak reads as full height, not hue. */
@Composable
private fun IosTrendBars(days: List<DailySpendPoint>, ink: Color, dim: Color, faint: Color) {
    if (days.isEmpty() || days.all { it.amount <= 0 }) {
        Text(
            text = "No expenses in the selected period.",
            fontFamily = SpaceGrotesk,
            fontSize = 15.sp,
            color = dim,
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
                    .background(ink)
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    val top = days.sortedByDescending { it.amount }.take(3)
    Text(
        text = "Highest days",
        fontFamily = SpaceGrotesk,
        fontSize = 13.sp,
        color = dim
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
                color = dim,
                modifier = Modifier.width(56.dp)
            )
            IosAmount(
                text = FormatUtils.formatLedger(day.amount),
                color = ink,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            SliverBar(
                fraction = (day.amount / maxAmount).toFloat(),
                ink = ink,
                faint = faint,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Hairline meter: faint track, paper fill. Replaces the red/green bar. */
@Composable
private fun SliverBar(
    fraction: Float,
    ink: Color,
    faint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(faint)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .background(ink)
        )
    }
}
