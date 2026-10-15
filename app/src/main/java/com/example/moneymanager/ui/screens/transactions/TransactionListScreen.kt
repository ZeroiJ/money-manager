package com.example.moneymanager.ui.screens.transactions

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
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.PaymentMode
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionScope
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.IosGroupedList
import com.example.moneymanager.ui.ios.IosRow
import com.example.moneymanager.ui.ios.IosSegmented
import com.example.moneymanager.ui.ios.LoadErrorCard
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.maskedCurrency
import com.example.moneymanager.ui.kinetic.KineticHeader
import com.example.moneymanager.ui.kinetic.LedgerAmount
import com.example.moneymanager.ui.kinetic.MonoLabel
import com.example.moneymanager.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
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
    val loadError by viewModel.loadError.collectAsState()
    val scheme = currentScheme()

    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        KineticHeader(
            lead = "All recent",
            headline = "Transactions",
            headlineSize = 28.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search", fontFamily = SpaceGrotesk) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = scheme.secondaryLabel
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.searchQuery.value = "" },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = scheme.secondaryLabel
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RectangleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = scheme.accent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = scheme.card,
                unfocusedContainerColor = scheme.card
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        val scopes = listOf(null, TransactionScope.PERSONAL, TransactionScope.HOUSEHOLD)
        IosSegmented(
            options = listOf("All", "Personal", "Household"),
            selected = scopes.indexOf(scopeFilter).coerceAtLeast(0),
            onSelect = { viewModel.scopeFilter.value = scopes[it] },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            scheme = scheme
        )

        Spacer(modifier = Modifier.height(8.dp))

        val payModes = listOf(null, PaymentMode.UPI, PaymentMode.CASH, PaymentMode.CARD)
        IosSegmented(
            options = listOf("All modes", "UPI", "Cash", "Card"),
            selected = payModes.indexOf(modeFilter).coerceAtLeast(0),
            onSelect = { viewModel.paymentModeFilter.value = payModes[it] },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            scheme = scheme
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${filteredTxs.size} records · ${maskedCurrency(totalFilteredSpend)}",
            fontFamily = SpaceGrotesk,
            fontSize = 13.sp,
            color = scheme.secondaryLabel,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (loadError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                LoadErrorCard(
                    message = loadError ?: "Couldn't load transactions",
                    onRetry = { viewModel.retryLoad() }
                )
            }
        } else if (filteredTxs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RectangleShape)
                    .background(scheme.card)
                    .padding(vertical = 32.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No matching records",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = scheme.label
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing the search or filters.",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        color = scheme.secondaryLabel
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedTxs.forEach { dayGroup ->
                    item(key = "day_${dayGroup.dateLabel}") {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dayGroup.dateLabel,
                                    fontFamily = SpaceGrotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = scheme.secondaryLabel
                                )
                                if (dayGroup.dayTotalSpend > 0) {
                                    IosAmount(
                                        text = "−" + FormatUtils.formatCurrency(dayGroup.dayTotalSpend),
                                        color = scheme.destructive,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(scheme.slab)
                            ) {
                                dayGroup.transactions.forEachIndexed { index, tx ->
                                    val category = categoriesMap[tx.categoryId]
                                    DeletableRow(
                                        tx = tx,
                                        categoryName = category?.name ?: "Misc",
                                        categoryColor = scheme.onSlab,
                                        categoryIcon = FormatUtils.getCategoryIcon(
                                            category?.icon.orEmpty()
                                        ),
                                        rowAlt = index % 2 == 1,
                                        onClick = { onNavigateToEditTransaction(tx.id) },
                                        onSwipeToDelete = { transactionToDelete = tx }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            shape = RectangleShape,
            containerColor = scheme.card,
            title = {
                Text(
                    text = "Delete transaction",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = scheme.label
                )
            },
            text = {
                Text(
                    text = "Delete record for ${FormatUtils.formatCurrency(tx.amount)}?",
                    fontFamily = SpaceGrotesk,
                    fontSize = 15.sp,
                    color = scheme.label
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.destructive)
                ) {
                    Text("Delete", fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel", fontFamily = SpaceGrotesk, color = scheme.accent)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeletableRow(
    tx: Transaction,
    categoryName: String,
    categoryColor: Color,
    categoryIcon: ImageVector,
    rowAlt: Boolean,
    onClick: () -> Unit,
    onSwipeToDelete: () -> Unit
) {
    val scheme = currentScheme()
    val expense = tx.type == TransactionType.EXPENSE
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.Settled) {
                false
            } else {
                onSwipeToDelete()
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.destructive),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        // PRD §4.1 row anatomy: icon tile · merchant · category · amount.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (rowAlt) scheme.slabAlt else scheme.slab)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(scheme.onSlab.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.note.ifBlank { categoryName },
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = scheme.onSlab,
                    maxLines = 1
                )
                MonoLabel(
                    text = "$categoryName · ${tx.paymentMode.name}",
                    color = scheme.onSlabSecondary,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            LedgerAmount(
                text = (if (expense) "−" else "+") + FormatUtils.formatLedger(tx.amount),
                fontSize = 15.sp,
                color = scheme.onSlab,
                dimmedColor = scheme.onSlabSecondary
            )
        }
    }
}
