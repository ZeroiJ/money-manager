package com.example.moneymanager.ui.screens.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moneymanager.data.model.Category
import com.example.moneymanager.data.model.Transaction
import com.example.moneymanager.data.model.TransactionType
import com.example.moneymanager.theme.SpaceGrotesk
import com.example.moneymanager.ui.ios.ConfettiOverlay
import com.example.moneymanager.ui.ios.IosAmount
import com.example.moneymanager.ui.ios.LoadErrorCard
import com.example.moneymanager.ui.ios.currentScheme
import com.example.moneymanager.ui.ios.firstEntryTransition
import com.example.moneymanager.ui.ios.pressShrink
import com.example.moneymanager.ui.kinetic.Hairline
import com.example.moneymanager.ui.kinetic.KineticHeader
import com.example.moneymanager.ui.kinetic.LedgerAmount
import com.example.moneymanager.ui.kinetic.LedgerPill
import com.example.moneymanager.ui.kinetic.MonoLabel
import com.example.moneymanager.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tab 01 — Home Dashboard: utility row, bold month-spend hero, full
 * month-grouped transaction history (edge-to-edge single-slab blocks),
 * footer diagnostics. Strictly boxes — no rounded corners anywhere.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    presetCategory: String? = null,
    presetPaymentMode: String? = null,
    hideAmounts: Boolean = false,
    onToggleHideAmounts: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditTransaction: (Long) -> Unit = {}
) {
    val allTxs by viewModel.allTransactions.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val monthSpend by viewModel.monthSpend.collectAsState()
    val scheme = currentScheme()
    val monthName = remember {
        FormatUtils.formatMonth(FormatUtils.getCurrentMonthKey()).substringBefore(" ")
    }

    // FILTER cycles the visible history: ALL → EXPENSE → INCOME.
    var filterIndex by rememberSaveable { mutableStateOf(0) }
    val filterLabels = listOf("FILTER: ALL", "FILTER: OUT", "FILTER: IN")
    val filteredTxs = remember(allTxs, filterIndex) {
        when (filterIndex) {
            1 -> allTxs.filter { it.type == TransactionType.EXPENSE }
            2 -> allTxs.filter { it.type == TransactionType.INCOME }
            else -> allTxs
        }
    }
    // Full history, newest month first — easy expense backtracking.
    val monthGroups = remember(filteredTxs) {
        filteredTxs.groupBy { monthKeyOf(it.date) }.toSortedMap(compareByDescending { it })
    }

    // Swipe-right on a ledger row -> confirm dialog -> dao delete (v1.21).
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // First-entry celebration: empty (after load) -> non-empty, once ever.
    var showConfetti by remember { mutableStateOf(false) }
    var celebratedFirstEntry by rememberSaveable { mutableStateOf(false) }
    var wasEmpty by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(allTxs.isEmpty()) {
        val empty = allTxs.isEmpty()
        if (firstEntryTransition(wasEmpty, empty, celebratedFirstEntry)) {
            celebratedFirstEntry = true
            showConfetti = true
        }
        wasEmpty = empty
    }

    // History blocks run edge-to-edge; every other block keeps 16dp insets.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 44.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Utility row (privacy toggle + settings) ─────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleHideAmounts, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = if (hideAmounts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (hideAmounts) "Show amounts" else "Hide amounts",
                        tint = scheme.label
                    )
                }
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Settings",
                        tint = scheme.label
                    )
                }
            }
        }

        // ── Month spend hero — the month is the bold headline ────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                KineticHeader(
                    lead = "Monthly spend",
                    headline = monthName,
                    headlineSize = 34.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LedgerAmount(
                    text = FormatUtils.formatLedger(monthSpend),
                    fontSize = 46.sp,
                    color = scheme.label,
                    dimmedColor = scheme.secondaryLabel
                )
            }
        }

        // ── Full transaction history, grouped by month ──────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonoLabel(
                    text = "History",
                    color = scheme.secondaryLabel,
                    modifier = Modifier
                        .weight(1f)
                        .pressShrink()
                        .clickable(onClick = onNavigateToTransactions)
                )
                LedgerPill(
                    text = filterLabels[filterIndex],
                    container = scheme.secondaryFill,
                    content = scheme.label,
                    modifier = Modifier
                        .pressShrink()
                        .clickable { filterIndex = (filterIndex + 1) % filterLabels.size }
                )
            }
        }

        if (loadError != null) {
            item {
                LoadErrorCard(
                    message = loadError ?: "Couldn't load transactions",
                    onRetry = { viewModel.retryLoad() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else if (allTxs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(scheme.card)
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "A clean slate",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = scheme.label
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Log an expense from tab 02 in under 5 seconds.",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        color = scheme.secondaryLabel
                    )
                }
            }
        } else if (filteredTxs.isEmpty()) {
            item {
                MonoLabel(
                    text = "No entries for this filter",
                    color = scheme.secondaryLabel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        } else {
            monthGroups.forEach { (monthKey, txs) ->
                val monthTotal = txs
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                item(key = "month-$monthKey") {
                    MonoLabel(
                        text = FormatUtils.formatMonth(monthKey) +
                            " · " + FormatUtils.formatCurrency(monthTotal),
                        color = scheme.secondaryLabel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                item(key = "rows-$monthKey") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(scheme.slab)
                    ) {
                        txs.forEachIndexed { i, tx ->
                            SwipeToDeleteRow(
                                onSwipeToDelete = { transactionToDelete = tx },
                                content = {
                                    LedgerRow(
                                        transaction = tx,
                                        category = categoriesMap[tx.categoryId],
                                        onClick = { onNavigateToEditTransaction(tx.id) }
                                    )
                                }
                            )
                            if (i < txs.lastIndex) {
                                Hairline(color = scheme.onSlab.copy(alpha = 0.14f))
                            }
                        }
                    }
                }
            }
        }

    }

    ConfettiOverlay(
        show = showConfetti,
        title = "First entry!",
        subtitle = "Your money story starts here.",
        onDone = { showConfetti = false }
    )

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

/** Month key `yyyy-MM` for grouping history (matches FormatUtils keys). */
private fun monthKeyOf(date: Long): String {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = date }
    return String.format(
        Locale.US, "%04d-%02d",
        cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1
    )
}

/**
 * Ledger slab row: merchant/note, category label, timestamp, tabular
 * amount. Full-bleed, single dark slab tone, no icons, hairline-separated.
 */
@Composable
private fun LedgerRow(
    transaction: Transaction,
    category: Category?,
    onClick: () -> Unit
) {
    val scheme = currentScheme()
    val isExpense = transaction.type == TransactionType.EXPENSE
    val timeText = remember(transaction.date) {
        val now = java.util.Calendar.getInstance()
        val then = java.util.Calendar.getInstance().apply { timeInMillis = transaction.date }
        if (now.get(java.util.Calendar.DAY_OF_YEAR) == then.get(java.util.Calendar.DAY_OF_YEAR) &&
            now.get(java.util.Calendar.YEAR) == then.get(java.util.Calendar.YEAR)
        ) {
            SimpleDateFormat("hh:mm a", Locale.US).format(Date(transaction.date))
        } else {
            FormatUtils.formatDate(transaction.date)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (transaction.note.isNotBlank()) transaction.note else (category?.name ?: "Expense"),
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = scheme.onSlab,
                maxLines = 1
            )
            MonoLabel(
                text = "${category?.name ?: "Misc"} · $timeText",
                color = scheme.onSlabSecondary,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IosAmount(
            text = (if (isExpense) "−" else "+") + FormatUtils.formatCurrency(transaction.amount),
            color = scheme.onSlab,
            fontSize = 15.sp,
            rolling = false
        )
    }
}

/**
 * v1.21 delete gesture: swipe a ledger row RIGHT — the slab slides right
 * and the destructive red bar is revealed underneath, following the finger.
 * Release past the threshold opens the confirm dialog; the row always snaps
 * back and is only removed once the dialog's Delete is tapped. Right-only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRow(
    onSwipeToDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val scheme = currentScheme()
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
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scheme.destructive),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(start = 20.dp)
                )
            }
        }
    ) {
        // Opaque slab layer: masks the red background while settled (rows
        // draw over the parent slab, so without this it bleeds through).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.slab)
        ) {
            content()
        }
    }
}
