package com.example.moneymanager.ui.screens.budgets

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.ui.ios.ConfettiOverlay
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.IosProgressBar
import com.example.moneymanager.ui.ios.IosSegmented
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.maskedCurrency
import com.example.moneymanager.ui.ios.pressShrink
import com.example.moneymanager.ui.kinetic.BarcodeMeter
import com.example.moneymanager.ui.kinetic.KineticHeader
import com.example.moneymanager.ui.kinetic.LedgerAmount
import com.example.moneymanager.ui.kinetic.LedgerPill
import com.example.moneymanager.ui.kinetic.MonoLabel
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
    val scheme = currentScheme()

    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf<Category?>(null) }
    var budgetAmountInput by remember { mutableStateOf("") }
    var showConfetti by remember { mutableStateOf(false) }
    var celebratedFirstLimit by rememberSaveable { mutableStateOf(false) }

    val activeMonthKey = FormatUtils.getCurrentMonthKey()
    var allowPastMonths by rememberSaveable { mutableStateOf(false) }
    val onActiveMonth = currentMonth == activeMonthKey
    val inPast = !onActiveMonth && currentMonth < activeMonthKey

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            KineticHeader(
                lead = "Monthly",
                headline = "Budgets",
                headlineSize = 28.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.card)
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val prevEnabled = !onActiveMonth || allowPastMonths
                    IconButton(
                        onClick = { viewModel.navigateMonth(-1) },
                        enabled = prevEnabled,
                        modifier = Modifier
                            .size(44.dp)
                            .alpha(if (prevEnabled) 1f else 0.35f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous month",
                            tint = scheme.accent
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = FormatUtils.formatMonth(currentMonth),
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = scheme.label
                        )
                        Text(
                            text = when {
                                onActiveMonth -> "Active"
                                inPast -> "Past"
                                else -> "Future"
                            },
                            fontFamily = SpaceGrotesk,
                            fontSize = 13.sp,
                            color = when {
                                onActiveMonth -> scheme.success
                                inPast -> Color(0xFFFF9500)
                                else -> scheme.secondaryLabel
                            }
                        )
                    }

                    val nextEnabled = !onActiveMonth
                    IconButton(
                        onClick = { viewModel.navigateMonth(1) },
                        enabled = nextEnabled,
                        modifier = Modifier
                            .size(44.dp)
                            .alpha(if (nextEnabled) 1f else 0.35f)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next month",
                            tint = scheme.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                IosSegmented(
                    options = listOf("Current", "Past"),
                    selected = if (allowPastMonths) 1 else 0,
                    onSelect = { allowPastMonths = it == 1 },
                    scheme = scheme
                )
                if (inPast && allowPastMonths) {
                    TextButton(
                        onClick = { viewModel.selectedMonth.value = activeMonthKey },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Back to current month",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = scheme.accent
                        )
                    }
                }
            }
        }

        // ── Capital overview hero (PRD §4.2) ────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.card)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonoLabel(
                        text = FormatUtils.formatMonth(currentMonth) + " allocations",
                        color = scheme.secondaryLabel,
                        modifier = Modifier.weight(1f)
                    )
                    if (categories.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .pressShrink()
                                .background(scheme.accent)
                                .clickable {
                                    selectedCategoryForBudget = categories.firstOrNull()
                                    budgetAmountInput = ""
                                    showAddBudgetDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+ ADD",
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = scheme.label
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LedgerAmount(
                    text = FormatUtils.formatLedger(totalBudget),
                    fontSize = 38.sp,
                    color = scheme.label,
                    dimmedColor = scheme.secondaryLabel
                )
                MonoLabel(text = "INR / month", color = scheme.secondaryLabel)
                Spacer(modifier = Modifier.height(12.dp))
                val utilization = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonoLabel(
                        text = if (totalBudget > 0) "${(utilization * 100).toString().take(4)}% utilized" else "no caps set",
                        color = scheme.label,
                        modifier = Modifier.weight(1f)
                    )
                }
                BarcodeMeter(
                    fraction = utilization,
                    color = scheme.label,
                    inactiveColor = scheme.separator,
                    total = 34,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .padding(top = 6.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        MonoLabel(text = "Used", color = scheme.secondaryLabel)
                        LedgerAmount(
                            text = FormatUtils.formatLedger(totalSpent),
                            fontSize = 18.sp,
                            color = scheme.label,
                            dimmedColor = scheme.secondaryLabel
                        )
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        MonoLabel(text = "Reserve", color = scheme.secondaryLabel)
                        LedgerAmount(
                            text = FormatUtils.formatLedger((totalBudget - totalSpent).coerceAtLeast(0.0)),
                            fontSize = 18.sp,
                            color = scheme.label,
                            dimmedColor = scheme.secondaryLabel
                        )
                    }
                }
                if (totalBudget > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val isExceeded = totalSpent > totalBudget
                    HapticOnCrossing(
                        key = null,
                        view = LocalView.current,
                        crossed = isExceeded,
                        effect = Haptics::budgetLimitCrossed
                    )
                    Text(
                        text = if (isExceeded) {
                            "Over by ${maskedCurrency(totalSpent - totalBudget)}"
                        } else {
                            "${(utilization * 100).toInt()}% used · ${maskedCurrency(totalBudget - totalSpent)} left"
                        },
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = if (isExceeded) scheme.destructive else scheme.secondaryLabel
                    )
                }
            }
        }

        // ── Buffer summary chip (PRD §4.2) ──────────────────────────────
        if (totalBudget > 0) {
            item {
                val activeCount = budgetProgressList.count { it.limit > 0 }
                LedgerPill(
                    text = "$activeCount active · ${maskedCurrency((totalBudget - totalSpent).coerceAtLeast(0.0))} buffer left",
                    container = if (totalSpent > totalBudget) scheme.destructive else scheme.slab,
                    content = if (totalSpent > totalBudget) scheme.paper else scheme.onSlab,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Limits",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = scheme.label,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
                if (categories.isNotEmpty()) {
                    TextButton(onClick = {
                        selectedCategoryForBudget = categories.firstOrNull()
                        budgetAmountInput = ""
                        showAddBudgetDialog = true
                    }) {
                        Text(
                            text = "Set limit",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = scheme.accent
                        )
                    }
                }
            }
        }

        if (budgetProgressList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(scheme.card)
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No budgets set",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = scheme.label
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set monthly limits to stay on track.",
                            fontFamily = SpaceGrotesk,
                            fontSize = 15.sp,
                            color = scheme.secondaryLabel
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Set a limit",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = scheme.accent,
                            modifier = Modifier.clickable {
                                selectedCategoryForBudget = categories.firstOrNull()
                                budgetAmountInput = ""
                                showAddBudgetDialog = true
                            }
                        )
                    }
                }
            }
        } else {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    budgetProgressList.forEachIndexed { index, item ->
                        val progress = item.progress.coerceIn(0f, 1f)
                        val isExceeded = item.isOverBudget

                        HapticOnCrossing(
                            key = item.category.id,
                            view = LocalView.current,
                            crossed = isExceeded,
                            effect = Haptics::budgetLimitCrossed
                        )

                        // PRD §4.2: stack tiles — overrun = deep maroon slab,
                        // healthy = dark slab, alternating tone.
                        val bg = when {
                            isExceeded -> scheme.maroon
                            index % 2 == 1 -> scheme.slabAlt
                            else -> scheme.slab
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .clickable {
                                    selectedCategoryForBudget = item.category
                                    budgetAmountInput =
                                        if (item.limit > 0) item.limit.toInt().toString() else ""
                                    showAddBudgetDialog = true
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = FormatUtils.getCategoryIcon(item.category.icon),
                                    contentDescription = null,
                                    tint = if (isExceeded) scheme.destructive else scheme.onSlab,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.category.name,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = if (isExceeded) scheme.destructive else scheme.onSlab
                                    )
                                    MonoLabel(
                                        text = if (item.limit > 0)
                                            "${maskedCurrency(item.spent)} of ${maskedCurrency(item.limit)}"
                                        else "${maskedCurrency(item.spent)} · no cap",
                                        color = if (isExceeded) scheme.destructive else scheme.onSlabSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                                if (item.limit > 0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        LedgerAmount(
                                            text = "${(progress * 100).toString().take(4)}%",
                                            fontSize = 16.sp,
                                            color = if (isExceeded) scheme.destructive else scheme.onSlab,
                                            dimmedColor = if (isExceeded) scheme.destructive.copy(alpha = 0.6f)
                                            else scheme.onSlabSecondary
                                        )
                                        if (isExceeded) {
                                            MonoLabel(
                                                text = "Spent",
                                                color = scheme.destructive,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }

                            if (item.limit > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                // Dual-tone linear bar (PRD §4.2).
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .background(scheme.onSlab.copy(alpha = 0.15f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progress)
                                                .height(6.dp)
                                                .background(
                                                    if (isExceeded) scheme.destructive
                                                    else scheme.accent
                                                )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                MonoLabel(
                                    text = if (isExceeded) {
                                        "Exceeded threshold by ${maskedCurrency(item.spent - item.limit)}"
                                    } else {
                                        "${maskedCurrency(item.remaining)} left"
                                    },
                                    color = if (isExceeded) scheme.destructive else scheme.onSlabSecondary,
                                    fontSize = 10.sp
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
            shape = RoundedCornerShape(16.dp),
            containerColor = scheme.card,
            title = {
                Text(
                    text = "Limit · ${cat.name}",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = scheme.label
                )
            },
            text = {
                Column {
                    Text(
                        text = "Monthly limit for ${FormatUtils.formatMonth(currentMonth)}",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        color = scheme.label
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetAmountInput,
                        onValueChange = { budgetAmountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Limit amount (₹)", fontFamily = SpaceGrotesk) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = scheme.accent,
                            unfocusedBorderColor = scheme.separator
                        )
                    )
                }
            },
            confirmButton = {
                val saveView = LocalView.current
                Button(
                    onClick = {
                        val amount = budgetAmountInput.toDoubleOrNull() ?: 0.0
                        val hadNoBudget = totalBudget == 0.0
                        if (amount > 0) {
                            Haptics.saveConfirmed(saveView)
                            viewModel.setBudget(cat.id, amount)
                            if (hadNoBudget && !celebratedFirstLimit) {
                                celebratedFirstLimit = true
                                showConfetti = true
                            }
                        }
                        showAddBudgetDialog = false
                    },
                    modifier = Modifier.pressShrink(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.accent)
                ) {
                    Text("Save", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text("Cancel", fontFamily = SpaceGrotesk, color = scheme.accent)
                }
            }
        )
    }

    ConfettiOverlay(
        show = showConfetti,
        title = "Limit set!",
        subtitle = "Your first monthly guardrail.",
        onDone = { showConfetti = false }
    )
    }
}
