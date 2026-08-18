package com.example.moneymanager.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.theme.*
import com.example.moneymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val period by viewModel.selectedPeriod.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val personalExpense by viewModel.personalExpense.collectAsState()
    val householdExpense by viewModel.householdExpense.collectAsState()
    val categoryReports by viewModel.categoryReports.collectAsState()
    val paymentModeReports by viewModel.paymentModeReports.collectAsState()
    val dailyTrend by viewModel.dailySpendTrend.collectAsState()
    val heatmapDays by viewModel.monthlyCalendarHeatmap.collectAsState()
    val settlements by viewModel.householdSettlements.collectAsState()

    var selectedHeatmapDay by remember { mutableStateOf<CalendarDayHeatmap?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {
            // Period Selector Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = period == ReportPeriod.THIS_MONTH,
                        onClick = {
                            viewModel.selectedPeriod.value = ReportPeriod.THIS_MONTH
                            selectedHeatmapDay = null
                        },
                        label = { Text("This Month") }
                    )
                    FilterChip(
                        selected = period == ReportPeriod.LAST_MONTH,
                        onClick = {
                            viewModel.selectedPeriod.value = ReportPeriod.LAST_MONTH
                            selectedHeatmapDay = null
                        },
                        label = { Text("Last Month") }
                    )
                    FilterChip(
                        selected = period == ReportPeriod.ALL_TIME,
                        onClick = {
                            viewModel.selectedPeriod.value = ReportPeriod.ALL_TIME
                            selectedHeatmapDay = null
                        },
                        label = { Text("All Time") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Summary Totals Card
            item {
                ReportSummaryHeroCard(
                    totalExpense = totalExpense,
                    totalIncome = totalIncome
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Personal vs Household Split
            item {
                PersonalHouseholdReportCard(
                    personal = personalExpense,
                    household = householdExpense,
                    total = totalExpense
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Household Split & Settlement Breakdown
            if (settlements.isNotEmpty()) {
                item {
                    Text(
                        text = "Household Settle Up & Splits",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    HouseholdSettlementCard(settlements = settlements, totalHousehold = householdExpense)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Calendar Spend Heatmap (for monthly views)
            if (period != ReportPeriod.ALL_TIME && heatmapDays.isNotEmpty()) {
                item {
                    Text(
                        text = "Spend Calendar Heatmap",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    CalendarHeatmapCard(
                        days = heatmapDays,
                        selectedDay = selectedHeatmapDay,
                        onDayClick = { selectedHeatmapDay = it }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Category Breakdown Chart & Ranking
            if (categoryReports.isNotEmpty()) {
                item {
                    Text(
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DonutChartCard(
                        categories = categoryReports,
                        totalExpense = totalExpense
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(categoryReports, key = { it.category.id }) { report ->
                    CategoryReportRow(report = report)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Payment Mode Breakdown
            if (paymentModeReports.any { it.amount > 0 }) {
                item {
                    Text(
                        text = "Payment Modes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PaymentModeRow(reports = paymentModeReports)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Daily Spend Trend Bar Chart
            if (dailyTrend.any { it.amount > 0 }) {
                item {
                    Text(
                        text = "Daily Spend Trend",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DailyTrendChartCard(points = dailyTrend)
                }
            }
        }
    }
}

@Composable
fun ReportSummaryHeroCard(totalExpense: Double, totalIncome: Double) {
    val net = totalIncome - totalExpense

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "TOTAL SPENT",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = FormatUtils.formatCurrency(totalExpense),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = ExpenseRed
            )

            if (totalIncome > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatCurrency(totalIncome), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = IncomeGreen)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Net Savings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = (if (net >= 0) "+ " else "") + FormatUtils.formatCurrency(net),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (net >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalHouseholdReportCard(personal: Double, household: Double, total: Double) {
    val personalPct = if (total > 0) (personal / total).toFloat() else 0f
    val householdPct = if (total > 0) (household / total).toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Personal vs Household Spend",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Two-segment bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
            ) {
                if (personalPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(personalPct.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(PersonalBlue)
                    )
                }
                if (householdPct > 0) {
                    Box(
                        modifier = Modifier
                            .weight(householdPct.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(HouseholdOrange)
                    )
                }
                if (total <= 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Personal Label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(PersonalBlue))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Personal (${(personalPct * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatCurrency(personal), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                // Household Label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(HouseholdOrange))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Household (${(householdPct * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormatUtils.formatCurrency(household), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun HouseholdSettlementCard(
    settlements: List<MemberSettlement>,
    totalHousehold: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Household: ${FormatUtils.formatCurrency(totalHousehold)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val fairShare = settlements.firstOrNull()?.fairShare ?: 0.0
                Text(
                    text = "Share/Person: ${FormatUtils.formatCurrency(fairShare)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = HouseholdOrange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            settlements.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = HouseholdOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = member.memberName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Paid ${FormatUtils.formatCurrency(member.totalPaid)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val isPositive = member.netBalance > 0.01
                        val isNegative = member.netBalance < -0.01

                        Text(
                            text = when {
                                isPositive -> "+ ${FormatUtils.formatCurrency(member.netBalance)}"
                                isNegative -> "- ${FormatUtils.formatCurrency(-member.netBalance)}"
                                else -> "Settled"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                isPositive -> IncomeGreen
                                isNegative -> ExpenseRed
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = when {
                                isPositive -> "gets back"
                                isNegative -> "owes"
                                else -> "all clear"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeatmapCard(
    days: List<CalendarDayHeatmap>,
    selectedDay: CalendarDayHeatmap?,
    onDayClick: (CalendarDayHeatmap) -> Unit
) {
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
    val firstDayOffset = if (days.isNotEmpty()) days.first().dayOfWeek - 1 else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Days of week header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekDays.forEach { w ->
                    Text(
                        text = w,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            val totalCells = firstDayOffset + days.size
            val numRows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until numRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayIndex = cellIndex - firstDayOffset

                            if (cellIndex < firstDayOffset || dayIndex >= days.size) {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                val day = days[dayIndex]
                                val isSelected = selectedDay?.dayNumber == day.dayNumber
                                val bgColor = when (day.intensityLevel) {
                                    0 -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    1 -> MintGreen.copy(alpha = 0.25f)
                                    2 -> MintGreen.copy(alpha = 0.55f)
                                    3 -> AmberGold.copy(alpha = 0.70f)
                                    else -> ExpenseRed.copy(alpha = 0.85f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bgColor)
                                        .clickable { onDayClick(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${day.dayNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        color = if (day.intensityLevel >= 3) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Info Footer
            if (selectedDay != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Day ${selectedDay.dayNumber} Spend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatCurrency(selectedDay.spendAmount),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedDay.spendAmount > 0) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChartCard(
    categories: List<CategorySpendReport>,
    totalExpense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val strokeWidth = 32.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    for (report in categories) {
                        val sweepAngle = report.percentage * 360f
                        if (sweepAngle > 0f) {
                            drawArc(
                                color = Color(report.category.color),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                            startAngle += sweepAngle
                        }
                    }
                }

                // Center Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Spend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatCurrency(totalExpense),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryReportRow(report: CategorySpendReport) {
    val catColor = Color(report.category.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FormatUtils.getCategoryIcon(report.category.icon),
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.category.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${report.transactionCount} spend(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FormatUtils.formatCurrency(report.amount),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(report.percentage * 100).toInt()}% of total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { report.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = catColor,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun PaymentModeRow(reports: List<PaymentModeSpendReport>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reports.forEach { report ->
            val color = when (report.mode) {
                PaymentMode.UPI -> MaterialTheme.colorScheme.primary
                PaymentMode.CASH -> AmberGold
                PaymentMode.CARD -> TealAccent
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = report.mode.name,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = FormatUtils.formatCurrency(report.amount),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${(report.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTrendChartCard(points: List<DailySpendPoint>) {
    val maxSpend = (points.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Peak day spend: ${FormatUtils.formatCurrency(maxSpend)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val barWidth = size.width / points.size
                val heightRatio = size.height / maxSpend.toFloat()

                points.forEachIndexed { index, point ->
                    val barHeight = (point.amount * heightRatio).toFloat()
                    val x = index * barWidth
                    val y = size.height - barHeight

                    if (point.amount > 0) {
                        drawRoundRect(
                            color = MintGreen,
                            topLeft = Offset(x + barWidth * 0.15f, y),
                            size = Size(barWidth * 0.7f, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}
