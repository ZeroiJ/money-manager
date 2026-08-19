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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.theme.*
import com.example.moneymanager.util.FormatUtils

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "budgets // limits",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
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
            // Month Selector Bar
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "period.selector",
                    shadowOffset = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { viewModel.navigateMonth(-1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = FormatUtils.formatMonth(currentMonth).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { viewModel.navigateMonth(1) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Overall Month Budget Summary
            item {
                ChromaCard(
                    modifier = Modifier.fillMaxWidth(),
                    windowTitle = "month_summary // progress",
                    statusIndicator = if (totalBudget > 0) "${((totalSpent / totalBudget) * 100).toInt()}%" else "NO LIMIT",
                    shadowOffset = 3.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL_SPENT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = FormatUtils.formatCurrency(totalSpent),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TOTAL_LIMIT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (totalBudget > 0) FormatUtils.formatCurrency(totalBudget) else "NO LIMIT",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = if (totalBudget > 0) ChromaCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (totalBudget > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val progress = (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
                            val isExceeded = totalSpent > totalBudget

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(ChromaStone200)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .background(if (isExceeded) ChromaRed else ChromaOrange)
                                )
                            }
                        }
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
                    Text(
                        text = "CATEGORY_LIMITS.CONFIG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    if (categories.isNotEmpty()) {
                        TextButton(onClick = {
                            selectedCategoryForBudget = categories.firstOrNull()
                            budgetAmountInput = ""
                            showAddBudgetDialog = true
                        }) {
                            Text(
                                text = "+ SET LIMIT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Budget Progress List
            if (budgetProgressList.isEmpty()) {
                item {
                    ChromaCard(
                        modifier = Modifier.fillMaxWidth(),
                        windowTitle = "category_budgets // empty",
                        shadowOffset = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NO BUDGETS SET FOR THIS MONTH",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Set monthly spending limits for categories to stay on track.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            ChromaButton(
                                text = "+ SET CATEGORY BUDGET",
                                onClick = {
                                    selectedCategoryForBudget = categories.firstOrNull()
                                    budgetAmountInput = ""
                                    showAddBudgetDialog = true
                                },
                                backgroundColor = ChromaBlack,
                                textColor = ChromaWhite
                            )
                        }
                    }
                }
            } else {
                items(budgetProgressList, key = { it.category.id }) { item ->
                    val progress = item.progress.coerceIn(0f, 1f)
                    val isExceeded = item.isOverBudget

                    ChromaCard(
                        modifier = Modifier.fillMaxWidth(),
                        shadowOffset = 2.dp,
                        onClick = {
                            selectedCategoryForBudget = item.category
                            budgetAmountInput = if (item.limit > 0) item.limit.toInt().toString() else ""
                            showAddBudgetDialog = true
                        }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ChromaStone100)
                                            .border(1.dp, ChromaStone400, RoundedCornerShape(4.dp)),
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
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Text(
                                    text = "${FormatUtils.formatCurrency(item.spent)} / ${if (item.limit > 0) FormatUtils.formatCurrency(item.limit) else "No limit"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            if (item.limit > 0) {
                                Spacer(modifier = Modifier.height(10.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(ChromaStone200)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progress)
                                            .background(if (isExceeded) ChromaRed else ChromaGreen)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${(progress * 100).toInt()}% USED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp
                                        )
                                    )
                                    Text(
                                        text = if (isExceeded) "OVER BY ${FormatUtils.formatCurrency(item.spent - item.limit)}"
                                        else "${FormatUtils.formatCurrency(item.remaining)} LEFT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isExceeded) ChromaRed else MaterialTheme.colorScheme.onSurfaceVariant
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

    if (showAddBudgetDialog && selectedCategoryForBudget != null) {
        val cat = selectedCategoryForBudget!!
        AlertDialog(
            onDismissRequest = { showAddBudgetDialog = false },
            shape = RoundedCornerShape(4.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "SET_LIMIT // ${cat.name.uppercase()}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter monthly limit for ${FormatUtils.formatMonth(currentMonth)}:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetAmountInput,
                        onValueChange = { budgetAmountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Limit Amount (₹)", fontFamily = FontFamily.Monospace) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
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
                    shadowOffset = 2.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddBudgetDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}
