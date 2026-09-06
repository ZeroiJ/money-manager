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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ascii.Ascii
import com.example.moneymanager.ui.ascii.AsciiEmptyState
import com.example.moneymanager.ui.ascii.AsciiSectionHeader
import com.example.moneymanager.ui.screens.add.AddSpendSheet
import com.example.moneymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    presetCategory: String? = null,
    presetPaymentMode: String? = null,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditTransaction: (Long) -> Unit = {}
) {
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    var showAddSheet by remember { mutableStateOf(presetCategory != null) }

    Scaffold(
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .chromaShadow(offset = 1.dp, cornerRadius = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ChromaOrange)
                    .border(1.5.dp, ChromaBlack, RoundedCornerShape(4.dp))
                    .clickable { showAddSheet = true }
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

            // ── Transaction feed — top 10 ────────────────────────────────────
            if (recentTxs.isEmpty()) {
                item {
                    AsciiEmptyState(
                        title = "NO TRANSACTIONS LOGGED",
                        subtitle = "Record an expense in under 5 seconds.",
                        actionLabel = "+ ADD TRANSACTION",
                        onAction = { showAddSheet = true }
                    )
                }
            } else {
                items(recentTxs.take(10), key = { it.id }) { tx ->
                    HomeFeedRow(
                        transaction = tx,
                        category = categoriesMap[tx.categoryId],
                        onClick = { onNavigateToEditTransaction(tx.id) }
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddSpendSheet(
            presetCategory = presetCategory,
            presetPaymentMode = presetPaymentMode,
            onDismiss = { showAddSheet = false }
        )
    }
}

/**
 * Compact one-line feed row for the home shortcut (top 10). Flat hairline box,
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