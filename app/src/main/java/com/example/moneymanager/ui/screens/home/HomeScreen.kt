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
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
 * Tab 01 — Home Dashboard: LEDGER // 01 header, month-spend hero,
 * full-bleed recent-ledger drawer (single slab tone, no icons), footer
 * diagnostics. Strictly boxes — no rounded corners anywhere.
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
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val monthSpend by viewModel.monthSpend.collectAsState()
    val scheme = currentScheme()
    val monthName = remember {
        FormatUtils.formatMonth(FormatUtils.getCurrentMonthKey()).substringBefore(" ")
    }

    // FILTER cycles the visible ledger: ALL → EXPENSE → INCOME.
    var filterIndex by rememberSaveable { mutableStateOf(0) }
    val filterLabels = listOf("FILTER: ALL", "FILTER: OUT", "FILTER: IN")
    val filteredTxs = remember(recentTxs, filterIndex) {
        when (filterIndex) {
            1 -> recentTxs.filter { it.type == TransactionType.EXPENSE }
            2 -> recentTxs.filter { it.type == TransactionType.INCOME }
            else -> recentTxs
        }
    }
    var showAll by rememberSaveable { mutableStateOf(false) }

    // First-entry celebration: empty (after load) -> non-empty, once ever.
    var showConfetti by remember { mutableStateOf(false) }
    var celebratedFirstEntry by rememberSaveable { mutableStateOf(false) }
    var wasEmpty by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(recentTxs.isEmpty()) {
        val empty = recentTxs.isEmpty()
        if (firstEntryTransition(wasEmpty, empty, celebratedFirstEntry)) {
            celebratedFirstEntry = true
            showConfetti = true
        }
        wasEmpty = empty
    }

    // Ledger rows run edge-to-edge; every other block keeps 16dp insets.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header: monogram + LEDGER // 01 + utilities ─────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(scheme.slab),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MM",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = scheme.accent
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                MonoLabel(
                    text = "LEDGER // 01",
                    color = scheme.secondaryLabel,
                    modifier = Modifier.weight(1f)
                )
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

        // ── Month spend hero ────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                KineticHeader(
                    lead = "$monthName spend",
                    headline = "Ledger",
                    headlineSize = 30.sp
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

        // ── Recent transactions drawer — full-bleed slab block ──────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonoLabel(
                    text = "Recent ledger",
                    color = scheme.secondaryLabel,
                    modifier = Modifier.weight(1f)
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
        } else if (filteredTxs.isEmpty()) {
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
        } else {
            val visible = if (showAll) filteredTxs.take(20) else filteredTxs.take(6)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(scheme.slab)
                ) {
                    visible.forEachIndexed { i, tx ->
                        LedgerRow(
                            transaction = tx,
                            category = categoriesMap[tx.categoryId],
                            onClick = { onNavigateToEditTransaction(tx.id) }
                        )
                        if (i < visible.lastIndex) Hairline(color = scheme.onSlab.copy(alpha = 0.14f))
                    }
                    if (filteredTxs.size > 6) {
                        Hairline(color = scheme.onSlab.copy(alpha = 0.14f))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAll = !showAll }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            MonoLabel(
                                text = if (showAll) "SHOW LESS ∧" else "SHOW MORE ∨",
                                color = scheme.onSlab
                            )
                        }
                    }
                }
            }
        }

        // ── Footer diagnostics ──────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Hairline()
                MonoLabel(
                    text = "Synced with ledger node 01 // local only",
                    color = scheme.secondaryLabel,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    ConfettiOverlay(
        show = showConfetti,
        title = "First entry!",
        subtitle = "Your money story starts here.",
        onDone = { showConfetti = false }
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
