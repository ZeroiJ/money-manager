package com.example.moneymanager.ui.screens.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ascii.Ascii
import com.example.moneymanager.ui.ascii.AsciiEmptyState
import com.example.moneymanager.ui.ascii.AsciiMoodBar
import com.example.moneymanager.ui.ascii.AsciiSectionHeader
import com.example.moneymanager.ui.ascii.AsciiSelectChip
import com.example.moneymanager.util.FormatUtils
import com.example.moneymanager.util.HapticOnCrossing
import com.example.moneymanager.util.Haptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val currentMonth by viewModel.selectedMonth.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val budgetProgressList by viewModel.budgetProgressList.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf<Category?>(null) }
    var budgetAmountInput by remember { mutableStateOf("") }

    // Active month is the app's current month. Past months are only reachable
    // behind an explicit PAST MODE toggle so the budgets screen defaults to the
    // live month every time.
    val activeMonthKey = FormatUtils.getCurrentMonthKey()
    var allowPastMonths by rememberSaveable { mutableStateOf(false) }
    val onActiveMonth = currentMonth == activeMonthKey
    val inPast = !onActiveMonth && currentMonth < activeMonthKey

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Inline header
        item {
            Text(
                text = "budgets // limits",
                style = Chroma.type.titleMedium.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Month Selector Bar — flat
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Chroma.color.surface)
                    .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                    .padding(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prev month — disabled on the active month unless past-mode is on
                    val prevEnabled = !onActiveMonth || allowPastMonths
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Chroma.color.surface)
                            .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                            .then(
                                if (prevEnabled) Modifier.clickable { viewModel.navigateMonth(-1) }
                                else Modifier.alpha(0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            modifier = Modifier.size(16.dp),
                            tint = if (prevEnabled) Chroma.color.onSurface else Chroma.color.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = FormatUtils.formatMonth(currentMonth).uppercase(),
                            style = Chroma.type.titleSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (onActiveMonth) {
                            Text(
                                text = "[ ACTIVE ]",
                                style = Chroma.type.labelSmall.copy(
                                    fontFamily = PlexMono,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ChromaGreen
                            )
                        } else {
                            Text(
                                text = if (inPast) "[ PAST ]" else "[ FUTURE ]",
                                style = Chroma.type.labelSmall.copy(
                                    fontFamily = PlexMono,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (inPast) ChromaOrange else Chroma.color.onSurfaceVariant
                            )
                        }
                    }

                    // Next month — never beyond the active month
                    val nextEnabled = !onActiveMonth
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Chroma.color.surface)
                            .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                            .then(
                                if (nextEnabled) Modifier.clickable { viewModel.navigateMonth(1) }
                                else Modifier.alpha(0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            modifier = Modifier.size(16.dp),
                            tint = if (nextEnabled) Chroma.color.onSurface else Chroma.color.onSurfaceVariant
                        )
                    }
                }

                // PAST MODE toggle
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsciiSelectChip(
                        label = "HIDE PAST",
                        selected = !allowPastMonths,
                        onClick = { allowPastMonths = false },
                        modifier = Modifier.weight(1f),
                        selectedColor = ChromaBlack
                    )
                    AsciiSelectChip(
                        label = "PAST MODE",
                        selected = allowPastMonths,
                        onClick = { allowPastMonths = true },
                        modifier = Modifier.weight(1f),
                        selectedColor = ChromaBlue
                    )
                }
                if (inPast && allowPastMonths) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = { viewModel.selectedMonth.value = activeMonthKey },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "↪ BACK TO CURRENT MONTH",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ChromaBlack
                        )
                    }
                }
            }
        }

        // Overall Month Budget Summary — flat
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Chroma.color.surface)
                    .border(1.dp, Ascii.hairlineStrong, RoundedCornerShape(2.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "TOTAL_SPENT",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Chroma.color.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatCurrency(totalSpent),
                            style = Chroma.type.titleLarge.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL_LIMIT",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Chroma.color.onSurfaceVariant
                        )
                        Text(
                            text = if (totalBudget > 0) FormatUtils.formatCurrency(totalBudget) else "NO LIMIT",
                            style = Chroma.type.titleLarge.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Black
                            ),
                            color = if (totalBudget > 0) ChromaCyan else Chroma.color.onSurfaceVariant
                        )
                    }
                }

                if (totalBudget > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val progress = (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
                    val isExceeded = totalSpent > totalBudget
                    HapticOnCrossing(
                        key = null,
                        view = LocalView.current,
                        crossed = isExceeded,
                        effect = Haptics::budgetLimitCrossed
                    )
                    AsciiMoodBar(
                        progress = progress,
                        overBudget = isExceeded,
                        label = if (isExceeded) {
                            "OVER ${FormatUtils.formatCurrency(totalSpent - totalBudget)}"
                        } else {
                            "${(progress * 100).toInt()}% USED · ${FormatUtils.formatCurrency(totalBudget - totalSpent)} LEFT"
                        }
                    )
                }
            }
        }

        // Category Budgets Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AsciiSectionHeader(text = "CATEGORY_LIMITS", showRule = false)
                }
                if (categories.isNotEmpty()) {
                    TextButton(onClick = {
                        selectedCategoryForBudget = categories.firstOrNull()
                        budgetAmountInput = ""
                        showAddBudgetDialog = true
                    }) {
                        Text(
                            text = "+ SET LIMIT",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Chroma.color.onSurface
                        )
                    }
                }
            }
        }

        // Budget Progress List
        if (budgetProgressList.isEmpty()) {
            item {
                AsciiEmptyState(
                    title = "NO BUDGETS SET",
                    subtitle = "Set monthly spending limits for categories to stay on track.",
                    actionLabel = "+ SET CATEGORY BUDGET",
                    onAction = {
                        selectedCategoryForBudget = categories.firstOrNull()
                        budgetAmountInput = ""
                        showAddBudgetDialog = true
                    }
                )
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(Chroma.color.surface)
                        .border(1.dp, Chroma.color.outline, RoundedCornerShape(2.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    budgetProgressList.forEach { item ->
                        val progress = item.progress.coerceIn(0f, 1f)
                        val isExceeded = item.isOverBudget

                        HapticOnCrossing(
                            key = item.category.id,
                            view = LocalView.current,
                            crossed = isExceeded,
                            effect = Haptics::budgetLimitCrossed
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(ChromaStone100)
                                .border(0.5.dp, ChromaStone300, RoundedCornerShape(2.dp))
                                .clickable {
                                    selectedCategoryForBudget = item.category
                                    budgetAmountInput = if (item.limit > 0) item.limit.toInt().toString() else ""
                                    showAddBudgetDialog = true
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(ChromaStone100)
                                        .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = FormatUtils.getCategoryIcon(item.category.icon),
                                        contentDescription = null,
                                        tint = ChromaBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.category.name,
                                    style = Chroma.type.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${FormatUtils.formatCurrency(item.spent)} / ${if (item.limit > 0) FormatUtils.formatCurrency(item.limit) else "No limit"}",
                                style = Chroma.type.labelSmall.copy(
                                    fontFamily = PlexMono,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        if (item.limit > 0) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Spacer(modifier = Modifier.width(2.dp))
                                AsciiMoodBar(
                                    progress = progress,
                                    overBudget = isExceeded,
                                    barColor = ChromaGreen,
                                    label = if (isExceeded) {
                                        "OVER ${FormatUtils.formatCurrency(item.spent - item.limit)}"
                                    } else {
                                        "( ${item.remaining.toLong()} LEFT )"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddBudgetDialog && selectedCategoryForBudget != null) {
        val cat = selectedCategoryForBudget!!
        AlertDialog(
            onDismissRequest = { showAddBudgetDialog = false },
            shape = RoundedCornerShape(2.dp),
            containerColor = Chroma.color.surface,
            title = {
                Text(
                    text = "SET_LIMIT // ${cat.name.uppercase()}",
                    style = Chroma.type.titleMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter monthly limit for ${FormatUtils.formatMonth(currentMonth)}:",
                        style = Chroma.type.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetAmountInput,
                        onValueChange = { budgetAmountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Limit Amount (₹)", fontFamily = PlexMono) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(2.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ChromaBlack,
                            unfocusedBorderColor = ChromaStone300
                        )
                    )
                }
            },
            confirmButton = {
                ChromaButton(
                    text = "SAVE",
                    onClick = {
                        val amount = budgetAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.setBudget(cat.id, amount)
                        }
                        showAddBudgetDialog = false
                    },
                    backgroundColor = ChromaOrange,
                    textColor = ChromaWhite,
                    shadowOffset = 1.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = Chroma.color.onSurface)
                }
            }
        )
    }
}