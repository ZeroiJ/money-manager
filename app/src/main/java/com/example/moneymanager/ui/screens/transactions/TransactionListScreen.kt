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
import com.example.moneymanager.data.model.TransactionType
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "TRANSACTIONS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Boxy Search Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .neoShadow(offset = 3.dp, cornerRadius = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = query,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = {
                            Text(
                                "Search notes, categories, amounts...",
                                style = MaterialTheme.typography.bodyMedium
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
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Boxy Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isSelected) {
                                    when (scope) {
                                        TransactionScope.PERSONAL -> NeoBlue
                                        TransactionScope.HOUSEHOLD -> NeoOrange
                                        else -> NeoBlack
                                    }
                                } else MaterialTheme.colorScheme.surface
                            )
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .clickable { viewModel.scopeFilter.value = scope }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = if (isSelected) NeoWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Payment Mode Filter Chips
                PaymentMode.values().forEach { mode ->
                    val isSelected = modeFilter == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) NeoYellow else MaterialTheme.colorScheme.surface)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.paymentModeFilter.value = if (isSelected) null else mode
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mode.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = if (isSelected) NeoBlack else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredTxs.size} TRANSACTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    text = "TOTAL: ${FormatUtils.formatCurrency(totalFilteredSpend)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = NeoRed
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
                        text = "NO TRANSACTIONS FOUND",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedTxs.forEach { dayGroup ->
                        // Sticky Date Header Block
                        stickyHeader {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeoGray100)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dayGroup.dateLabel.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = NeoBlack
                                    )
                                    if (dayGroup.dayTotalSpend > 0) {
                                        Text(
                                            text = "- " + FormatUtils.formatCurrency(dayGroup.dayTotalSpend),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = NeoRed
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Transaction Cards for this Date
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
    }

    // Delete Confirmation Dialog (Neo-Brutalist Dialog)
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            shape = RoundedCornerShape(6.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "DELETE TRANSACTION",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently remove this transaction of ${FormatUtils.formatCurrency(tx.amount)}?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                NeoButton(
                    text = "DELETE",
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    backgroundColor = NeoRed,
                    textColor = NeoWhite,
                    borderColor = NeoBlack,
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
