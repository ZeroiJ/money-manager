package com.example.moneymanager.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ascii.Ascii
import com.example.moneymanager.ui.ascii.AsciiEmptyState
import com.example.moneymanager.ui.ascii.AsciiSelectChip
import com.example.moneymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
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

    // UI-local tab state: CATEGORY | TREND | HEATMAP
    var selectedTab by rememberSaveable { mutableStateOf("CATEGORY") }
    var selectedHeatmapDay by remember { mutableStateOf<CalendarDayHeatmap?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Inline header
            item {
                Text(
                    text = "analytics // reports",
                    style = Chroma.type.titleMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Period Switcher — flat chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsciiSelectChip(
                        label = "THIS MONTH",
                        selected = selectedPeriod == ReportPeriod.THIS_MONTH,
                        onClick = { viewModel.selectedPeriod.value = ReportPeriod.THIS_MONTH },
                        modifier = Modifier.weight(1f)
                    )
                    AsciiSelectChip(
                        label = "LAST MONTH",
                        selected = selectedPeriod == ReportPeriod.LAST_MONTH,
                        onClick = { viewModel.selectedPeriod.value = ReportPeriod.LAST_MONTH },
                        modifier = Modifier.weight(1f)
                    )
                    AsciiSelectChip(
                        label = "ALL TIME",
                        selected = selectedPeriod == ReportPeriod.ALL_TIME,
                        onClick = { viewModel.selectedPeriod.value = ReportPeriod.ALL_TIME },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // High-Impact Spend vs Income Overview — flat boxes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Expense
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Chroma.color.surface)
                            .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "outflow.log",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Chroma.color.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ChromaBadge(text = "EXPENSE", backgroundColor = ChromaRed, textColor = ChromaWhite)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = FormatUtils.formatCurrency(totalExpense),
                            style = Chroma.type.titleMedium.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Black
                            ),
                            color = ChromaRed
                        )
                        Text(
                            text = "Total Outflow",
                            style = Chroma.type.labelSmall.copy(fontSize = 10.sp),
                            color = Chroma.color.onSurfaceVariant
                        )
                    }

                    // Total Income
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Chroma.color.surface)
                            .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "inflow.log",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Chroma.color.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ChromaBadge(text = "INCOME", backgroundColor = ChromaGreen, textColor = ChromaWhite)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = FormatUtils.formatCurrency(totalIncome),
                            style = Chroma.type.titleMedium.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Black
                            ),
                            color = ChromaGreen
                        )
                        Text(
                            text = "Total Inflow",
                            style = Chroma.type.labelSmall.copy(fontSize = 10.sp),
                            color = Chroma.color.onSurfaceVariant
                        )
                    }
                }
            }

            // Tab Switcher — flat chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CATEGORY" to "CATEGORY", "TREND" to "TREND", "HEATMAP" to "HEATMAP").forEach { (key, label) ->
                        AsciiSelectChip(
                            label = label,
                            selected = selectedTab == key,
                            onClick = { selectedTab = key },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            when (selectedTab) {
                "CATEGORY" -> {
                    // Scope Comparison — flat box
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Chroma.color.surface)
                                .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PERSONAL: ${FormatUtils.formatCurrency(personalSpend)}",
                                    style = Chroma.type.labelSmall.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold,
                                        color = ChromaBlue
                                    )
                                )
                                Text(
                                    text = "HOUSEHOLD: ${FormatUtils.formatCurrency(householdSpend)}",
                                    style = Chroma.type.labelSmall.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold,
                                        color = ChromaOrange
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val total = (personalSpend + householdSpend).coerceAtLeast(1.0)
                            val personalFraction = (personalSpend / total).toFloat().coerceIn(0f, 1f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ChromaStone200)
                                    .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                            ) {
                                if (personalSpend > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(personalFraction.coerceAtLeast(0.01f))
                                            .background(ChromaBlue)
                                    )
                                }
                                if (householdSpend > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight((1f - personalFraction).coerceAtLeast(0.01f))
                                            .background(ChromaOrange)
                                    )
                                }
                            }
                        }
                    }

                    // Category Spend Donut + top-5
                    if (categoryReports.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Chroma.color.surface)
                                    .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "category_distribution.chart",
                                    style = Chroma.type.labelSmall.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Chroma.color.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ChromaDonutChartCanvas(
                                        categories = categoryReports,
                                        totalSpend = totalExpense
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "SPENT",
                                            style = Chroma.type.labelSmall.copy(
                                                fontFamily = PlexMono,
                                                fontSize = 9.sp
                                            ),
                                            color = Chroma.color.onSurfaceVariant
                                        )
                                        Text(
                                            text = FormatUtils.formatCurrency(totalExpense),
                                            style = Chroma.type.labelMedium.copy(
                                                fontFamily = PlexMono,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Top-5 with SHOW ALL toggle
                                var showAll by remember { mutableStateOf(false) }
                                val visible = if (showAll) categoryReports else categoryReports.take(5)

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    visible.forEach { catReport ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(ChromaBlack)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = catReport.category.name,
                                                modifier = Modifier.weight(1f),
                                                style = Chroma.type.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = FormatUtils.formatCurrency(catReport.amount),
                                                style = Chroma.type.labelSmall.copy(
                                                    fontFamily = PlexMono,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${(catReport.percentage * 100).toInt()}%)",
                                                style = Chroma.type.labelSmall.copy(
                                                    fontFamily = PlexMono,
                                                    fontSize = 10.sp
                                                ),
                                                color = Chroma.color.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (categoryReports.size > 5) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        AsciiSelectChip(
                                            label = if (showAll) "SHOW TOP 5" else "SHOW ALL (${categoryReports.size} CATEGORIES)",
                                            selected = showAll,
                                            onClick = { showAll = !showAll },
                                            modifier = Modifier.align(Alignment.CenterHorizontally),
                                            selectedColor = ChromaStone200,
                                            selectedTextColor = ChromaBlack,
                                            borderColor = Ascii.hairline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Household Settle-Up Ledger
                    if (settlements.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Chroma.color.surface)
                                    .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "settle_up.ledger // splits",
                                    style = Chroma.type.labelSmall.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Chroma.color.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                settlements.forEach { balance ->
                                    val isOwed = balance.netBalance > 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(ChromaStone100)
                                            .border(0.5.dp, ChromaStone300, RoundedCornerShape(2.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = balance.memberName,
                                                style = Chroma.type.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Paid: ${FormatUtils.formatCurrency(balance.totalPaid)} | Fair Share: ${FormatUtils.formatCurrency(balance.fairShare)}",
                                                style = Chroma.type.labelSmall.copy(
                                                    fontFamily = PlexMono,
                                                    fontSize = 10.sp
                                                ),
                                                color = Chroma.color.onSurfaceVariant
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (isOwed) "+ ${FormatUtils.formatCurrency(balance.netBalance)}"
                                                else "- ${FormatUtils.formatCurrency(-balance.netBalance)}",
                                                style = Chroma.type.bodyMedium.copy(
                                                    fontFamily = PlexMono,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isOwed) ChromaGreen else ChromaRed
                                                )
                                            )
                                            Text(
                                                text = if (isOwed) "GETS BACK" else "OWES",
                                                style = Chroma.type.labelSmall.copy(
                                                    fontFamily = PlexMono,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = if (isOwed) ChromaGreen else ChromaRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "TREND" -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Chroma.color.surface)
                                .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "daily_spend.trend // flame",
                                style = Chroma.type.labelSmall.copy(
                                    fontFamily = PlexMono,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Chroma.color.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            AsciiFlameGraph(days = dailyTrend)
                        }
                    }
                }
                else -> { // HEATMAP
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Chroma.color.surface)
                                .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "spend_intensity.matrix // monthly",
                                style = Chroma.type.labelSmall.copy(
                                    fontFamily = PlexMono,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Chroma.color.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Day of week headers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLabel ->
                                    Text(
                                        text = dayLabel,
                                        style = Chroma.type.labelSmall.copy(
                                            fontFamily = PlexMono,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = Chroma.color.onSurfaceVariant,
                                        modifier = Modifier.width(36.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid of days
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                modifier = Modifier.height(180.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(heatmapDays) { day ->
                                    val cellColor = when (day.intensityLevel) {
                                        0 -> ChromaStone100
                                        1 -> ChromaYellow.copy(alpha = 0.4f)
                                        2 -> ChromaYellow
                                        3 -> ChromaOrange
                                        else -> ChromaRed
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(cellColor)
                                            .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                                            .clickable { selectedHeatmapDay = day },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${day.dayNumber}",
                                            style = Chroma.type.labelSmall.copy(
                                                fontFamily = PlexMono,
                                                fontWeight = if (day.intensityLevel > 0) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 9.sp
                                            ),
                                            color = if (day.intensityLevel >= 3) ChromaWhite else ChromaBlack
                                        )
                                    }
                                }
                            }

                            // Selected Heatmap Day Details
                            if (selectedHeatmapDay != null) {
                                val d = selectedHeatmapDay!!
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(ChromaStone200)
                                        .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "DAY ${d.dayNumber}: SPEND = ${FormatUtils.formatCurrency(d.spendAmount)}",
                                        style = Chroma.type.labelSmall.copy(
                                            fontFamily = PlexMono,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pure-text flame-graph of daily spend. Each day is a column of block chars
 * scaled to the max day (0→▁ … max→█), horizontally scrollable. Below it, the
 * top-3 highest-spend days with a mood-bar percentage. No Canvas.
 */
@Composable
private fun AsciiFlameGraph(days: List<DailySpendPoint>) {
    if (days.isEmpty() || days.all { it.amount <= 0 }) {
        AsciiEmptyState(
            title = "NO SPEND DATA",
            subtitle = "No expenses in the selected period."
        )
        return
    }

    val maxAmount = days.maxOf { it.amount }.coerceAtLeast(1.0)
    val levels = "▁▂▃▄▅▆▇█"
    val style = Chroma.type.labelSmall.copy(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Bold
    )

    // Flame row — each day one column of block glyphs
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.Bottom
    ) {
        // Left padding gutter for day numbers (up to 2 chars)
        Spacer(modifier = Modifier.width(14.dp))
        days.forEach { day ->
            Column(
                modifier = Modifier.width(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val ratio = (day.amount / maxAmount).toFloat().coerceIn(0f, 1f)
                val levelIndex = ((ratio * (levels.length - 1)).toInt()).coerceIn(1, levels.length - 1)
                Text(
                    text = "${levels[levelIndex]}",
                    style = style.copy(
                        fontSize = 13.sp,
                        lineHeight = 13.sp,
                        color = if (ratio >= 0.98f) ChromaOrange else ChromaGreen
                    )
                )
                Text(
                    text = day.dayNumber.toString().padStart(2, '0'),
                    style = style.copy(fontSize = 7.sp, color = Chroma.color.onSurfaceVariant)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Top-3 highest spend days
    val top = days.sortedByDescending { it.amount }.take(3)
    Text(
        text = "PEAKS // TOP ${top.size}",
        style = style.copy(fontSize = 9.sp, color = Chroma.color.onSurfaceVariant)
    )
    Spacer(modifier = Modifier.height(6.dp))
    top.forEach { day ->
        val pct = (day.amount / maxAmount * 100f).toInt()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DAY ${day.dayNumber.toString().padStart(2, '0')}",
                style = style.copy(fontSize = 9.sp, color = Chroma.color.onSurfaceVariant),
                modifier = Modifier.width(52.dp)
            )
            Text(
                text = FormatUtils.formatCurrency(day.amount),
                style = style.copy(fontSize = 10.sp),
                modifier = Modifier.width(88.dp)
            )
            Text(
                text = "█".repeat((pct / 10).coerceIn(1, 10)),
                style = style.copy(color = if (pct >= 90) ChromaOrange else ChromaGreen)
            )
        }
    }
}

@Composable
fun ChromaDonutChartCanvas(
    categories: List<CategorySpendReport>,
    totalSpend: Double
) {
    val colors = listOf(
        ChromaOrange, ChromaBlue, ChromaGreen, ChromaPurple,
        ChromaCyan, ChromaYellow, ChromaRed, ChromaStone600
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 18.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        if (totalSpend <= 0) {
            drawCircle(
                color = ChromaStone200,
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