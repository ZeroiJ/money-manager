package com.example.moneymanager.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.theme.*
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
    val paymentModeReports by viewModel.paymentModeReports.collectAsState()
    val heatmapDays by viewModel.monthlyCalendarHeatmap.collectAsState()
    val settlements by viewModel.householdSettlements.collectAsState()

    var selectedHeatmapDay by remember { mutableStateOf<CalendarDayHeatmap?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Inline header
        item {
            Text(
                text = "analytics // reports",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Period Switcher (THIS_MONTH, LAST_MONTH, ALL_TIME)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ReportPeriod.THIS_MONTH to "THIS MONTH",
                        ReportPeriod.LAST_MONTH to "LAST MONTH",
                        ReportPeriod.ALL_TIME to "ALL TIME"
                    ).forEach { (period, label) ->
                        val isSelected = selectedPeriod == period
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .chromaShadow(offset = if (isSelected) 2.dp else 1.dp, cornerRadius = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) ChromaBlack else MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { viewModel.selectedPeriod.value = period }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "[ $label ]",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) ChromaWhite else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // High-Impact Spend vs Income Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Expense Card
                    ChromaCard(
                        modifier = Modifier.weight(1f),
                        windowTitle = "outflow.log",
                        shadowOffset = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            ChromaBadge(text = "EXPENSE", backgroundColor = ChromaRed, textColor = ChromaWhite)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalExpense),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                ),
                                color = ChromaRed
                            )
                            Text(
                                text = "Total Outflow",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Total Income Card
                    ChromaCard(
                        modifier = Modifier.weight(1f),
                        windowTitle = "inflow.log",
                        shadowOffset = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            ChromaBadge(text = "INCOME", backgroundColor = ChromaGreen, textColor = ChromaWhite)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalIncome),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                ),
                                color = ChromaGreen
                            )
                            Text(
                                text = "Total Inflow",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Scope Comparison Progress
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "scope_ratio // personal_vs_household",
                    shadowOffset = 2.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "PERSONAL: ${FormatUtils.formatCurrency(personalSpend)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ChromaBlue
                                )
                            )
                            Text(
                                text = "HOUSEHOLD: ${FormatUtils.formatCurrency(householdSpend)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
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
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
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
            }

            // 7-Column Spend Heatmap Calendar Matrix
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "spend_intensity.matrix // monthly",
                    statusIndicator = "HEATMAP",
                    shadowOffset = 3.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Day of week headers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
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
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Household Settle-Up Ledger Card
            if (settlements.isNotEmpty()) {
                item {
                    ChromaCard(
                        modifier = Modifier.fillMaxWidth(),
                        windowTitle = "settle_up.ledger // splits",
                        shadowOffset = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Paid: ${FormatUtils.formatCurrency(balance.totalPaid)} | Fair Share: ${FormatUtils.formatCurrency(balance.fairShare)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isOwed) "+ ${FormatUtils.formatCurrency(balance.netBalance)}"
                                            else "- ${FormatUtils.formatCurrency(-balance.netBalance)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOwed) ChromaGreen else ChromaRed
                                            )
                                        )
                                        Text(
                                            text = if (isOwed) "GETS BACK" else "OWES",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
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

            // Category Spend Donut Chart
            if (categoryReports.isNotEmpty()) {
                item {
                    ChromaCard(
                        modifier = Modifier.fillMaxWidth(),
                        windowTitle = "category_distribution.chart",
                        shadowOffset = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = FormatUtils.formatCurrency(totalExpense),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Category Breakdown List
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categoryReports.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(ChromaBlack)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.category.name,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = FormatUtils.formatCurrency(item.amount),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Text(
                                                text = "(${(item.percentage * 100).toInt()}%)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
