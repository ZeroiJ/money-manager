package com.example.moneymanager.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.*
import com.example.moneymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAdd: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditTransaction: (Long) -> Unit = {}
) {
    val todaySpend by viewModel.todaySpend.collectAsState()
    val monthSpend by viewModel.monthSpend.collectAsState()
    val monthIncome by viewModel.monthIncome.collectAsState()
    val personalMonth by viewModel.personalSpendMonth.collectAsState()
    val householdMonth by viewModel.householdSpendMonth.collectAsState()
    val totalBudget by viewModel.totalBudgetLimit.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val selectedScope by viewModel.selectedScopeFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "chroma//money",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                        ChromaBadge(
                            text = FormatUtils.formatMonth(FormatUtils.getCurrentMonthKey()).uppercase(),
                            backgroundColor = ChromaStone200,
                            textColor = ChromaBlack
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .chromaShadow(offset = 3.dp, cornerRadius = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ChromaOrange)
                    .border(1.5.dp, ChromaBlack, RoundedCornerShape(4.dp))
                    .clickable { onNavigateToAdd() }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ChromaWhite, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ ADD SPEND",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ChromaWhite
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Today's Spend Window Card
            item {
                ChromaTodayHeroCard(
                    todaySpend = todaySpend,
                    monthSpend = monthSpend,
                    monthIncome = monthIncome,
                    totalBudget = totalBudget
                )
            }

            // Personal vs Household Split Cards
            item {
                ChromaPersonalHouseholdSplitRow(
                    personalSpend = personalMonth,
                    householdSpend = householdMonth
                )
            }

            // Scope Filter Selector Box
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        HomeScopeFilter.ALL to "ALL",
                        HomeScopeFilter.PERSONAL to "PERSONAL",
                        HomeScopeFilter.HOUSEHOLD to "HOUSEHOLD"
                    ).forEach { (scopeItem, label) ->
                        val isSelected = selectedScope == scopeItem
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .chromaShadow(offset = if (isSelected) 2.dp else 1.dp, cornerRadius = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) ChromaBlack else MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { viewModel.selectedScopeFilter.value = scopeItem }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "[ $label ]",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) ChromaWhite else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT_ACTIVITY.LOG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    TextButton(
                        onClick = onNavigateToTransactions,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "VIEW ALL →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Recent Transactions List
            if (recentTxs.isEmpty()) {
                item {
                    ChromaEmptyTransactionsPlaceholder(onAddClick = onNavigateToAdd)
                }
            } else {
                items(recentTxs, key = { it.id }) { tx ->
                    val category = categoriesMap[tx.categoryId]
                    TransactionCard(
                        transaction = tx,
                        category = category,
                        onClick = { onNavigateToEditTransaction(tx.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChromaTodayHeroCard(
    todaySpend: Double,
    monthSpend: Double,
    monthIncome: Double,
    totalBudget: Double
) {
    ChromaCard(
        modifier = Modifier.fillMaxWidth(),
        windowTitle = "today.spend // realtime",
        statusIndicator = "[ LIVE ]",
        shadowOffset = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TODAY'S TOTAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = FormatUtils.formatCurrency(todaySpend),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                ),
                color = if (todaySpend > 0) ChromaRed else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = ChromaStone200)
            Spacer(modifier = Modifier.height(12.dp))

            // Month summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MONTH_SPENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = FormatUtils.formatCurrency(monthSpend),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (monthIncome > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "MONTH_INCOME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatCurrency(monthIncome),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ChromaGreen
                        )
                    }
                } else if (totalBudget > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BUDGET_LIMIT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatCurrency(totalBudget),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ChromaCyan
                        )
                    }
                }
            }

            // Budget Progress Bar
            if (totalBudget > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = (monthSpend / totalBudget).toFloat().coerceIn(0f, 1f)
                val isOverBudget = monthSpend > totalBudget

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
                            .background(if (isOverBudget) ChromaRed else ChromaOrange)
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
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (isOverBudget) "OVER: ${FormatUtils.formatCurrency(monthSpend - totalBudget)}"
                        else "LEFT: ${FormatUtils.formatCurrency(totalBudget - monthSpend)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) ChromaRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ChromaPersonalHouseholdSplitRow(
    personalSpend: Double,
    householdSpend: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Personal Card
        ChromaCard(
            modifier = Modifier.weight(1f),
            windowTitle = "personal.db",
            shadowOffset = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                ChromaBadge(text = "PERSONAL", backgroundColor = ChromaBlue, textColor = ChromaWhite)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = FormatUtils.formatCurrency(personalSpend),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Household Card
        ChromaCard(
            modifier = Modifier.weight(1f),
            windowTitle = "household.db",
            shadowOffset = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                ChromaBadge(text = "HOUSEHOLD", backgroundColor = ChromaOrange, textColor = ChromaWhite)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = FormatUtils.formatCurrency(householdSpend),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    category: Category?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isExpense = transaction.type == TransactionType.EXPENSE

    ChromaCard(
        modifier = modifier.fillMaxWidth(),
        shadowOffset = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Boxy Category Icon Badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ChromaStone100)
                    .border(1.dp, ChromaStone400, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FormatUtils.getCategoryIcon(category?.icon ?: "category"),
                    contentDescription = null,
                    tint = ChromaBlack,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (transaction.note.isNotBlank()) transaction.note else (category?.name ?: "Expense"),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category?.name ?: "Misc",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = transaction.paymentMode.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text("•", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = transaction.scope.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.scope == TransactionScope.PERSONAL) ChromaBlue else ChromaOrange
                        )
                    )
                }
            }

            // Amount Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isExpense) "- " else "+ ") + FormatUtils.formatCurrency(transaction.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isExpense) ChromaRed else ChromaGreen
                )
                Text(
                    text = FormatUtils.formatDate(transaction.date),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChromaEmptyTransactionsPlaceholder(onAddClick: () -> Unit) {
    ChromaCard(
        modifier = Modifier.fillMaxWidth(),
        windowTitle = "status // empty",
        shadowOffset = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ChromaStone100)
                    .border(1.dp, ChromaStone400, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = ChromaBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "NO TRANSACTIONS LOGGED",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Record an expense in under 5 seconds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            ChromaButton(
                text = "+ ADD TRANSACTION",
                onClick = onAddClick,
                backgroundColor = ChromaBlack,
                textColor = ChromaWhite
            )
        }
    }
}
