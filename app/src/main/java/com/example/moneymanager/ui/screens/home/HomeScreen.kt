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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.moneymanager.ui.kinetic.BarcodeMeter
import com.example.moneymanager.ui.kinetic.Hairline
import com.example.moneymanager.ui.kinetic.KineticInk
import com.example.moneymanager.ui.kinetic.KineticHeader
import com.example.moneymanager.ui.kinetic.LedgerAmount
import com.example.moneymanager.ui.kinetic.LedgerPill
import com.example.moneymanager.ui.kinetic.MetricRow
import com.example.moneymanager.ui.kinetic.MonoLabel
import com.example.moneymanager.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tab 01 — Home Dashboard (PRD §4.1): LEDGER // 01 header, available-budget
 * gauge with barcode meter, terracotta overrun banner, ochre quick-assign,
 * recent ledger slab rows, footer diagnostics.
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
    onNavigateToBudgets: () -> Unit = {},
    onNavigateToEditTransaction: (Long) -> Unit = {}
) {
    val recentTxs by viewModel.recentTransactions.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val categoriesMap by viewModel.categoriesMap.collectAsState()
    val todaySpend by viewModel.todaySpend.collectAsState()
    val monthSpend by viewModel.monthSpend.collectAsState()
    val monthBudget by viewModel.monthBudget.collectAsState()
    val overruns by viewModel.overruns.collectAsState()
    val scheme = currentScheme()
    val monthName = remember {
        FormatUtils.formatMonth(FormatUtils.getCurrentMonthKey()).substringBefore(" ")
    }

    // FILTER cycles the visible ledger: ALL → EXPENSE → INCOME (PRD §4.1).
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

    val hasBudget = monthBudget > 0
    val left = (monthBudget - monthSpend).coerceAtLeast(0.0)
    val usedFraction = if (hasBudget) (monthSpend / monthBudget).toFloat() else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header: monogram + LEDGER // 01 + utilities ─────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
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

        // ── Available budget gauge (PRD §4.1) ───────────────────────────
        item {
            Column {
                KineticHeader(
                    lead = if (hasBudget) "Available $monthName" else "$monthName spend",
                    headline = if (hasBudget) "Budget" else "Ledger",
                    headlineSize = 30.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LedgerAmount(
                        text = FormatUtils.formatLedger(if (hasBudget) left else monthSpend),
                        fontSize = 46.sp,
                        color = scheme.label,
                        dimmedColor = scheme.secondaryLabel
                    )
                    if (hasBudget) {
                        Spacer(modifier = Modifier.width(10.dp))
                        val pctLeft = ((left / monthBudget) * 100).toInt()
                        LedgerPill(
                            text = "$pctLeft% LEFT",
                            container = scheme.accent,
                            content = scheme.label
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                MetricRow(label = "Budget", value = FormatUtils.formatLedger(monthBudget))
                Spacer(modifier = Modifier.height(6.dp))
                BarcodeMeter(
                    fraction = if (hasBudget) 1f else 0f,
                    color = scheme.label,
                    inactiveColor = scheme.separator,
                    total = 30,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                MetricRow(label = "Left", value = FormatUtils.formatLedger(if (hasBudget) left else monthSpend))
                BarcodeMeter(
                    fraction = if (hasBudget) usedFraction.coerceIn(0f, 1f) else 0f,
                    color = scheme.label,
                    inactiveColor = scheme.separator,
                    total = 30,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .padding(top = 4.dp)
                )
            }
        }

        // ── Exception & overrun module (PRD §4.1) ───────────────────────
        if (overruns.isNotEmpty()) {
            val first = overruns.first()
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(scheme.destructive)
                        .clickable { onNavigateToBudgets() }
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "!",
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = scheme.paper
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            MonoLabel(text = "Budget overrun", color = scheme.paper)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LedgerAmount(
                            text = FormatUtils.formatLedger(first.overspent),
                            fontSize = 30.sp,
                            color = scheme.paper,
                            dimmedColor = scheme.paper.copy(alpha = 0.6f)
                        )
                        MonoLabel(
                            text = first.name + (if (overruns.size > 1) " +${overruns.size - 1} more" else ""),
                            color = scheme.paper.copy(alpha = 0.85f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(scheme.paper.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↗",
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = scheme.paper
                        )
                    }
                }
            }
        }

        // ── Ochre quick-assign card (PRD §4.1) ──────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.accent)
                    .clickable { onNavigateToBudgets() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MonoLabel(text = "Allocations", color = KineticInk.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasBudget) "Tune $monthName budgets" else "Set your first budget",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = scheme.label
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MonoLabel(
                        text = if (hasBudget) FormatUtils.formatCurrency(monthBudget) + " / MONTH" else "NO CAP YET",
                        color = scheme.label.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = "+",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = KineticInk
                )
            }
        }

        // ── Recent transactions drawer (PRD §4.1) ───────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    onRetry = { viewModel.retryLoad() }
                )
            }
        } else if (filteredTxs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        text = "Log an expense from tab 03 in under 5 seconds.",
                        fontFamily = SpaceGrotesk,
                        fontSize = 15.sp,
                        color = scheme.secondaryLabel
                    )
                }
            }
        } else {
            val visible = if (showAll) filteredTxs.take(20) else filteredTxs.take(6)
            item {
                Column {
                    visible.forEachIndexed { i, tx ->
                        LedgerRow(
                            transaction = tx,
                            category = categoriesMap[tx.categoryId],
                            alt = i % 2 == 1,
                            onClick = { onNavigateToEditTransaction(tx.id) }
                        )
                    }
                }
            }
            if (filteredTxs.size > 6) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(scheme.slab)
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

        // ── Footer diagnostics (PRD §4.1) ───────────────────────────────
        item {
            Hairline()
            MonoLabel(
                text = "Synced with ledger node 01 // local only",
                color = scheme.secondaryLabel,
                modifier = Modifier.padding(top = 4.dp)
            )
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
 * Ledger slab row (PRD §4.1): icon tile, merchant/note, category label,
 * timestamp, tabular amount. Dark slab, alternating tone, square corners.
 */
@Composable
private fun LedgerRow(
    transaction: Transaction,
    category: Category?,
    alt: Boolean,
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
            .background(if (alt) scheme.slabAlt else scheme.slab)
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
                imageVector = FormatUtils.getCategoryIcon(category?.icon ?: "category"),
                contentDescription = null,
                tint = scheme.onSlab,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
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
        Column(horizontalAlignment = Alignment.End) {
            IosAmount(
                text = (if (isExpense) "−" else "+") + FormatUtils.formatCurrency(transaction.amount),
                color = scheme.onSlab,
                fontSize = 15.sp,
                rolling = false
            )
        }
    }
}
