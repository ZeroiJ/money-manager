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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
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
                title = {
                    Text(
                        text = "REPORTS & ANALYTICS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportPeriod.values().forEach { p ->
                        val isSelected = period == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .neoShadow(offset = if (isSelected) 3.dp else 1.dp, cornerRadius = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeoYellow else MaterialTheme.colorScheme.surface)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .clickable { viewModel.selectedPeriod.value = p }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (p) {
                                    ReportPeriod.THIS_MONTH -> "THIS MONTH"
                                    ReportPeriod.LAST_MONTH -> "LAST MONTH"
                                    ReportPeriod.ALL_TIME -> "ALL TIME"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                ),
                                color = if (isSelected) NeoBlack else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // High-Voltage KPI Cards (Expense & Income)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Expense Card
                    NeoCard(
                        modifier = Modifier.weight(1f),
                        shadowOffset = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            NeoBadge(text = "TOTAL SPENT", backgroundColor = NeoRed, textColor = NeoWhite)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalExpense),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                        }
                    }

                    // Total Income / Balance Card
                    NeoCard(
                        modifier = Modifier.weight(1f),
                        shadowOffset = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            NeoBadge(text = "TOTAL INCOME", backgroundColor = NeoGreen, textColor = NeoWhite)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = FormatUtils.formatCurrency(totalIncome),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                        }
                    }
                }
            }

            // Scope Comparison (Personal vs Household)
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowOffset = 3.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SCOPE BREAKDOWN",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalScope = personalExpense + householdExpense
                        val personalPct = if (totalScope > 0) (personalExpense / totalScope).toFloat() else 0.5f

                        // Segmented bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeoGray200)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(if (personalPct > 0f) personalPct else 0.001f)
                                        .background(NeoBlue)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(if (1f - personalPct > 0f) (1f - personalPct) else 0.001f)
                                        .background(NeoOrange)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(NeoBlue)
                                        .border(1.dp, NeoBlack)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PERSONAL: ${FormatUtils.formatCurrency(personalExpense)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(NeoOrange)
                                        .border(1.dp, NeoBlack)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HOUSEHOLD: ${FormatUtils.formatCurrency(householdExpense)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Monthly Spend Calendar Heatmap Section
            if (heatmapDays.isNotEmpty()) {
                item {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowOffset = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MONTHLY SPEND HEATMAP",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                                )
                                Text(
                                    text = "LEVELS 0-4",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Day of week headers (M, T, W, T, F, S, S)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                                    Text(
                                        text = d,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // 7-column Calendar Heatmap Grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                modifier = Modifier.height(200.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(heatmapDays) { day ->
                                    val cellBg = when (day.intensityLevel) {
                                        4 -> NeoRed
                                        3 -> NeoOrange
                                        2 -> NeoYellow
                                        1 -> NeoLime
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                    val isSelected = selectedHeatmapDay == day

                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(cellBg)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable { selectedHeatmapDay = day },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day.dayNumber.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = if (day.intensityLevel > 0) FontWeight.Black else FontWeight.Normal
                                            ),
                                            color = if (day.intensityLevel >= 3) NeoWhite else NeoBlack
                                        )
                                    }
                                }
                            }

                            // Selected day detail view
                            if (selectedHeatmapDay != null) {
                                val day = selectedHeatmapDay!!
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeoGray100)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "DAY ${day.dayNumber} SPEND:",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                            color = NeoBlack
                                        )
                                        Text(
                                            text = FormatUtils.formatCurrency(day.spendAmount),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = if (day.spendAmount > 0) NeoRed else NeoBlack
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Household Settle Up Card
            if (settlements.isNotEmpty()) {
                item {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowOffset = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HOUSEHOLD SETTLE UP",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                                )
                                NeoBadge(text = "SPLIT", backgroundColor = NeoOrange, textColor = NeoWhite)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            settlements.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = member.memberName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                                        )
                                        Text(
                                            text = "Paid: ${FormatUtils.formatCurrency(member.totalPaid)} | Share: ${FormatUtils.formatCurrency(member.fairShare)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    val isOwed = member.netBalance >= 0
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isOwed) NeoGreen.copy(alpha = 0.2f) else NeoRed.copy(alpha = 0.2f))
                                            .border(1.5.dp, if (isOwed) NeoGreen else NeoRed, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isOwed) "+${FormatUtils.formatCurrency(member.netBalance)} (Gets back)"
                                            else "${FormatUtils.formatCurrency(member.netBalance)} (Owes)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = if (isOwed) NeoGreen else NeoRed
                                            )
                                        )
                                    }
                                }
                                HorizontalDivider(thickness = 1.dp, color = NeoGray200)
                            }
                        }
                    }
                }
            }

            // Category Spend Donut & Breakdown
            if (categoryReports.isNotEmpty()) {
                item {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowOffset = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CATEGORY BREAKDOWN",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Donut Chart Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(170.dp)) {
                                    val strokeWidth = 32.dp.toPx()
                                    var startAngle = -90f
                                    val total = totalExpense.toFloat()

                                    if (total > 0) {
                                        categoryReports.forEach { rep ->
                                            val sweep = (rep.amount.toFloat() / total) * 360f
                                            drawArc(
                                                color = Color(rep.category.color.toInt()),
                                                startAngle = startAngle,
                                                sweepAngle = sweep,
                                                useCenter = false,
                                                style = Stroke(width = strokeWidth)
                                            )
                                            startAngle += sweep
                                        }
                                    } else {
                                        drawArc(
                                            color = Color.LightGray,
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "SPENT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = FormatUtils.formatCurrency(totalExpense),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Category List
                            categoryReports.forEach { rep ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Color(rep.category.color.toInt()))
                                                .border(1.dp, NeoBlack)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = rep.category.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = FormatUtils.formatCurrency(rep.amount),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        NeoBadge(
                                            text = "${rep.percentage.toInt()}%",
                                            backgroundColor = NeoYellow,
                                            textColor = NeoBlack
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Payment Mode Breakdown
            if (paymentModeReports.isNotEmpty()) {
                item {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowOffset = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PAYMENT MODES",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                paymentModeReports.forEach { modeRep ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeoGray100)
                                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = modeRep.mode.name,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                                color = NeoBlack
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = FormatUtils.formatCurrency(modeRep.amount),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 11.sp
                                                ),
                                                color = NeoBlack
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
}
