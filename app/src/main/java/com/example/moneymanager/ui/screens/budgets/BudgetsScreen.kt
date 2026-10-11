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
import com.example.moneymanager.theme.Geist
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.IosProgressBar
import com.example.moneymanager.ui.ios.IosSegmented
import com.example.moneymanager.ui.ios.currentScheme
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

    val activeMonthKey = FormatUtils.getCurrentMonthKey()
    var allowPastMonths by rememberSaveable { mutableStateOf(false) }
    val onActiveMonth = currentMonth == activeMonthKey
    val inPast = !onActiveMonth && currentMonth < activeMonthKey

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Budgets",
                fontFamily = Geist,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = scheme.label,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
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
                            fontFamily = Geist,
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
                            fontFamily = Geist,
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
                            fontFamily = Geist,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = scheme.accent
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.card)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Spent",
                            fontFamily = Geist,
                            fontSize = 13.sp,
                            color = scheme.secondaryLabel
                        )
                        IosAmount(
                            text = FormatUtils.formatCurrency(totalSpent),
                            color = scheme.label,
                            fontSize = 24.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Limit",
                            fontFamily = Geist,
                            fontSize = 13.sp,
                            color = scheme.secondaryLabel
                        )
                        Text(
                            text = if (totalBudget > 0) FormatUtils.formatCurrency(totalBudget) else "No limit",
                            fontFamily = Geist,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = if (totalBudget > 0) scheme.label else scheme.secondaryLabel
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
                    IosProgressBar(
                        progress = progress,
                        overBudget = isExceeded,
                        scheme = scheme
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isExceeded) {
                            "Over by ${FormatUtils.formatCurrency(totalSpent - totalBudget)}"
                        } else {
                            "${(progress * 100).toInt()}% used · ${FormatUtils.formatCurrency(totalBudget - totalSpent)} left"
                        },
                        fontFamily = Geist,
                        fontSize = 13.sp,
                        color = if (isExceeded) scheme.destructive else scheme.secondaryLabel
                    )
                }
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
                    fontFamily = Geist,
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
                            fontFamily = Geist,
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
                            fontFamily = Geist,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = scheme.label
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set monthly limits to stay on track.",
                            fontFamily = Geist,
                            fontSize = 15.sp,
                            color = scheme.secondaryLabel
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Set a limit",
                            fontFamily = Geist,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(scheme.card)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    budgetProgressList.forEachIndexed { index, item ->
                        val progress = item.progress.coerceIn(0f, 1f)
                        val isExceeded = item.isOverBudget

                        HapticOnCrossing(
                            key = item.category.id,
                            view = LocalView.current,
                            crossed = isExceeded,
                            effect = Haptics::budgetLimitCrossed
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategoryForBudget = item.category
                                    budgetAmountInput =
                                        if (item.limit > 0) item.limit.toInt().toString() else ""
                                    showAddBudgetDialog = true
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Color(item.category.color).copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = FormatUtils.getCategoryIcon(item.category.icon),
                                        contentDescription = null,
                                        tint = Color(item.category.color),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.category.name,
                                        fontFamily = Geist,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = scheme.label
                                    )
                                    Text(
                                        text = "${FormatUtils.formatCurrency(item.spent)} of ${if (item.limit > 0) FormatUtils.formatCurrency(item.limit) else "no limit"}",
                                        fontFamily = Geist,
                                        fontSize = 13.sp,
                                        color = scheme.secondaryLabel
                                    )
                                }
                            }

                            if (item.limit > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                IosProgressBar(
                                    progress = progress,
                                    overBudget = isExceeded,
                                    scheme = scheme
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isExceeded) {
                                        "Over by ${FormatUtils.formatCurrency(item.spent - item.limit)}"
                                    } else {
                                        "${FormatUtils.formatCurrency(item.remaining)} left"
                                    },
                                    fontFamily = Geist,
                                    fontSize = 13.sp,
                                    color = if (isExceeded) scheme.destructive else scheme.secondaryLabel
                                )
                            }
                        }
                        if (index < budgetProgressList.size - 1) {
                            HorizontalDivider(color = scheme.separator, thickness = 0.5.dp)
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
                    fontFamily = Geist,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = scheme.label
                )
            },
            text = {
                Column {
                    Text(
                        text = "Monthly limit for ${FormatUtils.formatMonth(currentMonth)}",
                        fontFamily = Geist,
                        fontSize = 15.sp,
                        color = scheme.label
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetAmountInput,
                        onValueChange = { budgetAmountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Limit amount (₹)", fontFamily = Geist) },
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
                Button(
                    onClick = {
                        val amount = budgetAmountInput.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.setBudget(cat.id, amount)
                        }
                        showAddBudgetDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.accent)
                ) {
                    Text("Save", fontFamily = Geist, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text("Cancel", fontFamily = Geist, color = scheme.accent)
                }
            }
        )
    }
}
