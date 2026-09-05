package com.example.moneymanager.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ascii.Ascii
import com.example.moneymanager.ui.ascii.AsciiDivider
import com.example.moneymanager.ui.ascii.AsciiEmptyState
import com.example.moneymanager.ui.ascii.AsciiHeroFigure
import com.example.moneymanager.ui.ascii.AsciiMoodBar
import com.example.moneymanager.ui.ascii.AsciiSectionHeader
import com.example.moneymanager.ui.ascii.AsciiSelectChip
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
    val totalBudget by viewModel.totalBudgetLimit.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val selectedScope by viewModel.selectedScopeFilter.collectAsState()

    Scaffold(
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .chromaShadow(offset = 1.dp, cornerRadius = 4.dp)
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
                        style = Chroma.type.labelMedium.copy(
                            fontFamily = PlexMono,
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
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Hero — ASCII ₹ figure + today/month/budget ──────────────────
            item {
                ChromaTodayHeroCard(
                    todaySpend = todaySpend,
                    monthSpend = monthSpend,
                    monthIncome = monthIncome,
                    totalBudget = totalBudget
                )
            }

            // ── Scope filter selector (flat segmented chips) ────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsciiSelectChip(
                        label = "ALL",
                        selected = selectedScope == HomeScopeFilter.ALL,
                        onClick = { viewModel.selectedScopeFilter.value = HomeScopeFilter.ALL },
                        modifier = Modifier.weight(1f),
                        selectedColor = ChromaBlack
                    )
                    AsciiSelectChip(
                        label = "PERSONAL",
                        selected = selectedScope == HomeScopeFilter.PERSONAL,
                        onClick = { viewModel.selectedScopeFilter.value = HomeScopeFilter.PERSONAL },
                        modifier = Modifier.weight(1f),
                        selectedColor = ChromaBlue
                    )
                    AsciiSelectChip(
                        label = "HOUSEHOLD",
                        selected = selectedScope == HomeScopeFilter.HOUSEHOLD,
                        onClick = { viewModel.selectedScopeFilter.value = HomeScopeFilter.HOUSEHOLD },
                        modifier = Modifier.weight(1f),
                        selectedColor = ChromaOrange
                    )
                    // Settings button — flat, no shadow
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Chroma.color.surface)
                            .border(1.dp, Ascii.hairlineStrong, RoundedCornerShape(2.dp))
                            .clickable { onNavigateToSettings() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Chroma.color.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ── Recent activity header ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AsciiSectionHeader(text = "RECENT_ACTIVITY.LOG", showRule = false)
                    }
                    TextButton(
                        onClick = onNavigateToTransactions,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "VIEW ALL →",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Chroma.color.onSurface
                        )
                    }
                }
            }

            // ── Transaction feed — top 3 ─────────────────────────────────────
            if (recentTxs.isEmpty()) {
                item {
                    AsciiEmptyState(
                        title = "NO TRANSACTIONS LOGGED",
                        subtitle = "Record an expense in under 5 seconds.",
                        actionLabel = "+ ADD TRANSACTION",
                        onAction = onNavigateToAdd
                    )
                }
            } else {
                items(recentTxs.take(3), key = { it.id }) { tx ->
                    HomeFeedRow(
                        transaction = tx,
                        category = categoriesMap[tx.categoryId],
                        onClick = { onNavigateToEditTransaction(tx.id) }
                    )
                }
            }
        }
    }
}

/**
 * Flat hero block: ASCII ₹ figure, today's total, month income/budget, then a
 * block-glyph mood bar when a budget limit is set. No shadow — hairline border only.
 */
@Composable
fun ChromaTodayHeroCard(
    todaySpend: Double,
    monthSpend: Double,
    monthIncome: Double,
    totalBudget: Double
) {
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Chroma.color.surface)
            .border(1.dp, Ascii.hairlineStrong, shape)
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsciiHeroFigure(color = ChromaBlack)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TODAY'S TOTAL",
                style = Chroma.type.labelSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Chroma.color.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = FormatUtils.formatCurrency(todaySpend),
                style = Chroma.type.displaySmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Black
                ),
                color = if (todaySpend > 0) ChromaRed else Chroma.color.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            AsciiDivider(color = Ascii.hairline)
            Spacer(modifier = Modifier.height(10.dp))

            // Month info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MONTH_SPENT",
                        style = Chroma.type.labelSmall.copy(
                            fontFamily = PlexMono,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Chroma.color.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatCurrency(monthSpend),
                        style = Chroma.type.titleMedium.copy(
                            fontFamily = PlexMono,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                if (monthIncome > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "MONTH_IN",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Chroma.color.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatCurrency(monthIncome),
                            style = Chroma.type.titleMedium.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ChromaGreen
                        )
                    }
                } else if (totalBudget > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BUDGET",
                            style = Chroma.type.labelSmall.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Chroma.color.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatCurrency(totalBudget),
                            style = Chroma.type.titleMedium.copy(
                                fontFamily = PlexMono,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ChromaCyan
                        )
                    }
                }
            }

            // Block mood bar inline if a budget limit exists
            if (totalBudget > 0) {
                val progress = (monthSpend / totalBudget).toFloat().coerceIn(0f, 1f)
                val isOverBudget = monthSpend > totalBudget
                Spacer(modifier = Modifier.height(8.dp))
                AsciiMoodBar(
                    progress = progress,
                    overBudget = isOverBudget,
                    label = if (isOverBudget) {
                        "OVER ${FormatUtils.formatCurrency(monthSpend - totalBudget)}"
                    } else {
                        "${(progress * 100).toInt()}% · LEFT ${FormatUtils.formatCurrency(totalBudget - monthSpend)}"
                    }
                )
            }
        }
    }
}

/**
 * Compact one-line feed row for the home shortcut (top 3). Flat hairline box,
 * scope prefix, category · payment mode, amount + date. Tap → edit transaction.
 */
@Composable
private fun HomeFeedRow(
    transaction: Transaction,
    category: Category?,
    onClick: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val shape = RoundedCornerShape(2.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Chroma.color.surface)
            .border(1.dp, Ascii.hairline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (transaction.scope == TransactionScope.PERSONAL) "[P]" else "[H]",
                style = Chroma.type.labelSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = if (transaction.scope == TransactionScope.PERSONAL) ChromaBlue else ChromaOrange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (transaction.note.isNotBlank()) transaction.note else (category?.name ?: "Expense"),
                    style = Chroma.type.bodySmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "${category?.name ?: "Misc"} · ${transaction.paymentMode.name}",
                    style = Chroma.type.labelSmall.copy(
                        fontFamily = PlexMono,
                        fontSize = 10.sp
                    ),
                    color = Chroma.color.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isExpense) "- " else "+ ") + FormatUtils.formatCurrency(transaction.amount),
                    style = Chroma.type.titleSmall.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isExpense) ChromaRed else ChromaGreen
                )
                Text(
                    text = FormatUtils.formatDate(transaction.date),
                    style = Chroma.type.labelSmall.copy(
                        fontFamily = PlexMono,
                        fontSize = 9.sp
                    ),
                    color = Chroma.color.onSurfaceVariant
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
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Box
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ChromaStone100)
                    .border(1.dp, ChromaStone400, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FormatUtils.getCategoryIcon(category?.icon ?: "category"),
                    contentDescription = null,
                    tint = ChromaBlack,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (transaction.note.isNotBlank()) transaction.note else (category?.name ?: "Expense"),
                    style = Chroma.type.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = category?.name ?: "Misc",
                        style = Chroma.type.labelSmall.copy(
                            fontFamily = PlexMono,
                            fontSize = 10.sp
                        ),
                        color = Chroma.color.onSurfaceVariant
                    )
                    Text("·", style = Chroma.type.labelSmall, color = Chroma.color.onSurfaceVariant)
                    Text(
                        text = transaction.paymentMode.name,
                        style = Chroma.type.labelSmall.copy(
                            fontFamily = PlexMono,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text("·", style = Chroma.type.labelSmall, color = Chroma.color.onSurfaceVariant)
                    Text(
                        text = transaction.scope.name,
                        style = Chroma.type.labelSmall.copy(
                            fontFamily = PlexMono,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (transaction.scope == TransactionScope.PERSONAL) ChromaBlue else ChromaOrange
                        )
                    )
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isExpense) "- " else "+ ") + FormatUtils.formatCurrency(transaction.amount),
                    style = Chroma.type.titleSmall.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isExpense) ChromaRed else ChromaGreen
                )
                Text(
                    text = FormatUtils.formatDate(transaction.date),
                    style = Chroma.type.labelSmall.copy(
                        fontFamily = PlexMono,
                        fontSize = 9.sp
                    ),
                    color = Chroma.color.onSurfaceVariant
                )
            }
        }
    }
}