package com.example.moneymanager.ui.screens.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.ascii.Ascii
import com.example.moneymanager.ui.ascii.AsciiEmptyState
import com.example.moneymanager.ui.ascii.AsciiSelectChip
import com.example.moneymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionListScreen(
    viewModel: TransactionsViewModel = hiltViewModel(),
    onNavigateToEditTransaction: (Long) -> Unit = {}
) {
    val query by viewModel.searchQuery.collectAsState()
    val scopeFilter by viewModel.scopeFilter.collectAsState()
    val modeFilter by viewModel.paymentModeFilter.collectAsState()
    val groupedTxs by viewModel.groupedTransactions.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val totalFilteredSpend by viewModel.totalFilteredSpend.collectAsState()
    val filteredTxs by viewModel.filteredTransactions.collectAsState()

    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // Inline header
        Text(
            text = "query // transactions",
            style = Chroma.type.titleMedium.copy(
                fontFamily = PlexMono,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        // Search Field — flat terminal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Chroma.color.surface)
                .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "❯",
                    style = Chroma.type.bodyMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold,
                        color = ChromaOrange
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = query,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = {
                        Text(
                            "filter by note, category, amount...",
                            style = Chroma.type.bodyMedium.copy(fontFamily = PlexMono)
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
                            .clickable { viewModel.searchQuery.value = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(12.dp),
                            tint = ChromaBlack
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row — flat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                null to "ALL",
                TransactionScope.PERSONAL to "PERSONAL",
                TransactionScope.HOUSEHOLD to "HOUSEHOLD"
            ).forEach { (scope, label) ->
                AsciiSelectChip(
                    label = label,
                    selected = scopeFilter == scope,
                    onClick = { viewModel.scopeFilter.value = scope },
                    selectedColor = when (scope) {
                        TransactionScope.PERSONAL -> ChromaBlue
                        TransactionScope.HOUSEHOLD -> ChromaOrange
                        else -> ChromaBlack
                    }
                )
            }

            PaymentMode.values().forEach { mode ->
                AsciiSelectChip(
                    label = mode.name,
                    selected = modeFilter == mode,
                    onClick = { viewModel.paymentModeFilter.value = if (modeFilter == mode) null else mode },
                    selectedColor = ChromaStone200
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECORDS: ${filteredTxs.size}",
                style = Chroma.type.labelSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "TOTAL: ${FormatUtils.formatCurrency(totalFilteredSpend)}",
                style = Chroma.type.labelSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    color = ChromaRed
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grouped Transaction Feed — dense single column, one-line rows
        if (filteredTxs.isEmpty()) {
            AsciiEmptyState(
                title = "NO MATCHING RECORDS",
                subtitle = "Try clearing the search or filters.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                groupedTxs.forEach { dayGroup ->
                    // ASCII day header
                    item(key = "day_${dayGroup.dateLabel}") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Ascii.hairline)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dayGroup.dateLabel.uppercase(),
                                style = Chroma.type.labelSmall.copy(
                                    fontFamily = PlexMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Chroma.color.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (dayGroup.dayTotalSpend > 0) {
                                Text(
                                    text = "-" + FormatUtils.formatCurrency(dayGroup.dayTotalSpend),
                                    style = Chroma.type.labelSmall.copy(
                                        fontFamily = PlexMono,
                                        fontWeight = FontWeight.Bold,
                                        color = ChromaRed,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .background(Ascii.hairline)
                            )
                        }
                    }

                    items(
                        items = dayGroup.transactions,
                        key = { "tx_${it.id}" }
                    ) { tx ->
                        val category = categoriesMap[tx.categoryId]
                        OneLineTransactionRow(
                            tx = tx,
                            categoryName = category?.name ?: "UNKNOWN",
                            categoryIcon = FormatUtils.getCategoryIcon(category?.icon.orEmpty()),
                            onClick = { onNavigateToEditTransaction(tx.id) },
                            onDelete = { transactionToDelete = tx }
                        )
                    }
                }
            }
        }
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            shape = RoundedCornerShape(2.dp),
            containerColor = Chroma.color.surface,
            title = {
                Text(
                    text = "DELETE_RECORD // CONFIRM",
                    style = Chroma.type.titleMedium.copy(
                        fontFamily = PlexMono,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Delete record for ${FormatUtils.formatCurrency(tx.amount)}?",
                    style = Chroma.type.bodyMedium
                )
            },
            confirmButton = {
                ChromaButton(
                    text = "CONFIRM DELETE",
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    backgroundColor = ChromaRed,
                    textColor = ChromaWhite,
                    shadowOffset = 1.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = Chroma.color.onSurface)
                }
            }
        )
    }
}

@Composable
private fun OneLineTransactionRow(
    tx: Transaction,
    categoryName: String,
    categoryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val expense = tx.type == TransactionType.EXPENSE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(Chroma.color.surface)
            .border(1.dp, Ascii.hairline, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ChromaStone100)
                .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = null,
                tint = ChromaBlack,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        // One-line label: note · mode · scope
        Text(
            text = buildString {
                append(tx.note.ifBlank { categoryName })
                append(" · ")
                append(tx.paymentMode.name)
                append(" · ")
                append(if (tx.scope == TransactionScope.PERSONAL) "P" else "H")
            },
            style = Chroma.type.bodySmall.copy(
                fontFamily = PlexMono,
                fontWeight = FontWeight.Bold
            ),
            color = Chroma.color.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = (if (expense) "-" else "+") + FormatUtils.formatCurrency(tx.amount),
            style = Chroma.type.labelSmall.copy(
                fontFamily = PlexMono,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            ),
            color = if (expense) ChromaRed else ChromaGreen
        )
        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, ChromaStone400, RoundedCornerShape(2.dp))
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✕",
                style = Chroma.type.labelSmall.copy(
                    fontFamily = PlexMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                ),
                color = Chroma.color.onSurfaceVariant
            )
        }
    }
}