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
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.theme.*
import com.example.moneymanager.ui.screens.home.TransactionCard
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
    val dateFilter by viewModel.dateFilter.collectAsState()
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
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        // Chroma Search Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .chromaShadow(offset = 2.dp, cornerRadius = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "❯",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
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
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
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
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Scope Filter Chips
                listOf(
                    null to "ALL SCOPE",
                    TransactionScope.PERSONAL to "PERSONAL",
                    TransactionScope.HOUSEHOLD to "HOUSEHOLD"
                ).forEach { (scope, label) ->
                    val isSelected = scopeFilter == scope
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isSelected) {
                                    when (scope) {
                                        TransactionScope.PERSONAL -> ChromaBlue
                                        TransactionScope.HOUSEHOLD -> ChromaOrange
                                        else -> ChromaBlack
                                    }
                                } else MaterialTheme.colorScheme.surface
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
                            .clickable { viewModel.scopeFilter.value = scope }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "[ $label ]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (isSelected) ChromaWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Payment Mode Filter Chips
                PaymentMode.values().forEach { mode ->
                    val isSelected = modeFilter == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) ChromaStone200 else MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
                            .clickable {
                                viewModel.paymentModeFilter.value = if (isSelected) null else mode
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "[ ${mode.name} ]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = ChromaBlack
                        )
                    }
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
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "TOTAL: ${FormatUtils.formatCurrency(totalFilteredSpend)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = ChromaRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grouped Transaction Feed
            if (filteredTxs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO MATCHING RECORDS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedTxs.forEach { dayGroup ->
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(ChromaStone200)
                                        .border(0.5.dp, ChromaStone400, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dayGroup.dateLabel.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = ChromaBlack
                                    )
                                    if (dayGroup.dayTotalSpend > 0) {
                                        Text(
                                            text = "- " + FormatUtils.formatCurrency(dayGroup.dayTotalSpend),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = ChromaRed,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        items(dayGroup.transactions, key = { it.id }) { tx ->
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

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            shape = RoundedCornerShape(4.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "DELETE_RECORD // CONFIRM",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Delete record for ${FormatUtils.formatCurrency(tx.amount)}?",
                    style = MaterialTheme.typography.bodyMedium
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
                    shadowOffset = 2.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}
