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
import com.example.moneymanager.data.model.PaymentMode
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
                            text = "MONEY MANAGER",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        NeoBadge(
                            text = FormatUtils.formatMonth(FormatUtils.getCurrentMonthKey()).uppercase(),
                            backgroundColor = NeoYellow,
                            textColor = NeoBlack
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .neoShadow(offset = 4.dp, cornerRadius = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeoYellow)
                    .border(2.5.dp, NeoBlack, RoundedCornerShape(8.dp))
                    .clickable { onNavigateToAdd() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NeoBlack, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ADD SPEND",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = NeoBlack
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
            // Hero Today's Spend Box
            item {
                NeoTodayHeroCard(
                    todaySpend = todaySpend,
                    monthSpend = monthSpend,
                    monthIncome = monthIncome,
                    totalBudget = totalBudget
                )
            }

            // Personal vs Household Split Cards
            item {
                NeoPersonalHouseholdSplitRow(
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
                                .neoShadow(offset = if (isSelected) 3.dp else 1.dp, cornerRadius = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeoBlack else MaterialTheme.colorScheme.surface)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .clickable { viewModel.selectedScopeFilter.value = scopeItem }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                ),
                                color = if (isSelected) NeoWhite else MaterialTheme.colorScheme.onSurface
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
                        text = "RECENT ACTIVITY",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                    TextButton(
                        onClick = onNavigateToTransactions,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "SEE ALL",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Recent Transactions List
            if (recentTxs.isEmpty()) {
                item {
                    NeoEmptyTransactionsPlaceholder(onAddClick = onNavigateToAdd)
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
fun NeoTodayHeroCard(
    todaySpend: Double,
    monthSpend: Double,
    monthIncome: Double,
    totalBudget: Double
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        shadowOffset = 5.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeoBadge(text = "TODAY'S SPEND", backgroundColor = NeoYellow, textColor = NeoBlack)
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = FormatUtils.formatCurrency(todaySpend),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                ),
                color = if (todaySpend > 0) NeoRed else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(14.dp))

            // Month summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MONTH SPENT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = FormatUtils.formatCurrency(monthSpend),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                }

                if (monthIncome > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "MONTH INCOME",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatCurrency(monthIncome),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = NeoGreen
                        )
                    }
                } else if (totalBudget > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BUDGET LIMIT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = FormatUtils.formatCurrency(totalBudget),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = NeoCyan
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
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeoGray200)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(if (isOverBudget) NeoRed else NeoLime)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}% USED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = if (isOverBudget) "EXCEEDED BY ${FormatUtils.formatCurrency(monthSpend - totalBudget)}"
                        else "${FormatUtils.formatCurrency(totalBudget - monthSpend)} LEFT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) NeoRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NeoPersonalHouseholdSplitRow(
    personalSpend: Double,
    householdSpend: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Personal Card
        NeoCard(
            modifier = Modifier.weight(1f),
            shadowOffset = 3.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                NeoBadge(text = "PERSONAL", backgroundColor = NeoBlue, textColor = NeoWhite)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = FormatUtils.formatCurrency(personalSpend),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Household Card
        NeoCard(
            modifier = Modifier.weight(1f),
            shadowOffset = 3.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                NeoBadge(text = "HOUSEHOLD", backgroundColor = NeoOrange, textColor = NeoWhite)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = FormatUtils.formatCurrency(householdSpend),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.labelSmall,
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

    NeoCard(
        modifier = modifier.fillMaxWidth(),
        shadowOffset = 3.dp,
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
                    .size(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeoYellow)
                    .border(2.dp, NeoBlack, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FormatUtils.getCategoryIcon(category?.icon ?: "category"),
                    contentDescription = null,
                    tint = NeoBlack,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (transaction.note.isNotBlank()) transaction.note else (category?.name ?: "Expense"),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category?.name ?: "Misc",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = transaction.paymentMode.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Text("•", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = transaction.scope.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = if (transaction.scope == TransactionScope.PERSONAL) NeoBlue else NeoOrange
                        )
                    )
                }
            }

            // Amount Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isExpense) "- " else "+ ") + FormatUtils.formatCurrency(transaction.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = if (isExpense) NeoRed else NeoGreen
                )
                Text(
                    text = FormatUtils.formatDate(transaction.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NeoEmptyTransactionsPlaceholder(onAddClick: () -> Unit) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        shadowOffset = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NeoYellow)
                    .border(2.dp, NeoBlack, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = NeoBlack,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "NO EXPENSES RECORDED YET",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the button below to add your first spend in under 5 seconds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            NeoButton(
                text = "+ ADD TRANSACTION",
                onClick = onAddClick,
                backgroundColor = NeoBlack,
                textColor = NeoWhite,
                borderColor = NeoBlack
            )
        }
    }
}
